package edu.wisc.ece454.hu_mon.Services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.Models.Jsonable;
import edu.wisc.ece454.hu_mon.R;

public class ServerConnection extends Service {
    private final String TAG = "SERVCONN";
    public static final String SERVERIP = "68.185.171.192";
    public static final int SERVERPORT = 9898;
    PrintWriter out;
    BufferedReader in;
    Socket socket;
    InetAddress serverAddr;
    private final IBinder myBinder = new LocalBinder();
    boolean running = false;

    public class LocalBinder extends Binder {
        public ServerConnection getService() {
            return ServerConnection.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Creates a thread to send a message to the server in the background.
     * @param message
     */
    public void sendMessage(String message) {
        Runnable sendSocket = new sendSocket(message, null);
        new Thread(sendSocket).start();
    }

    /**
     * Creates a thread to convert object to JSON representation and send in a message to the
     * server in the background.
     * @param message - the message to send the server, should be a command.
     * @param object - the JsonObject to convert to JSON and send as the data portion of the message.
     */
    public void sendMessage(String message, Jsonable object) {
        Runnable sendSocket = new sendSocket(message, object);
        new Thread(sendSocket).start();
    }

    /**
     * Message sending thread
     */
    class sendSocket implements Runnable {
        private String msg;
        private Jsonable obj;

        public sendSocket(String msg, Jsonable obj){
            this.msg = msg;
            this.obj = obj;
        }

        @Override
        public void run() {
            try {
                if (obj == null) {
                    Log.d(TAG, "Attempting to send: " + msg);
                    if (out != null && !out.checkError()) {
                        Log.d(TAG, "Sending...");
                        out.println(msg);
                        out.flush();
                    } else {
                        Log.d(TAG, "Out was null, or had an error");
                    }
                } else {
                    String data = obj.toJson(new ObjectMapper());
                    if(msg.length() < 100) {
                        Log.d(TAG, "Attempting to send: " + msg + ": " + data);
                    }
                    if (out != null && !out.checkError()) {
                        Log.d(TAG, "Sending...");
                        out.println( msg + ": " + data);
                        out.flush();
                    } else {
                        Log.d(TAG, "Out was null, or had an error");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            super.onStartCommand(intent, flags, startId);
            Runnable connect = new connectSocket();
            new Thread(connect).start();
            running = true;
        }
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

                Log.d(TAG, "Setting up socket");
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "Waiting for responses");
                while (true) {
                    String res = in.readLine();
                    Log.d(TAG, "Server response: " + res);

                    //Rewrite image since too large for parcel
                    if (res != null && res.indexOf(':') != -1) {
                        String command = res.substring(0, res.indexOf(':'));
                        String data = res.substring(res.indexOf(':') + 1, res.length());

                        //write image
                        if(command.toUpperCase().equals(getString(R.string.ServerCommandGetHumon))) {
                            Log.d(TAG, "GET-HUMON response, saving image");
                            ObjectMapper mapper = new ObjectMapper();
                            //create Humon object from payload
                            Humon indexHumon = mapper.readValue(data, Humon.class);

                            //tell Humon where to expect image file
                            File imageFile = new File(getFilesDir(), indexHumon.gethID() + ".jpg");
                            indexHumon.setImagePath(imageFile.getPath());

                            //Create bitmap of image to be stored
                            byte[] imageAsBytes = Base64.decode(indexHumon.getImage().getBytes(), Base64.DEFAULT);
                            Bitmap humonImage = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                            //save image to file
                            FileOutputStream outputStream = new FileOutputStream(imageFile);
                            humonImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.close();

                            //remove image from Humon object
                            indexHumon.setImage(null);

                            //fix response
                            res = command + ": " + indexHumon.toJson(mapper);
                            Log.d(TAG, "New response: " + res);
                        }
                    }

                    if (res != null && !res.isEmpty()) {
                        Intent intent = new Intent();
                        intent.setAction(getString(R.string.serverBroadCastEvent));
                        intent.putExtra(getString(R.string.serverBroadCastResponseKey),res);
                        sendBroadcast(intent);
                    } else if (res == null) {
                        // If we null back, the server was unreachable.
                        // TODO: Schedule a retry to connect to the server.
                        Intent intent = new Intent();
                        intent.setAction(getString(R.string.serverBroadCastEvent));
                        intent.putExtra(getString(R.string.serverBroadCastResponseKey), "Unable to reach Server. Try again later.");
                        sendBroadcast(intent);
                        return;
                    }

                }

            } catch (IOException e) {
                Log.d(TAG, e.toString());
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