package edu.wisc.ece454.hu_mon.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.wisc.ece454.hu_mon.R;

public class CreateHumonActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Create Hu-mon";
    private final int MOVE_REQUEST_CODE = 1;
    private final String MOVE_DEFAULT_VALUE = "Add Move";
    private String MOVE_POSITION_KEY;
    private String MOVE_KEY;

    private TextView statTextView;
    private String[] moveList;
    ArrayAdapter<String> moveAdapter;

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
        Bitmap rawHumonImage = BitmapFactory.decodeFile(tempImagePath);

        //Change the image to the correct size and orientation
        Matrix m = new Matrix();
        m.postRotate(-90);
        Bitmap humonImage = Bitmap.createBitmap(rawHumonImage, 0, 0, rawHumonImage.getWidth(), rawHumonImage.getHeight(), m, true);


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

        //TODO: Create Humon object here and save


        Toast toast = Toast.makeText(this, "Hu-mon Successfully Created", Toast.LENGTH_SHORT);
        toast.show();

        //return to the menu
        Intent intent = new Intent(this, MenuActivity.class);
        finish();
        startActivity(intent);

    }
}
