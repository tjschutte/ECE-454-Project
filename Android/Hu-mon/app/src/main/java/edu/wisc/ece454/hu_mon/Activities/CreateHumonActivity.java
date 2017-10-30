package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;

public class CreateHumonActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Create Hu-mon";
    private final int MOVE_REQUEST_CODE = 1;
    private final String MOVE_DEFAULT_VALUE = "Add Move";
    private String MOVE_POSITION_KEY;
    private String MOVE_KEY;
    private final int MAX_RESOLUTION = 4096;

    private TextView statTextView;
    private String[] moveList;
    ArrayAdapter<String> moveAdapter;
    ServerConnection mServerConnection;
    boolean mBound;
    IntentFilter filter = new IntentFilter();

    //image data
    Bitmap rawHumonImage;
    Bitmap humonImage;
    int imageOrientation;
    int imageWidth;
    int imageHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_humon_layout);
        setTitle(ACTIVITY_TITLE);

        String HUMON_IMAGE_KEY = getString(R.string.humonImageKey);
        MOVE_POSITION_KEY = getString(R.string.movePositionKey);
        MOVE_KEY = getString(R.string.moveKey);
        Intent incomingIntent = getIntent();

        //load the image from previous activity
        String tempImagePath =  incomingIntent.getStringExtra(HUMON_IMAGE_KEY);
        rawHumonImage = BitmapFactory.decodeFile(tempImagePath);

        //obtain dimensions of the image
        imageWidth = Math.min(rawHumonImage.getWidth(), MAX_RESOLUTION);
        imageHeight = Math.min(rawHumonImage.getHeight(), MAX_RESOLUTION);

        //Change the image to the correct size and orientation
        Matrix m = new Matrix();
        imageOrientation = -90;
        m.postRotate(imageOrientation);
        humonImage = Bitmap.createBitmap(rawHumonImage, 0, 0, imageWidth, imageHeight, m, true);


        //display the image
        ImageView humonImageView = (ImageView) findViewById(R.id.humonImageView);
        humonImageView.setImageBitmap(humonImage);

        //obtain the textview for stat points
        statTextView = (TextView) findViewById(R.id.statValue);

        //Set up move grid
        moveList = new String[4];
        for(int i = 0; i < moveList.length; i++) {
            moveList[i] = MOVE_DEFAULT_VALUE;
        }

        GridView moveGridView = (GridView) findViewById(R.id.moveGridView);
        moveAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, moveList);
        moveGridView.setAdapter(moveAdapter);

        moveGridView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent moveIntent = new Intent(getApplication(), MoveListActivity.class);
                        moveIntent.putExtra(MOVE_POSITION_KEY, position);
                        startActivityForResult(moveIntent, MOVE_REQUEST_CODE);
                    }
                }
        );

        // Attach to the server communication service
        Intent intent = new Intent(this, ServerConnection.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerConnection.LocalBinder myBinder = (ServerConnection.LocalBinder) service;
            mServerConnection = myBinder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceConnection = null;
            mBound = false;
        }
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(getString(R.string.serverBroadCastResponseKey));
            String command;
            String data;
            if (response.indexOf(':') == -1) {
                // Got a bad response from the server. Do nothing.
                Toast toast = Toast.makeText(context, "Error communicating with server. Try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            command = response.substring(0, response.indexOf(':'));
            command = command.toUpperCase();
            data = response.substring(response.indexOf(':') + 1, response.length());

            System.out.println(command + ": " + data);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // make sure to unbind
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
            if (mBound) {
                unbindService(mServiceConnection);
                mBound = false;
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        filter.addAction(getString(R.string.serverBroadCastEvent));
        registerReceiver(receiver, filter);
        if (!mBound) {
            // Attach to the server communication service
            Intent intent = new Intent(this, ServerConnection.class);
            startService(intent);
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MOVE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                int movePosition = data.getIntExtra(MOVE_POSITION_KEY, -1);
                String moveName = data.getStringExtra(MOVE_KEY);

                if(movePosition > -1) {
                    moveList[movePosition] = moveName;
                    moveAdapter.notifyDataSetChanged();
                }

            }
        }
    }

    public void incrementStat(View view) {
        int statPoints = Integer.parseInt(statTextView.getText().toString());

        //check if you still have stat points to give
        if(statPoints > 0) {

            //decrement remaining stat points
            statPoints--;
            statTextView.setText("" + statPoints);

            //identify which stat is being updated
            String statTag = view.getTag().toString();

            TextView currStatView;

            //find value marked with given tag
            switch(statTag) {
                case "health":
                    currStatView = (TextView) findViewById(R.id.healthValue);
                    break;
                case "attack":
                    currStatView = (TextView) findViewById(R.id.attackValue);
                    break;
                case "defense":
                    currStatView = (TextView) findViewById(R.id.defenseValue);
                    break;
                case "speed":
                    currStatView = (TextView) findViewById(R.id.speedValue);
                    break;
                case "luck":
                    currStatView = (TextView) findViewById(R.id.luckValue);
                    break;
                default:
                    System.out.print("No stat for tag: " + statTag);
                    return;
            }

            //increment stat value
            if(currStatView != null) {
                int currStat = Integer.parseInt(currStatView.getText().toString());
                currStat++;
                currStatView.setText("" + currStat);
            }

        }
    }

    public void decrementStat(View view) {
        int statPoints = Integer.parseInt(statTextView.getText().toString());

        //identify which stat is being updated
        String statTag = view.getTag().toString();

        TextView currStatView;

        //find value marked with given tag
        switch(statTag) {
            case "health":
                currStatView = (TextView) findViewById(R.id.healthValue);
                break;
            case "attack":
                currStatView = (TextView) findViewById(R.id.attackValue);
                break;
            case "defense":
                currStatView = (TextView) findViewById(R.id.defenseValue);
                break;
            case "speed":
                currStatView = (TextView) findViewById(R.id.speedValue);
                break;
            case "luck":
                currStatView = (TextView) findViewById(R.id.luckValue);
                break;
            default:
                System.out.print("No stat for tag: " + statTag);
                return;
        }

        //decrement stat value
        if(currStatView != null) {
            int currStat = Integer.parseInt(currStatView.getText().toString());
            if(currStat > 1) {
                currStat--;
                currStatView.setText("" + currStat);

                //increment remaining stat points
                statPoints++;
                statTextView.setText("" + statPoints);
            }
        }

    }

    //rotates the humon image
    public void rotateImageRight(View view) {
        imageOrientation += 90;
        Matrix m = new Matrix();
        m.postRotate(imageOrientation);
        humonImage = Bitmap.createBitmap(rawHumonImage, 0, 0, imageWidth, imageHeight, m, true);
        ImageView humonImageView = (ImageView) findViewById(R.id.humonImageView);
        humonImageView.setImageBitmap(humonImage);

    }

    public void rotateImageLeft(View view) {
        imageOrientation -= 90;
        Matrix m = new Matrix();
        m.postRotate(imageOrientation);
        humonImage = Bitmap.createBitmap(rawHumonImage, 0, 0, imageWidth, imageHeight, m, true);
        ImageView humonImageView = (ImageView) findViewById(R.id.humonImageView);
        humonImageView.setImageBitmap(humonImage);

    }

    //button called by done button
    //saves humon data and returns user to menu
    //TODO: Add humon to index and send to server
    public void createHumon(View view) {

        //retrieve values from ui elements
        EditText nameView = (EditText) findViewById(R.id.humonNameEditText);
        EditText descriptionView = (EditText) findViewById(R.id.humonDescriptionEditText);
        String humonName = nameView.getText().toString();
        String humonDescription = descriptionView.getText().toString();
        int statPoints = Integer.parseInt(statTextView.getText().toString());

        //check all values are filled correctly
        if(humonName.length() == 0) {
            Toast toast = Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if(humonDescription.length() == 0) {
            Toast toast = Toast.makeText(this, "Description Required", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if(statPoints > 0) {
            Toast toast = Toast.makeText(this, "Stat Point(s) Remaining", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        //make sure moves are filled in correctly
        for(int i = 0; i < moveList.length; i++) {
            for(int j = 0; j < moveList.length; j++) {
                if(i == j) {
                    continue;
                }
                if(moveList[i].equals(MOVE_DEFAULT_VALUE)) {
                    Toast toast = Toast.makeText(this, "Must Fill All Moves", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(moveList[i].equals(moveList[j])) {
                    Toast toast = Toast.makeText(this, "Cannot Have Duplicate Moves", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
            }
        }

        ArrayList<String> moves = new ArrayList<>();
        for (String move : moveList) {
            moves.add(move);
        }

        //TODO: Create Humon object here and save
        // String name, String description, Bitmap image, int level, int xp, int hID, String uID, String iID, ArrayList<Move> moves, int health, int luck, int attack, int speed, int defense
        Humon h = new Humon(humonName, humonDescription, humonImage, 1, 0, 0, "", "", null, 10, 10, 10, 10, 10);
        mServerConnection.sendMessage("CREATE-HUMON", h);

        Toast toast = Toast.makeText(this, "Hu-mon Successfully Created", Toast.LENGTH_SHORT);
        toast.show();

        //return to the menu
        Intent intent = new Intent(this, MenuActivity.class);
        finish();
        startActivity(intent);

    }
}
