package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
					} else if (Headers[1].toLowerCase().contains("/download-app")) {
						// Send them the app to install
						downloadApp();
					} else if (Headers[1].toLowerCase().contains("/download")) {
						// Send them the app to install
						download();
					} else if (Headers[1].toLowerCase().contains("/log")){
						log();
					} else if (Headers[1].toLowerCase().contains("/search-log")){
						searchLog(Headers[1]);
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
		
		BufferedReader br = new BufferedReader(new FileReader(new File("res/humon.html")));
		String string = br.readLine();
		while (string != null) {
			out.println(string);
			string = br.readLine();
		}
			
		br.close();
		out.close();
	}

	private void download() throws IOException {
		PrintWriter out;
		out = new PrintWriter(socket.getOutputStream());
		out.print("HTTP/1.1 200 \r\n"); // Version & status code
		out.print("Content-Type: text/html\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers
		
		BufferedReader br = new BufferedReader(new FileReader(new File("res/download.html")));
		String string = br.readLine();
		while (string != null) {
			out.println(string);
			string = br.readLine();
		}
			
		br.close();
		out.close();
		
	}
	
	private void downloadApp() throws IOException {
		PrintWriter pw;
		pw = new PrintWriter(socket.getOutputStream());
		pw.print("HTTP/1.1 200 \r\n"); // Version & status code
		pw.print("location: Humon.apk\r\n");
		pw.print("Content-Type: application/download\r\n"); // The type of data 'Content-Disposition: attachment; filename="
		//pw.print("Connection: close\r\n"); // Will close stream
		pw.print("\r\n"); // End of headers
		
		
		FileOutputStream out;
		out = (FileOutputStream)socket.getOutputStream();
		File apk = new File("app-debug.apk");
		FileInputStream fis = new FileInputStream(apk);
		byte[] b = new byte[(int)apk.length()];
		int read;
		read = fis.read(b);
		while (read > 0) {
			out.write(b);
			read = fis.read(b);
		}
		out.close();
		fis.close();
		pw.close();
	}
	
	private void log() throws IOException {
		PrintWriter out;
		
		out = new PrintWriter(socket.getOutputStream());
		out.print("HTTP/1.1 200 \r\n"); // Version & status code
		out.print("Content-Type: text/html\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers

		BufferedReader br = new BufferedReader(new FileReader(new File("log.txt")));
		String string = br.readLine();
		out.println("<HTML><meta http-equiv=\"refresh\" content=\"3\">"
				+ "<script>" + 
				"function scrollDown() {" + 
				"   window.scrollTo(0, document.body.scrollHeight);" + 
				"} " +
				"//setInterval(scrollDown, 500);" +
				"</script>"
				+ "");
		while (string != null) {
			out.println(string);
			out.println("</br>");
			string = br.readLine();
		}
		
		out.println("<body onload=\"scrollDown()\"></body></HTML>");
		
		br.close();
		out.close();
	}
	
	private void searchLog(String url) throws IOException {
		String param = "";
		if (url.contains("?")) {
			param = java.net.URLDecoder.decode(url.substring(url.indexOf("?") + 1, url.length()), "UTF-8");
		}
		PrintWriter out;
		out = new PrintWriter(socket.getOutputStream());
		out.print("HTTP/1.1 200 \r\n"); // Version & status code
		out.print("Content-Type: text/html\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers

		out.println("<html><body>");
		out.println("<h1>Search the log file</h1>");
		out.println("<p> Search for: <input type='text' id='search' value='" + param + "'/> <button onclick='search()'>Search</button> </p>");
		out.println("<h3>Results:</h3>");
		out.println("</body>");
		
		out.println("<script>" + 
				"function search() {" + 
				"   var param = document.getElementById('search').value;" + 
				"	console.log(param);" +
				"	window.location = '/search-log?' + param;" +
				"} " +
				"</script></html>"
				+ "");
		
		BufferedReader br = new BufferedReader(new FileReader(new File("log.txt")));
		String string = br.readLine();
		while (string != null) {
			if (string.contains(param)) {
				out.println(string);
				out.println("</br>");
			}
			string = br.readLine();
		}
		
		br.close();
		out.close();
	}

}
