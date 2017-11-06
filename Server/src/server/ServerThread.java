package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.Global;
import models.Humon;
import models.User;
import utilities.Connector;
import utilities.NotificationHandler;
import utilities.SQLHelper;

public class ServerThread extends Thread {

	/**
	 * A private thread to handle requests on a particular socket. The client
	 * terminates the diaGlobal.logue by sending a single line containing only a
	 * period.
	 */
	private Socket socket;
	private int clientNumber;
	private Connector databaseConnection;
	private ObjectMapper mapper;
	private BufferedReader clientIn;
	private PrintWriter clientOut;
	private User user;

	public ServerThread(Socket socket, int clientNumber) throws IOException {
		this.socket = socket;
		this.clientNumber = clientNumber;
		Global.log(clientNumber, "connected at " + socket);
		mapper = new ObjectMapper();
		databaseConnection = new Connector(Global.DATABASE_NAME, Global.TABLE_NAME, Global.DATABASE_USER_NAME,
				Global.DATABASE_USER_PASSWORD, Global.DEFAULT_CONNECTIONS);
		// Connect to the database and table
		databaseConnection.startConnection();

		clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		clientOut = new PrintWriter(socket.getOutputStream());
	}

	/**
	 * Services this thread's client by first sending the client a welcome message
	 * then repeatedly reading strings and sending back the capitalized version of
	 * the string.
	 */
	public void run() {
		try {
			String input;
			String command;
			String data;

			while (true) {

				// Note: this line is blocking
				input = clientIn.readLine();
				// Fast input checking. Error on bad command, check to see if they
				// wanted to close the connection otherwise
				if (input == null || input.length() == 0 || input.equals(".")) {
					Global.log(clientNumber, "Saving any dirty data and disconnecting from server.");
					save();
					break;
				} else if (input.indexOf(':') == -1) {
					if (input.toUpperCase().equals(Commands.LOGOUT)) {
						break;
					} else {
						error(Message.BAD_COMMAND);
						continue;
					}
				}

				command = input.substring(0, input.indexOf(':'));
				command = command.toUpperCase();
				data = input.substring(input.indexOf(':') + 1, input.length());

				Global.log(clientNumber, command);

				switch (command) {
				case Commands.REGISTER:
					register(data);
					break;
				case Commands.LOGIN:
					login(data);
					break;
				case Commands.CREATE_HUMON:
					createNewHumon(data);
					break;
				case Commands.FRIEND_REQUEST:
					friendRequest(data);
					break;
				default:
					error(Message.BAD_COMMAND);
					break;
				}

				clientOut.flush();
			}
		} catch (IOException | JSONException | SQLException e) {
			Global.log(clientNumber, "had error - " + e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				Global.log(clientNumber, "Couldn't close a socket, what's going on?");
			}
			Global.log(clientNumber, "closed");
		}
	}

