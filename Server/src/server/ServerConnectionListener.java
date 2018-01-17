package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.ArrayList;

import main.Global;
import models.UserHistory;
import utilities.Database;

/**
 * Humon server.  Listens on the Humon Port and acts according to device
 * calls to the server.
 *
 */
public class ServerConnectionListener extends Thread {
	
	private boolean dropTables;
	private boolean testData;
	public volatile ArrayList<UserHistory> online;
	
	public ServerConnectionListener(boolean dropTables, boolean testData, ArrayList<UserHistory> online) {
		this.dropTables = dropTables;
		this.testData = testData;
		this.online = online;
	}
	
	public void run() {
		ServerSocket humonListener = null;
		try {
			System.out.println("Starting Humon Server...");
			int clientNumber = 0;
			humonListener = new ServerSocket(Global.HUMON_SERVER_PORT);

			// Ensure that the database is set up, and has tables set up.
			@SuppressWarnings("unused")
			Database database = new Database(dropTables, testData);

			System.out.println("Waiting for Humon-Service connections.");
			while (true) {
				new ServerConnection(humonListener.accept(), clientNumber++, online).start();
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