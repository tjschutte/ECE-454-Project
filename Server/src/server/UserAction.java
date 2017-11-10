package server;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;

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
	static void save(ServerThread connection) {
		Global.log(connection.clientNumber, "Save was issued");
		if (connection.user != null && connection.user.getIsDirty()) {
			Global.log(connection.clientNumber, "User data was updated. Saving to database");

			PreparedStatement ps;
			try {
				ps = connection.databaseConnection.prepareStatement("update users set " + connection.user.updateSyntax() + "where email='"
						+ connection.user.getEmail() + "' and password='" + connection.user.getPassword() + "';");
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
	static void register(ServerThread connection, String data) {
		Global.log(connection.clientNumber, "Trying to register new user");
		Global.log(connection.clientNumber, data);

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
			User newUser = new User(u.getEmail(), u.getPassword(), 0, u.getDeviceToken(), false);
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
	static void login(ServerThread connection, String data) {
		Global.log(connection.clientNumber, "Trying to Login with ");
		Global.log(connection.clientNumber, data);

		try {
			User u = connection.mapper.readValue(data, User.class);
			ResultSet resultSet = connection.databaseConnection.executeSQL(
					"select * from users where email='" + u.getEmail() + "' and password='" + u.getPassword() + "';");

			if (!resultSet.next()) {
				connection.sendResponse(Command.ERROR, Message.BAD_CREDENTIALS);
				Global.log(connection.clientNumber, "Invalid Global.login creditials for user: " + u.getEmail());
				Global.log(connection.clientNumber, "Does user exist?");
				return;
			}

			// Map from database to object
			//String email, String password, String party, String encounteredHumons, String friends, String friendRequests, int hcount, String deviceToken, boolean isDirty
			connection.user = new User(resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
					resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getInt(8), resultSet.getString(9), false);

			// Check if the user is on a new device. If so, update so we can send
			// notifications to it.
			if (connection.user.getDeviceToken() != null && !connection.user.getDeviceToken().equals(u.getDeviceToken())
					&& !connection.user.getDeviceToken().isEmpty()) {
				connection.user.setDeviceToken(u.getDeviceToken());
				save(connection);
			}

			connection.sendResponse(Command.LOGIN, connection.user.toJson(connection.mapper));


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

	static void friendRequest(ServerThread connection, String data) throws JsonParseException, IOException, JSONException, SQLException {
		Global.log(connection.clientNumber, "Sending friend Request");
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
				.executeSQL("select deviceToken from users where email='" + email + "';");

		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		JSONObject notificationData = new JSONObject();
		notificationData.put(Command.FRIEND_REQUEST, connection.user.getEmail());

		NotificationHandler.sendPushNotification(resultSet.getString(1), Message.NEW_FRIEND_REQUEST_TITLE,
				connection.user.getEmail() + Message.NEW_FRIEND_REQUEST_BODY, notificationData);
		connection.sendResponse(Command.FRIEND_REQUEST, Message.FRIEND_REQUEST_SENT);
	}

	static void battleRequest(ServerThread connection, String data) throws JsonParseException, IOException, JSONException, SQLException {
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
				.executeSQL("select deviceToken from users where email='" + email + "';");

		if (!resultSet.next()) {
			connection.sendResponse(Command.ERROR, Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		JSONObject notificationData = new JSONObject();
		notificationData.put(Command.BATTLE_REQUEST, connection.user.getEmail());

		NotificationHandler.sendPushNotification(resultSet.getString(1), Message.NEW_BATTLE_REQUEST_TITLE,
				connection.user.getEmail() + Message.NEW_BATTLE_REQUEST_BODY, notificationData);
		connection.sendResponse(Command.BATTLE_REQUEST, Message.BATTLE_REQUEST_SENT);
	}

	public static void saveAccount(ServerThread connection, String data) throws JsonParseException, JsonMappingException, IOException {	
		User user = connection.mapper.readValue(data, User.class);
		
		Global.log(connection.clientNumber, "User data was updated. Saving to database");

		PreparedStatement ps;
		try {
			ps = connection.databaseConnection.prepareStatement("update users set " + user.updateSyntax() + "where email='"
					+ user.getEmail() + "' and password='" + user.getPassword() + "';");
			
			int rows = ps.executeUpdate();
			// Should only get 1 row was affected.
			if (rows != 1) {
				connection.sendResponse(Command.ERROR, Message.COULD_NOT_SAVE_ACCOUNT);
				throw new SQLException();
			}
			connection.user.setClean();
		} catch (SQLException e) {
			connection.sendResponse(Command.ERROR, Message.COULD_NOT_SAVE_ACCOUNT);
			e.printStackTrace();
		}
		
		connection.sendResponse(Command.SAVE_USER, Command.SUCCESS);
	}
	
}
