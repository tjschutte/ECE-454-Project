package main;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;


import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import models.UserHistory;
import server.ServerConnectionListener;
import utilities.Connector;

public class Main {

	private static volatile ArrayList<UserHistory> online;
	private static Connector databaseConnection;
	private static ServerConnectionListener serverConnectionListener;
	private static Scanner scanner;
	private static HelpFormatter formatter;
	private static Options options;

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {
		online = new ArrayList<UserHistory>();

		serverConnectionListener = new ServerConnectionListener(false, false, online);
		serverConnectionListener.start();

		// DB connection for checking users / commands that interact with DB
		databaseConnection = new Connector(Global.DATABASE_NAME, Global.TABLE_NAME, Global.DATABASE_USER_NAME,
				Global.DATABASE_USER_PASSWORD, Global.DEFAULT_CONNECTIONS);
		// Connect to the database and table
		databaseConnection.startConnection();
		
		// Wait to make sure everything is ready
		Thread.sleep(3000);

		String input = "";
		scanner = new Scanner(System.in);
		options = ConsoleCommands.getConsoleOptions();
		formatter = new HelpFormatter();

		while (true) {
			System.out.print(ConsoleCommands.SERVER_PROMPT);
			input = scanner.nextLine();
			// add on '-' if needed
			String[] inputArr = input.split(" ");

			if (inputArr.length == 0) {
				formatter.printHelp(" ", options);
				continue;
			}
			
			switch (inputArr[0]) {
				case ConsoleCommands.ONLINE:
					online();
					break;
				case ConsoleCommands.EXIT:
					exit();
					break;
				case ConsoleCommands.HELP:
					formatter.printHelp(" ", options);
					break;
				case ConsoleCommands.SEARCH:
					search(inputArr);
					break;
				case ConsoleCommands.GET:
					get(inputArr);
					break;
				case ConsoleCommands.RESET:
					reset(inputArr);
					break;
				default:
					formatter.printHelp(" ", options);
					break;
			}
		}
	}

	private static void reset(String... args) {
		// TODO: This method
	}

	private static void get(String... args) {
		if (args.length < 2) {
			formatter.printHelp(" ", options);
			return;
		}
		
		String user = args[1];
		
		UserHistory hist = null;
		for (UserHistory history : online) {
			if (history.who().getEmail().equals(user)) {
				hist = history;
				break;
			}
		}

		if (null == hist) {
			System.out.println(user + " is not currently online or does not exist.");
		} else {
			System.out.println("Session History for: " + user);
			System.out.println("Logged in since: " + hist.loggedInAt());
			System.out.print("Player actions:\n\t");
			for (String action : hist.getActionHistory()) {
				System.out.print(" | " + action);
			}
			System.out.print(" |\n");
		}
	}

	private static void search(String... args) throws SQLException {
		if (args.length < 2) {
			formatter.printHelp(" ", options);
			return;
		}
		
		String user = args[1];
		
		ResultSet resultSet = databaseConnection.executeSQL(
				"select email from users where email like '%" + user + "%';");
		
		if (!resultSet.next()) {
			System.out.println("Couldn't find any users matching: " + user);
		} else {
			System.out.println("Search results:");
			int found = 0;
			String userEmail = resultSet.getString(1);
			while (resultSet.next()) {
				found++;
				System.out.println("\t" + found + ": " + userEmail);
				userEmail = resultSet.getString(1);
			}
			found++;
			System.out.println("\t" + found + ": " + userEmail);
		}
	}

	private static void exit() {
		System.out.println("Exiting");
		scanner.close();
		System.exit(0);
	}

	private static void online() {
		if (online.size() != 0) {
			System.out.println("Number of players online: " + online.size());
			for (UserHistory userHistory : online) {
				System.out.println("User: " + userHistory.who().getEmail() + " | Logged in at: "
						+ userHistory.loggedInAt() + " | Last action at: " + userHistory.lastActionAt() + "");
			}
		} else {
			System.out.println("No users currently online.");
		}
	}
}