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
		JTextArea messageArea = new JTextArea(20, 60);
		// Layout GUI
		messageArea.setEditable(false);
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		
		JTextAreaOutputStream out = new JTextAreaOutputStream(messageArea);
		System.setOut(new PrintStream(out));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		
		JPanel config = new JPanel();

		config.setSize(40, 80);
		
		final JCheckBox dropTablesCheckBox = new JCheckBox("Drop existing database tables?");
		final JCheckBox testDataCheckBox = new JCheckBox("Insert test data into database tables?");
		final JButton start = new JButton("Start");
		config.add(dropTablesCheckBox);
		config.add(testDataCheckBox);
		config.add(start);
		
		frame.getContentPane().add(config, "South");
		frame.setVisible(true);
		
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean dropTables;
				boolean testData;
				
				dropTables = dropTablesCheckBox.isSelected();
				testData = testDataCheckBox.isSelected();
				start.setEnabled(false);

				new ServerConnectionListener(dropTables, testData).start();
				new HttpConnectionListener().start();
				
			}
		});
		
	}
}






