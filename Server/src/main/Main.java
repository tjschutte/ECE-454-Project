package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

import server.Server;


public class Main {

	public static void main(String[] args) throws IOException, SQLException {		
		System.out.println("Starting Server...");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(9898);
        try {
        	System.out.println("Waiting for connections.");
            while (true) {
                new Server(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
        
	}

}
