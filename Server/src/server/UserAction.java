package server;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.Global;
import models.User;
import utilities.NotificationHandler;

/**
 * This class contains methods for the things a user does on thier own account, or requests
 * to other users accounts.  
 * Things like:
 * - Logging in
 * - Registering an account
 *
 */
public class UserAction {
	
	/**
	 * Push update data to the sever *IF* it has changed.
	 */
	static void save(ServerConnection connection) {
		Global.log(connection.clientNumber, "Save was issued");
		if (connection.user != null && connection.user.getIsDirty()) {
			Global.log(connection.clientNumber, "User data was updated. Saving to database");

			PreparedStatement ps;
			try {
				ps = connection.databaseConnection.prepareStatement("update users set " + connection.user.updateSyntax() + "where email='"
						+ connection.user.getEmail() + ";");//' and password='" + connection.user.getPassword() + "';");
				int rows = ps.executeUpdate();
				// Should only get 1 row was affected.
				if (rows != 1) {
					throw new SQLException();
				}
				connection.user.setClean();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check if the email is in use, and register a new fresh user.
	 * 
	 * @param data
	 */
	static void register(ServerConnection connection, String data) {	
		Global.log(connection.clientNumber, "Trying to register new user");

		try {
			// Attempt to map email and password to an object.
			User u = connection.mapper.readValue(data, User.class);
			ResultSet resultSet = connection.databaseConnection
					.executeSQL("select * from users where email='" + u.getEmail() + "';");

			if (resultSet.next()) {
				connection.error("email already in use");
				Global.log(connection.clientNumber, "User attempted to reregister email: " + u.getEmail());
				return;
			}

			// Unique email, create a new user
			User newUser = new User(u.getEmail(), DigestUtils.sha512Hex(u.getPassword()), 0, u.getDeviceToken(), false);
			// Insert into the database.
			PreparedStatement ps = connection.databaseConnection.prepareStatement(
					"insert into users " + Global.USERS_TABLE_COLUMNS + " values " + newUser.toSqlValueString());
			// Should only get 1 row was affected.
			int rows = ps.executeUpdate();
			if (rows == 1) {
				connection.user = newUser;
			} else {
				throw new SQLException();
			}

			// Send success, and the user JSON string so client has it as well.
			connection.sendResponse(Command.REGISTER, connection.user.toJson(connection.mapper));
			Global.log(connection.clientNumber, "New user registered - " + connection.user.toJson(connection.mapper));

		} catch (JsonParseException e) {
			Global.log(connection.clientNumber, "Recieved malformed data packet");
			connection.error(Message.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			Global.log(connection.clientNumber, "Bad sql " + e);
			connection.error(Message.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			Global.log(connection.clientNumber, "Something went wrong mapping user to object" + e);
			connection.error(Message.SERVER_ERROR_RETRY);
		}

	}

	/**
	 * Check if the account exists, then Global.log them into the server, and send a
	 * copy of user data back.
	 * 
	 * @param data
	 */
	static void login(ServerConnection connection, String data) {
		Global.log(connection.clientNumber, "Trying to Login with ");

		try {
			User u = connection.mapper.readValue(data, User.class);
			ResultSet resultSet = connection.databaseConnection.executeSQL(
					"select * from users where email='" + u.getEmail() + "' and password='" + DigestUtils.sha512Hex(u.getPassword()) + "';");

			if (!resultSet.next()) {
				connection.sendResponse(Command.ERROR, Message.BAD_CREDENTIALS);
				Global.log(connection.clientNumber, "Invalid login creditials for user: " + u.getEmail());
				Global.log(connection.clientNumber, "Does user exist?");
				return;
			}

			// Map from database to object
			connection.user = new User(resultSet);

			// Check if the user is on a new device. If so, update so we can send
			// notifications to it.
			if (connection.user.getDeviceToken() != null && !connection.user.getDeviceToken().equals(u.getDeviceToken())
					&& !connection.user.getDeviceToken().isEmpty()) {
				connection.user.setDeviceToken(u.getDeviceToken());
				save(connection);
			}

			connection.sendResponse(Command.LOGIN, connection.user.toJson(connection.mapper));
			Global.log(connection.clientNumber, "User logged in - " + connection.user.toJson(connection.mapper));

		} catch (JsonParseException e) {
			Global.log(connection.clientNumber, "Recieved malformed data packet");
			connection.error(Message.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			Global.log(connection.clientNumber, "Bad sql " + e);
			connection.error(Message.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			Global.log(connection.clientNumber, "Something went wrong mapping user to object " + e);
			connection.error(Message.SERVER_ERROR_RETRY);
		}

	}

	static void friendRequest(ServerConnection connection, String data) throws JsonParseException, IOException, JSONException, SQLException {
		Global.log(connection.clientNumber, "Sending friend Request");
		Global.log(connection.clientNumber, data);

		// Check if issuing user is logged in
		if (connection.user == null || connection.user.getEmail().isEmpty()) {
			connection.error(Message.NOT_LOGGEDIN);
			return;
		}

		String email = null;

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(data);

		// Get the email of the user they want to be friends with
		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}
			if (JsonToken.FIELD_NAME.equals(token) && "email".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				email = parser.getText();
			}
		}

		// Make sure data was valid
		if (email == null || email.isEmpty()) {
			connection.error(Message.MALFORMED_DATA_PACKET);
			return;
		}

		ResultSet resultSet = connection.databaseConnection
				.executeSQL("select * from users where email='" + email + "';");

		// check to make sure the requested user exists...
		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		User requested = new User(resultSet);
		
		
		// Check if we are already friends
		for (String friend : requested.getFriends()) {
			if (connection.user.getEmail().equals(friend)) {
				connection.sendResponse(Command.ERROR, Message.USERS_ALREADY_FRIENDS);
				return;
			}
		}
		
		// Check if we are already in their pending requests
		for (String request : requested.getfriendRequests()) {
			if (connection.user.getEmail().equals(request)) {
				connection.sendResponse(Command.ERROR, Message.REQUEST_ALREADY_PENDING);
				return;
			}
		}
		
		// Made it here, valid friend request.  Add us to their pending friend requests, this way if they are not
		// currently logged in, the client gets accurate info from the server.  Means client also needs to verify
		// That is does not add duplicate users to friends / requests.
		requested.addFriendRequest(connection.user.getEmail());
		
		saveAccount(connection, requested.toJson(new ObjectMapper()));
		
		// Send notification to user. If they are logged in they can add user to pending friends and on close will overwrite the server data.
		// If they were not logged in, then they will need to make sure to check for duplicates.
		JSONObject notificationData = new JSONObject();
		notificationData.put(Command.FRIEND_REQUEST, connection.user.getEmail());

		boolean success = NotificationHandler.sendPushNotification(requested.getDeviceToken(), Message.NEW_FRIEND_REQUEST_TITLE,
				connection.user.getEmail() + Message.NEW_FRIEND_REQUEST_BODY, notificationData);
		
		// Data was good. Send it back and let device add friend.
		if (success) {
			connection.sendResponse(Command.FRIEND_REQUEST_SUCCESS, data);
		} else {
			connection.sendResponse(Command.ERROR, Message.SERVER_ERROR_RETRY);
		}
	}
	
	// We accepted someone elses friend request. make sure they are in our friends list (on server) so we can battle.
	static void friendAdded(ServerConnection connection, String data) throws JsonParseException, IOException, SQLException {
		
		if (connection.user == null || connection.user.getEmail().isEmpty()) {
			connection.error(Message.NOT_LOGGEDIN);
			return;
		}

		String email = null;

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(data);

		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}

			if (JsonToken.FIELD_NAME.equals(token) && "email".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				email = parser.getText();
			}
		}

		if (email == null || email.isEmpty()) {
			connection.error(Message.MALFORMED_DATA_PACKET);
			return;
		}
		
		// Add them to us
		connection.user.addFriend(email);
		// save back to DB
		save(connection);
		
		ResultSet resultSet = connection.databaseConnection
				.executeSQL("select * from users where email='" + email + "';");

		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		User requested = new User(resultSet);
		
		// Make sure we are added to them..
		requested.addFriend(connection.user.getEmail());
		// update DB so their user is correct
		saveAccount(connection, requested.toJson(new ObjectMapper()));
		
	}

	static void battleRequest(ServerConnection connection, String data) throws JsonParseException, IOException, JSONException, SQLException {
		Global.log(connection.clientNumber, "Sending battle Request");
		Global.log(connection.clientNumber, data);

		if (connection.user == null || connection.user.getEmail().isEmpty()) {
			connection.error(Message.NOT_LOGGEDIN);
			return;
		}

		String email = null;

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(data);

		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}

			if (JsonToken.FIELD_NAME.equals(token) && "email".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				email = parser.getText();
			}
		}

		if (email == null || email.isEmpty()) {
			connection.error(Message.MALFORMED_DATA_PACKET);
			return;
		}

		ResultSet resultSet = connection.databaseConnection
				.executeSQL("select * from users where email='" + email + "';");

		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		User requested = new User(resultSet);
		
		// Get requested users friends
		ArrayList<String> friends = requested.getFriends();
		// Check if we are already friends
		boolean areFriends = false;
		for (String friend : friends) {
			if (connection.user.getEmail().equals(friend)) {
				areFriends = true;
				break;
			}
		}
		
		// If users are not friends, they can't battle.
		if (!areFriends) {
			connection.sendResponse(Command.ERROR, Message.MUST_BE_FRIENDS_TODO);
			return;
		}
		
		JSONObject notificationData = new JSONObject();
		notificationData.put(Command.BATTLE_REQUEST, connection.user.getEmail());

		boolean success = NotificationHandler.sendPushNotification(requested.getDeviceToken(), Message.NEW_BATTLE_REQUEST_TITLE,
				connection.user.getEmail() + Message.NEW_BATTLE_REQUEST_BODY, notificationData);
		
		if (success) {
			connection.sendResponse(Command.BATTLE_REQUEST, Message.BATTLE_REQUEST_SENT);
		} else {
			connection.sendResponse(Command.ERROR, Message.SERVER_ERROR_RETRY);
		}
	}
	
	static void battleAccepted(ServerConnection connection, String data) throws JsonParseException, IOException, SQLException, JSONException {
		String email = null;

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(data);

		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}

			if (JsonToken.FIELD_NAME.equals(token) && "email".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				email = parser.getText();
			}
		}

