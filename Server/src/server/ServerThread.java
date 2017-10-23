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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import data.models.User;
import main.GlobalConstants;
import service.database.Connector;

public class ServerThread extends Thread {

	/**
	 * A private thread to handle requests on a particular socket.
	 * The client terminates the dialogue by sending a single line containing only a
	 * period.
	 */
	private Socket socket;
	private int clientNumber;
	private Connector databaseConnection;
	private ObjectMapper mapper;
	private BufferedReader clientIn;
	private PrintWriter clientOut;
	private User user;
	private long lastSave;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public ServerThread(Socket socket, int clientNumber) throws IOException {
		this.socket = socket;
		this.clientNumber = clientNumber;
		log("connected at " + socket);
		mapper = new ObjectMapper();
		databaseConnection = new Connector(GlobalConstants.DATABASE_NAME, GlobalConstants.TABLE_NAME, 
				GlobalConstants.DATABASE_USER_NAME, GlobalConstants.DATABASE_USER_PASSWORD, GlobalConstants.DEFAULT_CONNECTIONS);
		// Connect to the database and table
		databaseConnection.startConnection();
		// Decorate the streams so we can send characters and not just bytes. Ensure output is flushed
		// after every newline.
		clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		clientOut = new PrintWriter(socket.getOutputStream());
		lastSave = System.nanoTime();
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
				/* Save any dirty data every ~5 minutes, assuming user is active. Else data will be
				* saved on close. (This is blocked by below readLine)
				*/
				if (Math.abs((lastSave - System.nanoTime())) > GlobalConstants.UPDATE_TIME) {
					lastSave = System.nanoTime();
					save();
				}

				// Note: this line is blocking
				input = clientIn.readLine();
				if (input == null || input.length() == 0 || input.indexOf(':') == -1) {
					error("empty or unrecognized command was issued");
					continue;
				}

				command = input.substring(0, input.indexOf(':'));
				command = command.toUpperCase();
				data = input.substring(input.indexOf(':') + 1, input.length());

				log("Command: " + command);
				log("Data: " + data);

				switch (command) {
				case Commands.LOGIN:
					login(data);
					break;
				case Commands.REGISTER:
					register(data);
					break;
				default:
					error("empty or unrecognized command was issued");
					break;
				}

				if (command == null || command.equals(".")) {
					log("Saving any dirty data and disconnecting from server.");
					save();
					break;
				}
				
				clientOut.flush();
			}
		} catch (IOException e) {
			log("had error - " + e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				log("Couldn't close a socket, what's going on?");
			}
			log("closed");
		}
	}
	
	/**
	 * Push update data to the sever *IF* it has changed.
	 */
	private void save() {
		log("Save was issued");
		if (user != null && user.isDirty()) {
			log("User data was updated. Saving to database");
			sendResponse(Commands.SAVE_USER, Message.SERVER_SAVED_USER);
		}
	}

	private void register(String data) {
		log("Trying to register new user");
		log(data);
		
		try {
			// Attempt to map email and password to an object.
			User u = new User(mapper, data);
			
			ResultSet resultSet = databaseConnection.executeSQL("SELECT * from users where email='" + u.getEmail() + "';");

			if (resultSet.next()) {
				error("email already in use");
				log("User attempted to reregister email: " + u.getEmail());
				return;
			}

			// Unique email, create a new user
			User newUser = new User(mapper, u.getEmail(), u.getPassword(), null, null, null, 0, true);
			
			// Insert into the database.
			PreparedStatement ps = databaseConnection.prepareStatement("INSERT INTO USERS "
					+ GlobalConstants.USERS_TABLE_COLUMNS + " values "
					+ newUser.toSqlValueString());
			
			// Should only get 1 row was affected.
			int rows = ps.executeUpdate();
			if (rows == 1) {
				user = newUser;
			}
			else {
				throw new SQLException();
			}
			
			sendResponse(Commands.SUCCESS, "");

		} catch (JsonParseException e) {
			log("Recieved malformed data packet");
			error(Message.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			log("Bad sql " + e);
			error(Message.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			log("Something went wrong mapping user to object" + e);
			error(Message.SERVER_ERROR_RETRY);
		}
		
	}

	private void login(String data) {
		log("Trying to login with ");
		log(data);
		
		try {
			User u = new User(mapper, data);
			ResultSet resultSet = databaseConnection.executeSQL("SELECT * from users where email='" + u.getEmail()
					+ "' and password='" + u.getPassword() + "';");

			if (!resultSet.next()) {
				sendResponse(Commands.ERROR, Message.BAD_CREDENTIALS);
				log("Invalid login creditials for user: " + u.getEmail());
				log("Does user exist?");
				return;
			} else {
				// reset cursor position
				resultSet.beforeFirst();
			}

			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			
			String object = "";
			while (resultSet.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1)
						object += (", ");
					String columnValue = resultSet.getString(i);
					object += (rsmd.getColumnName(i) + " " + columnValue);
				}
				log("Found user");
				log(object);
			}
			
			sendResponse(Commands.SUCCESS, "");

		} catch (JsonParseException e) {
			log("Recieved malformed data packet");
			error(Message.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			log("Bad sql " + e);
			error(Message.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			log("Something went wrong mapping user to object " + e);
			error(Message.SERVER_ERROR_RETRY);
		}

	}
	
	/**
	 * Sends error message to client.
	 */
	private void error(String msg) {
		clientOut.println(Commands.ERROR + ": " + msg);
		clientOut.flush();
		log("Client was sent error [" + Commands.ERROR + ":" + msg + "]");
	}
	
	private void sendResponse(String cmd, String msg) {
		clientOut.println(cmd + ": " + msg);
		clientOut.flush();
		log("Client was sent <cmd:msg> [" + cmd + ": " + msg + "]");
	}

	/**
	 * Logs a simple message. In this case we just write the message to the server
	 * applications standard output.
	 */
	public void log(String message) {
		System.out.println("Client " + clientNumber + ": " + dateFormat.format(Calendar.getInstance().getTime()) + " " + message);
	}
}
