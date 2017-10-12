package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import data.models.*;
import service.database.Connector;

public class Server {

	public static void main(String[] args) throws JsonParseException, IOException {	
		// Mapper for converting from POJO to JSON.  This is passed to each instance so a global
		// is used to save on memory
		/*
		ObjectMapper mapper = new ObjectMapper();
		
		ArrayList<Integer> party = new ArrayList<Integer>();
		ArrayList<Integer> encounteredHumons = new ArrayList<Integer>();
		party.add(new Integer(1));
		party.add(new Integer(2));
		party.add(new Integer(3));
		
		encounteredHumons.add(new Integer(1));
		encounteredHumons.add(new Integer(2));
		encounteredHumons.add(new Integer(3));
		encounteredHumons.add(new Integer(4));
		encounteredHumons.add(new Integer(5));
		encounteredHumons.add(new Integer(6));

		User user = new User(mapper, "UserName", "userPasswordHash", party, encounteredHumons);	
		
		String userJSON = user.toJson();
		System.out.println("userToJSON: " + userJSON);
		
		User userFromJSON = new User(mapper, userJSON);
		
		System.out.println("userFromJSON: " + userFromJSON.toJson());
		*/
		/*
		Connector TableConnection = new Connector("jdbc:mysql://localhost:3306/", "humon-test", "ece454", "zYFqzVgW3t2Y", "5");
		// Connect to the database and table 
		TableConnection.startConnection();
		// Execute a query 
		String username = "Tom";
		String passwordhash = "tompasshash";
		ResultSet resultSet = TableConnection.executeSQL("SELECT userid, party from users where username='" + username + "' and passwordhash='" + passwordhash + "';");
		
		try {
			if (!resultSet.next()) {
				System.out.println("Bad login");
			} else {
				// reset cursor position
				resultSet.beforeFirst();
			}
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			
			while (resultSet.next()) {
			    for (int i = 1; i <= columnsNumber; i++) {
			        if (i > 1) System.out.print(", ");
			        String columnValue = resultSet.getString(i);
			        System.out.print(rsmd.getColumnName(i) + " " + columnValue);
			    }
			    System.out.println("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		*/
		System.out.println("The socket server is running.");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(9898);
        try {
            while (true) {
                new Echo(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }

	}
	
	/**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Echo extends Thread {
        private Socket socket;
        private int clientNumber;

        public Echo(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            log("New connection with client# " + clientNumber + " at " + socket);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line with only a period to quit\n");

                // Get messages from the client, line by line; return them
                // capitalized
                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals(".")) {
                        break;
                    }
                    // Echo input back to user
                    out.println("You said:" + input);
                }
            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }

}