	/**
	 * Push update data to the sever *IF* it has changed. TODO: Make this actually
	 * save the user.
	 */
	private void save() {
		Global.log(clientNumber, "Save was issued");
		if (user != null && user.getIsDirty()) {
			Global.log(clientNumber, "User data was updated. Saving to database");

			PreparedStatement ps;
			try {
				ps = databaseConnection.prepareStatement("update users set " + user.updateSyntax() + "where email='"
						+ user.getEmail() + "' and password='" + user.getPassword() + "';");
				int rows = ps.executeUpdate();
				// Should only get 1 row was affected.
				if (rows != 1) {
					throw new SQLException();
				}
				user.setClean();
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
	private void register(String data) {
		Global.log(clientNumber, "Trying to register new user");
		Global.log(clientNumber, data);

		try {
			// Attempt to map email and password to an object.
			User u = mapper.readValue(data, User.class);
			ResultSet resultSet = databaseConnection
					.executeSQL("select * from users where email='" + u.getEmail() + "';");

			if (resultSet.next()) {
				error("email already in use");
				Global.log(clientNumber, "User attempted to reregister email: " + u.getEmail());
				return;
			}

			// Unique email, create a new user
			User newUser = new User(u.getEmail(), u.getPassword(), 0, u.getDeviceToken(), false);
			// Insert into the database.
			PreparedStatement ps = databaseConnection.prepareStatement(
					"insert into users " + Global.USERS_TABLE_COLUMNS + " values " + newUser.toSqlValueString());
			// Should only get 1 row was affected.
			int rows = ps.executeUpdate();
			if (rows == 1) {
				user = newUser;
			} else {
				throw new SQLException();
			}

			// Send success, and the user JSON string so client has it as well.
			sendResponse(Commands.SUCCESS, user.toJson(mapper));

		} catch (JsonParseException e) {
			Global.log(clientNumber, "Recieved malformed data packet");
			error(Message.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			Global.log(clientNumber, "Bad sql " + e);
			error(Message.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			Global.log(clientNumber, "Something went wrong mapping user to object" + e);
			error(Message.SERVER_ERROR_RETRY);
		}

	}

	/**
	 * Check if the account exists, then Global.log them into the server, and send a
	 * copy of user data back.
	 * 
	 * @param data
	 */
	private void login(String data) {
		Global.log(clientNumber, "Trying to Login with ");
		Global.log(clientNumber, data);

		try {
			User u = mapper.readValue(data, User.class);
			ResultSet resultSet = databaseConnection.executeSQL(
					"select * from users where email='" + u.getEmail() + "' and password='" + u.getPassword() + "';");

			if (!resultSet.next()) {
				sendResponse(Commands.ERROR, Message.BAD_CREDENTIALS);
				Global.log(clientNumber, "Invalid Global.login creditials for user: " + u.getEmail());
				Global.log(clientNumber, "Does user exist?");
				return;
			}

			// Map from database to object
			user = new User(resultSet.getString(2), resultSet.getString(3), resultSet.getString(4),
					resultSet.getString(5), resultSet.getString(6), resultSet.getInt(7), resultSet.getString(8), false);

			// Check if the user is on a new device. If so, update so we can send
			// notifications to it.
			if (user.getDeviceToken() != null && !user.getDeviceToken().equals(u.getDeviceToken())
					&& !user.getDeviceToken().isEmpty()) {
				user.setDeviceToken(u.getDeviceToken());
				save();
			}

			sendResponse(Commands.SUCCESS, user.toJson(mapper));


		} catch (JsonParseException e) {
			Global.log(clientNumber, "Recieved malformed data packet");
			error(Message.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			Global.log(clientNumber, "Bad sql " + e);
			error(Message.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			Global.log(clientNumber, "Something went wrong mapping user to object " + e);
			error(Message.SERVER_ERROR_RETRY);
		}

	}

	private void friendRequest(String data) throws JsonParseException, IOException, JSONException, SQLException {
		Global.log(clientNumber, "Sending friend Request");
		Global.log(clientNumber, data);

		if (user == null || user.getEmail().isEmpty()) {
			error(Message.NOT_LOGGEDIN);
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
			error(Message.MALFORMED_DATA_PACKET);
			return;
		}

		ResultSet resultSet = databaseConnection
				.executeSQL("select deviceToken from users where email='" + email + "';");

		if (!resultSet.next()) {
			sendResponse(Commands.ERROR, Message.USER_DOES_NOT_EXIST);
			return;
		}
		
		JSONObject notificationData = new JSONObject();
		notificationData.put(Commands.FRIEND_REQUEST, user.getEmail());

		NotificationHandler.sendPushNotification(resultSet.getString(1), Message.NEW_FRIEND_REQUEST_TITLE,
				user.getEmail() + Message.NEW_FRIEND_REQUEST_BODY, notificationData);
		sendResponse(Commands.SUCCESS, Message.FRIEND_REQUEST_SENT);
	}

	/**
	 * Save a new Humon and New instance at the same time. Reply to the client with
	 * the HumonId (hID)
	 * 
	 * @param data
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws SQLException
	 */
	private void createNewHumon(String data)
			throws JsonParseException, JsonMappingException, IOException, SQLException {

		if (user == null || user.getEmail().isEmpty()) {
			error(Message.NOT_LOGGEDIN);
			return;
		}
		Humon humon = mapper.readValue(data, Humon.class);

		// print it
		Global.log(clientNumber,
				user.getEmail() + " is creating a new Humon: " + humon.getName() + ", " + humon.getDescription());

		// Check to make sure it is a unique name / email / description.
		ResultSet resultSet = databaseConnection.executeSQL("select * from humon where created_by='"
				+ SQLHelper.sqlString(user.getEmail()) + "'" + " and name='" + SQLHelper.sqlString(humon.getName())
				+ "' and description='" + SQLHelper.sqlString(humon.getDescription()) + "';");
		if (resultSet.next()) {
			error(Message.DUPLICATE_HUMON);
			Global.log(clientNumber, "User attempted to create a duplicate humon");
			return;
		}

		int hID;

		// Insert into humon Table
		PreparedStatement ps = databaseConnection.prepareStatement(
				"insert into humon " + Global.HUMON_TABLE_COLUMNS + " values " + humon.toSqlHumonValueString(user));
		// Should only get 1 row was affected.
		int rows = ps.executeUpdate();
		if (rows != 1) {
			throw new SQLException();
		}

		// Get the HID to return to the user
		resultSet = databaseConnection
				.executeSQL("select humonID from humon where name='" + SQLHelper.sqlString(humon.getName())
						+ "' and description='" + SQLHelper.sqlString(humon.getDescription()) + "';");
		if (!resultSet.next()) {
			sendResponse(Commands.ERROR, Message.HUMON_CREATION_ERROR);
			return;
		}

		// Get the hID of the created humon, and send it as the response, as well as
		// updated hcount. User is also now dirty.
		hID = resultSet.getInt(1);

		sendResponse(Commands.SUCCESS, "{\"hID\":\"" + hID + "\",\"name\":\"" + humon.getName() + "\","
				+ "\"description\"" + humon.getDescription() + "\"}");

		// Insert image into image Table
		ps = databaseConnection.prepareStatement("insert into image " + Global.IMAGE_TABLE_COLUMNS + " values " + "('"
				+ hID + "','" + humon.getImage() + "')");

		// Should only get 1 row was affected.
		rows = ps.executeUpdate();
		if (rows != 1) {
			throw new SQLException();
		}

	}

	/**
	 * Sends error message to client.
	 */
	private void error(String msg) {
		clientOut.println(Commands.ERROR + ": " + msg);
		clientOut.flush();
		Global.log(clientNumber, "Client was sent error [" + Commands.ERROR + ":" + msg + "]");
	}

	private void sendResponse(String cmd, String msg) {
		clientOut.println(cmd + ": " + msg);
		clientOut.flush();
		Global.log(clientNumber, "Client was sent <cmd:msg> [" + cmd + ": " + msg + "]");
	}

}
