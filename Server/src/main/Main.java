package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;

import data.models.Humon;
import data.models.User;
import server.ServerThread;
import service.database.Database;


public class Main {

	public static void main(String[] args) throws IOException, SQLException {		
		
		System.out.println("Starting Server...");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(9898);
        
        // Ensure that the database is set up, and has tables set up.
        Database database = new Database(true, false);
        
        try {
        	System.out.println("Waiting for connections.");
            while (true) {
                new ServerThread(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
        
	}

}
