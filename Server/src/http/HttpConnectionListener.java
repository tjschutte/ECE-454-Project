package http;

import java.io.IOException;
import java.net.ServerSocket;

import main.Global;

/**
 * This is so the server can handle http request later on.  This will allow us to distribute the app to 
 * friends by having them visit in a webrowser to download the compiled app.
 * 
 */
public class HttpConnectionListener extends Thread {
	public void run() {
		ServerSocket httpListener = null;
		try {
			httpListener = new ServerSocket(Global.HUMON_HTTP_PORT);

			System.out.println("Waiting for Http connections.");
			while (true) {
				new HttpConnection(httpListener.accept()).start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpListener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}