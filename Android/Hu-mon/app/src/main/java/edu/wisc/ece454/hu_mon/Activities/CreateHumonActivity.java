package edu.wisc.ece454.hu_mon.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.wisc.ece454.hu_mon.Models.Humon;
import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.Models.User;
import edu.wisc.ece454.hu_mon.R;
import edu.wisc.ece454.hu_mon.Services.ServerConnection;
import edu.wisc.ece454.hu_mon.Utilities.HumonIndexSaver;
import edu.wisc.ece454.hu_mon.Utilities.HumonPartySaver;
import edu.wisc.ece454.hu_mon.Utilities.UserHelper;

public class CreateHumonActivity extends SettingsActivity {

    private final String ACTIVITY_TITLE = "Create Hu-mon";
    private final int MOVE_REQUEST_CODE = 1;
    private final String MOVE_DEFAULT_VALUE = "Add Move";
    private String MOVE_POSITION_KEY;
    private String MOVE_KEY;
    private final int MAX_WIDTH = 1080;

    private TextView statTextView;
    private String[] moveDisplayList;
    private Move[] moveList;
    ArrayAdapter<String> moveAdapter;
    ServerConnection mServerConnection;
    boolean mBound;
    private String userEmail;
    private User user;

    //image data
    private String tempImagePath;
    Bitmap rawHumonImage;
    Bitmap humonImage;
    int imageOrientation;
    int imageWidth;
    int heightAdjusted;


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
        imageWidth = Math.min(rawHumonImage.getWidth(), MAX_WIDTH);

        //Change the image to the correct size and orientation
        Matrix m = new Matrix();
        imageOrientation = -90;
        m.postRotate(imageOrientation);

        // Caclulate the raw image aspect ratio.  We need to maintain this to make the image
        // not look like it was squeezed in one direction.
        float aspectRatio = (float)rawHumonImage.getHeight() / (float)rawHumonImage.getWidth();

        heightAdjusted = (int)(aspectRatio * (float)imageWidth);

        humonImage = Bitmap.createScaledBitmap(rawHumonImage, imageWidth, heightAdjusted, true);
        Log.d("Image dimenstions", "Height: " + heightAdjusted + " Width: " + imageWidth);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Thread rotateThread = new Thread() {
            public void run() {
                imageOrientation = 90;
                Matrix m = new Matrix();
                m.postRotate(imageOrientation);
                humonImage = Bitmap.createBitmap(humonImage, 0, 0, humonImage.getWidth(), humonImage.getHeight(), m, true);
                ImageView humonImageView = (ImageView) findViewById(R.id.humonImageView);
                humonImageView.setImageBitmap(humonImage);
            }
        };

