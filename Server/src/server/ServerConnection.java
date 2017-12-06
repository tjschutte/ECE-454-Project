package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;

import org.json.JSONException;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.Global;
import models.User;
import utilities.Connector;

public class ServerConnection extends Thread {

	/**
	 * A private thread to handle requests on a particular socket. The client
	 * terminates the dialogue by sending a single line containing only a
	 * period.
	 */
	public Socket socket;
	public int clientNumber;
	public Connector databaseConnection;
	public ObjectMapper mapper;
	public BufferedReader clientIn;
	public PrintWriter clientOut;
	public User user;

	public ServerConnection(Socket socket, int clientNumber) throws IOException {
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
					UserAction.save(this);
					break;
				} else if (input.indexOf(':') == -1) {
					if (input.toUpperCase().equals(Command.LOGOUT)) {
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
					// Register a new account
					case Command.REGISTER:
						UserAction.register(this, data);
						break;
					// Login to existing account
					case Command.LOGIN:
						UserAction.login(this, data);
						break;
					// Send a friend request
					case Command.FRIEND_REQUEST:
						UserAction.friendRequest(this, data);
						break;
					case Command.FRIEND_ADDED:
						UserAction.friendAdded(this, data);
						break;
					// Send a battle request
					case Command.BATTLE_REQUEST:
						UserAction.battleRequest(this, data);
						break;
					// Accept a battle request from another user
					case Command.BATTLE_ACCEPTED:
						UserAction.battleAccepted(this, data);
						break;
					// Get the opposing players party (unique IDs, which can then be used to get humons)
					case Command.GET_PARTY:
						UserAction.getParty(this, data);
						break;
					// Save the user data back to the database
					case Command.SAVE_USER:
						UserAction.saveAccount(this, data);
						break;
					// Create a new Humon-Type
					case Command.CREATE_HUMON:
						HumonAction.createNewHumon(this, data);
						break;
					// Get a existing Humon-Type
					case Command.GET_HUMON:
						HumonAction.getHumon(this, data);
						break;
					// Save a new / update and existing Humon-Instance
					case Command.SAVE_INSTANCE:
						HumonAction.saveInstance(this, data);
						break;
					// Get an existing Humon-instance
					case Command.GET_INSTANCE:
						HumonAction.getInstance(this, data);
						break;
					case Command.GET_IMAGE:
						HumonAction.getImage(this, data);
						break;
					// They sent baaaad data
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
	 * Sends error message to client.
	 */
	public void error(String msg) {
		clientOut.println(Command.ERROR + ": " + msg);
		clientOut.flush();
		Global.log(clientNumber, "Client was sent error [" + Command.ERROR + ":" + msg + "]");
	}

	public void sendResponse(String cmd, String msg) {
		clientOut.println(cmd + ": " + msg);
		clientOut.flush();
		Global.log(clientNumber, "Client was sent <cmd:msg> [" + cmd + ": " + msg + "]");
	}

}
