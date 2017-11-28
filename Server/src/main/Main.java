package main;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import http.HttpConnectionListener;
import server.ServerConnectionListener;
import utilities.JTextAreaOutputStream;

public class Main {

	public static void main(String[] args) throws IOException, SQLException {
		Options options = new Options();

		options.addOption("drop", false, "Drop any preexisting tables in the databases.");
		options.addOption("test", false, "Attempt to load test data into the databases.");
		options.addOption("cmd", false, "Run in command line instead of openning a GUI.");
		
		// create the parser
	    CommandLineParser parser = new DefaultParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        boolean dropTables;
	        boolean testData;
	        
	        // If they ask for gui, give it to them.
	        if (!line.hasOption("cmd")) {
	        	JFrame frame = new JFrame("Humon Server");
				JTextArea messageArea = new JTextArea(20, 60);
				// Layout GUI
				messageArea.setEditable(false);
				frame.getContentPane().add(new JScrollPane(messageArea), "Center");
				
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
				
				JTextAreaOutputStream out = new JTextAreaOutputStream (messageArea);
		        System.setOut (new PrintStream (out));
	        }
	        
	        dropTables = line.hasOption("drop");
	        testData = line.hasOption("test");
	        
			
			new ServerConnectionListener(dropTables, testData).start();
			new HttpConnectionListener().start();
	        
	        
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
	}
}






