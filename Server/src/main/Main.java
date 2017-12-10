package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import http.HttpConnectionListener;
import server.ServerConnectionListener;
import utilities.JTextAreaOutputStream;

public class Main {

	public static void main(String[] args) throws IOException, SQLException  {
		JFrame frame = new JFrame("Humon Server");
		frame.setSize(50, 80);
		final JTextArea messageArea = new JTextArea(30, 80);
		// Layout GUI
		messageArea.setEditable(false);
		frame.getContentPane().add(new JScrollPane(messageArea), "North");

		JPanel config = new JPanel();

		config.setSize(20, 80);
		
		final JCheckBox dropTablesCheckBox = new JCheckBox("Drop existing database tables?");
		final JCheckBox testDataCheckBox = new JCheckBox("Insert test data into database tables?");
		final JCheckBox appendToLogCheckBox = new JCheckBox("Append to exisitng log file if exists?");
		final JCheckBox startHttpServer = new JCheckBox("Start Http server as well?");
		startHttpServer.setSelected(true);
		final JButton start = new JButton("Start");
		config.add(dropTablesCheckBox);
		config.add(testDataCheckBox);
		config.add(appendToLogCheckBox);
		config.add(startHttpServer);
		config.add(start);
		
		frame.getContentPane().add(config, "Center");
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean dropTables;
				boolean testData;
				boolean appendToLog;
				
				dropTables = dropTablesCheckBox.isSelected();
				testData = testDataCheckBox.isSelected();
				appendToLog = appendToLogCheckBox.isSelected();
				start.setEnabled(false);
				
				JTextAreaOutputStream out = new JTextAreaOutputStream(messageArea, appendToLog);
				System.setOut(new PrintStream(out));
				System.setErr(new PrintStream(out));

				new ServerConnectionListener(dropTables, testData).start();
				
				
				if (startHttpServer.isSelected()) {
					new HttpConnectionListener().start();
				}
			}
		});
		
	}
}






