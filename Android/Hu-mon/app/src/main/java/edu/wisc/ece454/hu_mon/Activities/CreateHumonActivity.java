package edu.wisc.ece454.hu_mon.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;
import edu.wisc.ece454.hu_mon.Utilities.HumonIndexSaver;

public class CreateHumonActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Create Hu-mon";
    private final int MOVE_REQUEST_CODE = 1;
    private final String MOVE_DEFAULT_VALUE = "Add Move";
    private String MOVE_POSITION_KEY;
    private String MOVE_KEY;
    private final int MAX_RESOLUTION = 4096;

    private TextView statTextView;
    private String[] moveDisplayList;
    private Move[] moveList;
    ArrayAdapter<String> moveAdapter;
    ServerConnection mServerConnection;
    boolean mBound;
    IntentFilter filter = new IntentFilter();

    //image data
    private String tempImagePath;
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
        tempImagePath =  incomingIntent.getStringExtra(HUMON_IMAGE_KEY);
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
        moveDisplayList = new String[4];
        moveList = new Move[4];
        for(int i = 0; i < moveDisplayList.length; i++) {
            moveDisplayList[i] = MOVE_DEFAULT_VALUE;
        }

        GridView moveGridView = (GridView) findViewById(R.id.moveGridView);
        moveAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, moveDisplayList);
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
                Move chosenMove = (Move) data.getParcelableExtra(MOVE_KEY);
                String moveName = chosenMove.getName();

                if(movePosition > -1) {
                    moveDisplayList[movePosition] = moveName;
                    moveList[movePosition] = chosenMove;
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
        for(int i = 0; i < moveDisplayList.length; i++) {
            for(int j = 0; j < moveDisplayList.length; j++) {
                if(i == j) {
                    continue;
                }
                if(moveDisplayList[i].equals(MOVE_DEFAULT_VALUE)) {
                    Toast toast = Toast.makeText(this, "Must Fill All Moves", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(moveDisplayList[i].equals(moveDisplayList[j])) {
                    Toast toast = Toast.makeText(this, "Cannot Have Duplicate Moves", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
            }
        }

        //Retrieve user created data for humon
        ArrayList<Move> movesArrayList =  new ArrayList<Move>(Arrays.asList(moveList));
        TextView tempTextView = (TextView) findViewById(R.id.healthValue);
        int health = Integer.parseInt(tempTextView.getText().toString());
        tempTextView = (TextView) findViewById(R.id.attackValue);
        int attack = Integer.parseInt(tempTextView.getText().toString());
        tempTextView = (TextView) findViewById(R.id.defenseValue);
        int defense = Integer.parseInt(tempTextView.getText().toString());
        tempTextView = (TextView) findViewById(R.id.speedValue);
        int speed = Integer.parseInt(tempTextView.getText().toString());
        tempTextView = (TextView) findViewById(R.id.luckValue);
        int luck = Integer.parseInt(tempTextView.getText().toString());

        //retrieve email of the user
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        String userEmail = sharedPref.getString(getString(R.string.emailKey), "");


        //TODO: Create Humon object here and save
        // String name, String description, Bitmap image, int level, int xp, int hID, String uID, String iID, ArrayList<Move> moves, int health, int luck, int attack, int speed, int defense
        Humon h = new Humon(humonName, humonDescription, humonImage, 1, 0, 0, userEmail, "", movesArrayList, health, luck, attack, speed, defense, "");
        mServerConnection.sendMessage("CREATE-HUMON", h);

        //Store image path instead of image locally
        //TODO: replace filename with hID
        h.setImage(null);
        File imageFile = new File(tempImagePath);
        File renameFile = new File(getFilesDir(),humonName + ".jpg");
        if(imageFile.exists()) {
            imageFile.renameTo(renameFile);
        }
        h.setImagePath(renameFile.getPath());

        //save humon data to index
        AsyncTask<Humon, Integer, Boolean> indexSaveTask = new HumonIndexSaver(userEmail + getString(R.string.indexFile),
                userEmail, this, getString(R.string.humonsKey));
        indexSaveTask.execute(h);

        //return to the menu
        Intent intent = new Intent(this, MenuActivity.class);
        finish();
        startActivity(intent);

    }

    //Called after user hits DONE
    //Adds the humon to the user's index file
    //Returns true on successful write
    /*private boolean saveHumon(Humon createdHumon, String email) {
        boolean goodSave = true;

        String filename = email + getString(R.string.indexFile);
        String oldIndex = "";
        FileInputStream inputStream;
        FileOutputStream outputStream;

        //read in current index (if it exists)
        try {
            inputStream = openFileInput(filename);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            oldIndex = new String(buffer, "UTF-8");
            System.out.println("Current humon index: " + oldIndex);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("No index currently exists for: " + email);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //append new humon
        try {

            //append humon on to current object
            JSONObject indexJSON;
            JSONArray humonsArray;
            String HUMONS_KEY = getString(R.string.humonsKey);
            if(oldIndex.length() == 0) {
                indexJSON = new JSONObject();
                humonsArray = new JSONArray();
            }
            else {
                indexJSON = new JSONObject(oldIndex);
                humonsArray = indexJSON.getJSONArray(HUMONS_KEY);
            }

            humonsArray.put(createdHumon.toJson(new ObjectMapper()));
            indexJSON.put(HUMONS_KEY, humonsArray);

            //write object to file
            System.out.println("Writing: " + indexJSON.toString() + " to: " + filename);
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(indexJSON.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            goodSave = false;
        }

        return goodSave;
    }*/
}
