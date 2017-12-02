package edu.wisc.ece454.hu_mon.Services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import edu.wisc.ece454.hu_mon.R;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ServerSaveService extends JobService {

    public static final String SERVERIP = "68.185.171.192";
    public static final int SERVERPORT = 9898;
    PrintWriter out;
    BufferedReader in;
    Socket socket;
    InetAddress serverAddr;
    private boolean socketConnected;

    private String[] humons;
    private String user;
    private Thread[] messThreads;
    private boolean threadsStarted = false;
    private int numThreadsStarted = 0;
    private int numResponses = 0;
    private int numAttempts = 0;
    private JobParameters jobParams;

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParams = params;

        Thread saveThread = new Thread() {
            public void run() {
                System.out.println("Server Save Service started");

                socketConnected = false;
                Runnable connect = new connectSocket();
                new Thread(connect).start();

                Timer timer = new Timer();
                TimerTask restartAttempts = new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Server save service timed out");
                        numAttempts++;
                    }
                };

                //wait for socket to connect before sending
                while(!socketConnected);

                //unpackage json strings to be sent
                if(!threadsStarted) {
                    if(numThreadsStarted == 0) {
                        int threadCount = 0;
                        if (jobParams.getExtras().containsKey(getString(R.string.humonsKey))) {
                            humons = jobParams.getExtras().getStringArray(getString(R.string.humonsKey));
                            if (humons != null) {
                                threadCount += humons.length;
                            }
                        }
                        if (jobParams.getExtras().containsKey(getString(R.string.userKey))) {
                            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPreferencesFile),
                                    Context.MODE_PRIVATE);
                            user = jobParams.getExtras().getStringArray(getString(R.string.userKey))[0];
                            threadCount += 1;
                        }

                        messThreads = new Thread[threadCount];
                    }

                    //send all humons to server
                    for (/* Nothing */; jobParams.getExtras().containsKey(getString(R.string.humonsKey)) && humons != null
                            && numThreadsStarted < humons.length; numThreadsStarted++) {

                        Runnable sendSocket = new sendSocket(getString(R.string.ServerCommandSaveInstance),
                                humons[numThreadsStarted]);

                        messThreads[numThreadsStarted] = new Thread(sendSocket);
                        messThreads[numThreadsStarted].start();
                    }

                    // Send the user back to the server on the alst available thread
                    if (jobParams.getExtras().containsKey(getString(R.string.userKey))) {
                        Runnable sendSocket = new sendSocket(getString(R.string.ServerCommandSaveUser), user);

                        messThreads[numThreadsStarted] = new Thread(sendSocket);
                        messThreads[numThreadsStarted].start();
                        numThreadsStarted++;
                    }

                    threadsStarted = true;
                }

                //wait for messages to be sent
                int oldAttempts = numAttempts;
                timer.schedule(restartAttempts, 10000);
                while(numResponses < numThreadsStarted && oldAttempts == numAttempts);
                timer.cancel();

                if(oldAttempts > numAttempts) {
                    System.out.println("Failed to save to server!");
                    Toast toast = Toast.makeText(getApplicationContext(), "Failed to save to server!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    System.out.println("All messages sent");
                }

                //Close the connection
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                socket = null;
                jobFinished(jobParams, false);
            }
        };

        saveThread.run();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        System.out.println("Stop job called");
        return true;
    }

    /**
     * Message sending thread
     */
    class sendSocket implements Runnable {
        private String msg;
        private String obj;

        public sendSocket(String msg, String obj){
            this.msg = msg;
            this.obj = obj;
        }

        @Override
        public void run() {
            try {
                if (obj == null) {
                    System.out.println("Attempting to send: " + msg);
                    if (out != null && !out.checkError()) {
                        System.out.println("Sending...");
                        out.println(msg);
                        out.flush();
                    } else {
                        System.out.println("Out was null, or had an error");
                    }
                } else {
                    String data = obj;
                    if(msg.length() < 100) {
                        System.out.println("Attempting to send: " + msg + ": " + data);
                    }
                    if (out != null && !out.checkError()) {
                        System.out.println("Sending...");
                        out.println( msg + ": " + data);
                        out.flush();
                    } else {
                        System.out.println("Out was null, or had an error");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                socketConnected = true;
                while (true) {
                    String res = in.readLine();
                    System.out.println("Server response: " + res);

                    if (res != null && !res.isEmpty()) {
                        numResponses++;
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
                System.out.println(e);
            }
        }
    }

}