		if (email == null || email.isEmpty()) {
			connection.error(Message.MALFORMED_DATA_PACKET);
			return;
		}

		ResultSet resultSet = connection.databaseConnection
				.executeSQL("select * from users where email='" + email + "';");

		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		User requested = new User(resultSet);
		
		JSONObject notificationData = new JSONObject();
		notificationData.put(Command.BATTLE_ACCEPTED, connection.user.getEmail());

		boolean success = NotificationHandler.sendPushNotification(requested.getDeviceToken(), Message.BATTLE_ACCEPTED,
				connection.user.getEmail() + Message.BATTLE_ACCEPTED_BODY, notificationData);
		
		if (success) {
			connection.sendResponse(Command.BATTLE_ACCEPTED, Command.SUCCESS);
		} else {
			connection.sendResponse(Command.ERROR, Message.SERVER_ERROR_RETRY);
		}
	}

	static void saveAccount(ServerConnection connection, String data) throws JsonParseException, JsonMappingException, IOException {	
		Global.log(connection.clientNumber, data);
		User user = connection.mapper.readValue(data, User.class);
		
		Global.log(connection.clientNumber, "Saving client account data back to server for: " + user.getEmail());

		PreparedStatement ps;
		try {
			ps = connection.databaseConnection.prepareStatement("update users set " + user.updateSyntax() + " where email='"
					+ user.getEmail() + "';");// and password='" + user.getPassword() + "';");
			
			int rows = ps.executeUpdate();
			// Should only get 1 row was affected.
			if (rows != 1) {
				connection.sendResponse(Command.ERROR, Message.COULD_NOT_SAVE_ACCOUNT);
				throw new SQLException();
			}
			
		} catch (SQLException e) {
			connection.sendResponse(Command.ERROR, Message.COULD_NOT_SAVE_ACCOUNT);
			e.printStackTrace();
		}
		
		connection.sendResponse(Command.SAVE_USER, Command.SUCCESS);
	}

	static void getParty(ServerConnection connection, String data) throws JsonParseException, IOException, SQLException {
		Global.log(connection.clientNumber, Command.GET_PARTY + ": " + data);
		
		String email = null;

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(data);

		while (!parser.isClosed()) {
			JsonToken token = parser.nextToken();
			if (token == null) {
				break;
			}

			if (JsonToken.FIELD_NAME.equals(token) && "email".equals(parser.getCurrentName())) {
				token = parser.nextToken();
				email = parser.getText();
			}
		}

		if (email == null || email.isEmpty()) {
			connection.error(Message.MALFORMED_DATA_PACKET);
			Global.log(connection.clientNumber, Command.ERROR + ": " + Message.MALFORMED_DATA_PACKET);
			return;
		}

		ResultSet resultSet = connection.databaseConnection
				.executeSQL("select * from users where email='" + email + "';");

		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.USER_DOES_NOT_EXIST);
			Global.log(connection.clientNumber, Command.ERROR + ": " + Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		User requested = new User(resultSet);
		
		connection.sendResponse(Command.GET_PARTY, "{\"party\":\"" + requested.getParty() + "\"}");
	}
	
}
