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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import data.models.User;
import service.database.Connector;

public class Server extends Thread {

	/**
	 * A private thread to handle capitalization requests on a particular socket.
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

	public Server(Socket socket, int clientNumber) throws IOException {
		this.socket = socket;
		this.clientNumber = clientNumber;
		log(" connected at " + socket);
		mapper = new ObjectMapper();
		databaseConnection = new Connector("jdbc:mysql://localhost:3306", "humon-test", "ece454", "zYFqzVgW3t2Y", "5");
		// Connect to the database and table
		databaseConnection.startConnection();
		// Decorate the streams so we can send characters and not just bytes. Ensure output is flushed
		// after every newline.
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

			while (true) {

				String input = clientIn.readLine();
				if (input == null || input.length() == 0 || input.indexOf(':') == -1) {
					error("empty or unrecognized command was issued");
					continue;
				}

				String command = input.substring(0, input.indexOf(':'));
				command = command.toUpperCase();
				String data = input.substring(input.indexOf(':') + 1, input.length());

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
					log("Disconnecting from server.");
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
			User newUser = new User(mapper, u.getEmail(), u.getPassword(), null, null, 0);
			
			// Insert into the database.
			PreparedStatement ps = databaseConnection.prepareStatement("INSERT INTO USERS "
					+ "(email, password, party, encountered_humons, hcount) values "
					+ "(" + newUser.toSqlValueString() + ")");
			
			// Should only get 1 row was affected.
			int rows = ps.executeUpdate();
			if (rows == 1) {
				user = newUser;
			}
			else {
				throw new SQLException();
			}
			
			sendResponse(Commands.SUCCESS);

		} catch (JsonParseException e) {
			log("Recieved malformed data packet");
			error(Errors.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			log("Bad sql " + e);
			error(Errors.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			log("Something went wrong mapping user to object" + e);
			error(Errors.SERVER_ERROR_RETRY);
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
				sendResponse(Commands.ERROR + Errors.BAD_CREDENTIALS);
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
				log("Found in user");
				log(object);
			}
			
			sendResponse(Commands.SUCCESS);

		} catch (JsonParseException e) {
			log("Recieved malformed data packet");
			error(Errors.MALFORMED_DATA_PACKET);
		} catch (SQLException e) {
			log("Bad sql " + e);
			error(Errors.SERVER_ERROR_RETRY);
		} catch (IOException e) {
			log("Something went wrong mapping user to object" + e);
			error(Errors.SERVER_ERROR_RETRY);
		}

	}
	
	/**
	 * Sends error message to client.
	 */
	private void error(String msg) {
		clientOut.println(Commands.ERROR + msg);
		clientOut.flush();
	}
	
	private void sendResponse(String msg) {
		clientOut.println(msg);
		clientOut.flush();
	}

	/**
	 * Logs a simple message. In this case we just write the message to the server
	 * applications standard output.
	 */
	public void log(String message) {
		System.out.println(clientNumber + ": " + message);
	}
}
