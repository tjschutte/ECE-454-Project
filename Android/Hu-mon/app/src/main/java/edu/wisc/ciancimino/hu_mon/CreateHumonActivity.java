package edu.wisc.ciancimino.hu_mon;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class CreateHumonActivity extends AppCompatActivity {

    private final String ACTIVITY_TITLE = "Create Hu-mon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_humon_layout);
        setTitle(ACTIVITY_TITLE);

        String HUMON_IMAGE_KEY = getString(R.string.humonImageKey);
        Intent incomingIntent = getIntent();
        Bitmap humonImage = (Bitmap) incomingIntent.getParcelableExtra(HUMON_IMAGE_KEY);

        ImageView humonImageView = (ImageView) findViewById(R.id.humonImageView);

        humonImageView.setImageBitmap(humonImage);

    }
}
