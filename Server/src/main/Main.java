package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import models.UserHistory;
import server.ServerConnectionListener;

public class Main {
	
	public static volatile ArrayList<UserHistory> online;

	public static void main(String[] args) throws IOException, SQLException  {
		online = new ArrayList<UserHistory>();
		
		new ServerConnectionListener(false, false, online).start();
		
		/**
		 * Some sort of loop here to check who is logged in / server stats etc.
		 */
		String input = "";
		Scanner scanner = new Scanner(System.in);
		while (!input.equals("end")) {
			input = scanner.nextLine();
			
			if (input.equals("online")) {
				if (online.size() != 0) {
					System.out.println("Number of players online: " + online.size());
					for (UserHistory userHistory : online) {
						System.out.println("User: " + userHistory.who().getEmail() + " | Logged in at: " + userHistory.loggedInAt() + " | Last action at: " + userHistory.lastActionAt() + "");
					}
				} else {
					System.out.println("No users currently online.");
				}
			}
				
			
		}
	}
}