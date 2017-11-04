package server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

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
					} else if (Headers[1].contains("get-image")) {
						// Send them a page with the requested image
						getImage(Headers[1]);
					} else {
						// Do nothing, probably not the header we want.
					}
				}
			}
			
			
			in.close();
			socket.close();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	private void homePage() throws IOException {
		PrintWriter out;
		out = new PrintWriter(socket.getOutputStream());
		System.out.println("Connection to Homepage.");
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
		System.out.println("Connection to Download");
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

	private void getImage(String connectionString) throws IOException, SQLException {
		System.out.println("Connection to Images");
		System.out.println(connectionString);
		OutputStream out;
		
		try {
			int imageNum = Integer.parseInt(connectionString.split("/")[2]);
			
			ResultSet resultSet = databaseConnection
					.executeSQL("select * from image where imageID='" + imageNum + "';");
			
			if (!resultSet.next()) {
				System.out.println("Connection to Homepage.");
				PrintWriter outPW = new PrintWriter(socket.getOutputStream());
				outPW.print("HTTP/1.1 200 \r\n"); // Version & status code
				outPW.print("Content-Type: text/html\r\n"); // The type of data
				outPW.print("Connection: close\r\n"); // Will close stream
				outPW.print("\r\n"); // End of headers

				outPW.println("<html><body>");
				outPW.println("<p>Could not find image: " + imageNum + "</p>");
				outPW.println("</body></html>");
				outPW.close();
				return;
			} 
			out = socket.getOutputStream();
			
			String image = resultSet.getString(2);
			
			byte[] bytes = Base64.decodeBase64(image);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        ImageIO.write(img, "png", byteArrayOutputStream);
	        
	        out.write(byteArrayOutputStream.toByteArray());
	        out.flush();
	        out.close();
			
		} catch (ArrayIndexOutOfBoundsException e) {
			
		}
	}

}
