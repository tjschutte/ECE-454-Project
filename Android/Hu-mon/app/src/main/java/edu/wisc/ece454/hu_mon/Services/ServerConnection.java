package edu.wisc.ece454.hu_mon.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ServerConnection extends Service {
    public static final String SERVERIP = "68.185.171.192";
    public static final int SERVERPORT = 9898;
    PrintWriter out;
    BufferedReader in;
    Socket socket;
    InetAddress serverAddr;
    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ServerConnection getService() {
            return ServerConnection.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return myBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void IsBoundable() {
        // TODO: Does this need to be implemented?
    }

    /**
     * Creates a thread to send a message to the server in the background.
     * @param message
     */
    public void sendMessage(String message) {
        Runnable sendSocket = new sendSocket(message);
        new Thread(sendSocket).start();
    }

    /**
     * Message sending thread
     */
    class sendSocket implements Runnable {
        private String msg;

        public sendSocket(String msg){
            this.msg = msg;
        }

        @Override
        public void run() {
            try {
                System.out.println("Attempting to send: " + msg);
                if (out != null && !out.checkError()) {
                    System.out.println("Sending...");
                    out.println(msg);
                    out.flush();
                } else {
                    System.out.println("Out was null, or had an error");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Runnable connect = new connectSocket();
        new Thread(connect).start();
        return START_NOT_STICKY;
    }

    /**
     * Sets up the socket, then begins to listen for responces from the server
     */
    class connectSocket implements Runnable {

        @Override
        public void run() {
            try {
                //here you must put your computer's IP address.
                serverAddr = InetAddress.getByName(SERVERIP);
                //create a socket to make the connection with the server
                socket = new Socket(serverAddr, SERVERPORT);

                System.out.println("Setting up socket");
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                System.out.println("Waiting for responses");
                while (true) {
                    String res = in.readLine();
                    System.out.println("Server response: " + res);

                    if (res != null && !res.isEmpty()) {
                        Intent intent = new Intent();
                        intent.setAction("edu.wisc.ece454.hu_mon.SERVER_RESPONSE");
                        intent.putExtra("Response",res);
                        sendBroadcast(intent);
                    }

                }

            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket = null;
    }

}