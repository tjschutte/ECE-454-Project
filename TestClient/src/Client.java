import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the server. It has a main frame window with a
 * text field for entering strings and a textarea to see the results from the
 * server
 */
public class Client {

	private BufferedReader in;
	private PrintWriter out;
	private JFrame frame = new JFrame("Client");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 60);
	private boolean getReply = false;

	/**
	 * Constructs the client by laying out the GUI and registering a listener with
	 * the textfield so that pressing Enter in the listener sends the textfield
	 * contents to the server.
	 */
	public Client() {

		// Layout GUI
		messageArea.setEditable(false);
		frame.getContentPane().add(dataField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");

		// Add Listeners
		dataField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending the contents
			 * of the text field to the server and displaying the response from the server
			 * in the text area. If the response is "." we exit the whole application, which
			 * closes all sockets, streams and windows.
			 */
			public void actionPerformed(ActionEvent e) {
				out.println(dataField.getText());
				String response;
					try {
						response = in.readLine();
					} catch (IOException ex) {
						response = "Error: " + ex;
					}
					if (response != null) {
						messageArea.append(response + "\n");
						System.out.println(response);
					}
					dataField.selectAll();
				}
		});
	}

	/**
	 * Implements the connection logic by prompting the end user for the server's IP
	 * address, connecting, setting up streams, and consuming the welcome messages
	 * from the server. The protocol says that the server sends three lines of text
	 * to the client immediately after establishing a connection.
	 */
	public void connectToServer() throws IOException {

		// Get the server address from a dialog box.
		String serverAddress = JOptionPane.showInputDialog(frame, "Enter IP Address of the Server:",
				"Welcome to the Simple Client Program", JOptionPane.QUESTION_MESSAGE);

		// Make connection and initialize streams
		Socket socket = new Socket(serverAddress, 9898);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	/**
	 * Runs the client application.
	 */
	public static void main(String[] args) throws Exception {
		Client client = new Client();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.pack();
		client.frame.setVisible(true);
		client.connectToServer();
	}
}