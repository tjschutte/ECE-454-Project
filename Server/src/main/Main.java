package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

import server.HttpConnection;
import server.ServerThread;
import utilities.Database;

public class Main {

	public static void main(String[] args) throws IOException, SQLException {
		new ServerConnectionListener().start();
		new HttpConnectionListener().start();
	}
}

class ServerConnectionListener extends Thread {
	
	public ServerConnectionListener() {}
	
	public void run() {
		ServerSocket humonListener = null;
		try {
			System.out.println("Starting Humon Server...");
			int clientNumber = 0;
			humonListener = new ServerSocket(9898);

			// Ensure that the database is set up, and has tables set up.
			Database database = new Database(false, false);

			System.out.println("Waiting for Humon-Service connections.");
			while (true) {
				new ServerThread(humonListener.accept(), clientNumber++).start();
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				humonListener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

/**
 * This is so the server can handle http request later on.  This will allow us to distribute the app to 
 * friends by having them visit in a webrowser to download the compiled app.
 * 
 */
class HttpConnectionListener extends Thread {
	public void run() {
		ServerSocket httpListener = null;
		try {
			httpListener = new ServerSocket(80);

			System.out.println("Waiting for Http connections.");
			while (true) {
				new HttpConnection(httpListener.accept()).start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpListener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
