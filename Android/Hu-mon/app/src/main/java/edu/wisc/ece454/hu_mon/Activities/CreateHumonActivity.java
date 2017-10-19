package edu.wisc.ece454.hu_mon.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import edu.wisc.ece454.hu_mon.R;

public class CreateHumonActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Create Hu-mon";
    private String tempImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_humon_layout);
        setTitle(ACTIVITY_TITLE);

        String HUMON_IMAGE_KEY = getString(R.string.humonImageKey);
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

    }
}
