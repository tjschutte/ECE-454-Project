package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import main.Global;
import utilities.Connector;

public class HttpConnection extends Thread {

	private Socket socket;
	private Connector databaseConnection;
	private BufferedReader in;

	public HttpConnection(Socket socket) throws IOException {
		this.socket = socket;

		databaseConnection = new Connector(Global.DATABASE_NAME, Global.TABLE_NAME, Global.DATABASE_USER_NAME,
				Global.DATABASE_USER_PASSWORD, Global.DEFAULT_CONNECTIONS);
		// Connect to the database and table
		databaseConnection.startConnection();

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public void run() {
		try {

			String line;
			while ((line = in.readLine()) != null) {
				if (line.length() == 0)
					break;
				String[] Headers = line.split(" ");
				if (Headers.length > 2) {
					if (Headers[1].equals("/")) {
						// Send them the 'home page'
						homePage();
					} else if (Headers[1].equalsIgnoreCase("/download")) {
						// Send them the app to install
						download();
					} else if (Headers[1].equalsIgnoreCase("/log")){
						log();
					}
				}
			}
			
			
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void homePage() throws IOException {
		PrintWriter out;
		out = new PrintWriter(socket.getOutputStream());
		out.print("HTTP/1.1 200 \r\n"); // Version & status code
		out.print("Content-Type: text/html\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers

		out.println("<html><body>");
		out.println("<h1>Humon Android App!</h1>");
		out.println("<p>To download the app go <a href='/download'>here</a> </p>");
		out.println("</body></html>");
		out.close();
	}

	private void download() throws IOException {
		PrintWriter out;
		out = new PrintWriter(socket.getOutputStream());
		out.print("HTTP/1.1 200 \r\n"); // Version & status code
		out.print("Content-Type: text/html\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers

		out.println("<html><body>");
		out.println("<h1>Downloads</h1>");
		out.println("<p>Not available yet.</p>");
		out.println("</body></html>");
		out.close();
		
	}
	
	private void log() throws IOException {
		PrintWriter out;
		
		out = new PrintWriter(socket.getOutputStream());
		out.print("HTTP/1.1 200 \r\n"); // Version & status code
		out.print("Content-Type: text/plain\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers

		BufferedReader br = new BufferedReader(new FileReader(new File("nohup.out")));
		String string = br.readLine();
		while (string != null) {
			out.println(string);
			string = br.readLine();
		}
		
		br.close();
		out.close();
	}

}