        rotateThread.run();
    }

    public void rotateImageLeft(View view) {
        Thread rotateThread = new Thread() {
            public void run() {
                imageOrientation = -90;
                Matrix m = new Matrix();
                m.postRotate(imageOrientation);
                humonImage = Bitmap.createBitmap(humonImage, 0, 0, humonImage.getWidth(), humonImage.getHeight(), m, true);
                ImageView humonImageView = (ImageView) findViewById(R.id.humonImageView);
                humonImageView.setImageBitmap(humonImage);
            }
        };

        rotateThread.run();
    }

    //button called by done button
    //saves humon data and returns user to menu
    //TODO: Add humon to index and send to server
    public void createHumon(View view) throws IOException {

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
                if(moveList[i].getName().equals(moveList[j].getName()) ||
                        moveList[i].getId() == moveList[j].getId()) {
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
        userEmail = sharedPref.getString(getString(R.string.emailKey), "");

        //convert to string to send to server
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        humonImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String humonImageString = Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);

        // String name, String description, Bitmap image, int level, int xp, int hID, String uID, String iID, ArrayList<Move> moves, int health, int luck, int attack, int speed, int defense
        Humon h = new Humon(humonName, humonDescription, humonImageString, 1, 0, 0, userEmail, "",
                movesArrayList, health*10, luck, attack, speed, defense, "", health*10);

        //Store image path instead of image locally
        Humon localHumon = new Humon(humonName, humonDescription, null, 1, 0, 0, userEmail, "",
                movesArrayList, health*10, luck, attack, speed, defense, "", health*10);
        File imageFile = new File(getFilesDir(),humonName + ".jpg");
        storeImageFile(imageFile);
        localHumon.setImagePath(imageFile.getPath());
        System.out.println("Old image path: " + tempImagePath);
        System.out.println("New image path: " + localHumon.getImagePath());

        //save humon data to index
        AsyncTask<Humon, Integer, Boolean> indexSaveTask = new HumonIndexSaver(userEmail + getString(R.string.indexFile),
                userEmail, this, getString(R.string.humonsKey), false);
        indexSaveTask.execute(localHumon);

        //Create an instance if first humon
        //TODO:Add hCount to iID
        if(!hasHumons()) {
            Humon partyHumon = new Humon(humonName, humonDescription, null, 1, 1, 0, userEmail, userEmail + "-0",
                    movesArrayList, health*10, luck, attack, speed, defense, localHumon.getImagePath(), health*10);
            nameHumonDialog(partyHumon, h);
        }
        else {

            //Send new humon to server
            mServerConnection.sendMessage(getString(R.string.ServerCommandCreateHumon), h);

            //return to the menu
            finish();
        }

    }

    //stores humon image in file designated by file parameter
    private void storeImageFile(final File newFile) {

        Thread moveThread = new Thread() {
            public void run() {
                try {

                    FileOutputStream outputStream = new FileOutputStream(newFile);
                    humonImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        moveThread.run();
    }

    //check if the user has a humon in their party
    private boolean hasHumons() {
        boolean hasHumons = true;

        //TODO: Instead check user model (return true if hCount == 0)
        File partyFile = new File(this.getFilesDir(), userEmail + getString(R.string.partyFile));
        String partyFileText;

        //read in current index (if it exists)
        try {
            FileInputStream inputStream = new FileInputStream(partyFile);
            int inputBytes = inputStream.available();
            byte[] buffer = new byte[inputBytes];
            inputStream.read(buffer);
            inputStream.close();
            partyFileText = new String(buffer, "UTF-8");

            //check number of humons in file
            JSONObject pObject = new JSONObject(partyFileText);
            JSONArray humonsArray = pObject.getJSONArray(getString(R.string.humonsKey));
            if(humonsArray.length() == 0) {
                hasHumons = false;
            }
        } catch(FileNotFoundException e) {
            System.out.println("No party file for " + userEmail);
            hasHumons = false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            hasHumons = false;
        }

        return hasHumons;
    }

    //Create a dialog for user to input name for new party humon
    private void nameHumonDialog(Humon humon, final Humon serverHumon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.name_humon_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Name Hu-mon");

        //Assign the textboxes so they can be accessed by buttons
        final EditText nameText = (EditText) dialogView.findViewById(R.id.nameEditText);
        nameText.setText(humon.getName());

        //set to final (required to save)
        final Humon saveHumon = humon;
        user = UserHelper.loadUser(this);
        saveHumon.setIID(userEmail + "-" + user.getHcount());
        user.incrementHCount();
        UserHelper.saveUser(this, user);

        //Add Element to list
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String humonName = nameText.getText().toString();
                if(humonName.isEmpty()) {
                    //save humon data to party
                    AsyncTask<Humon, Integer, Boolean> partySaveTask = new HumonPartySaver(getApplicationContext());
                    partySaveTask.execute(saveHumon);
                }
                else {
                    //save humon data to index
                    saveHumon.setName(humonName);
                    AsyncTask<Humon, Integer, Boolean> partySaveTask = new HumonPartySaver(getApplicationContext());
                    partySaveTask.execute(saveHumon);
                }

                //send humon object to server
                mServerConnection.sendMessage(getString(R.string.ServerCommandCreateHumon), serverHumon);

                //return to the menu
                finish();
            }
        });

        //display the dialog
        final AlertDialog nameHumonDialog = builder.create();
        nameHumonDialog.show();
    }
}
