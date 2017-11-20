package edu.wisc.ece454.hu_mon.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.ece454.hu_mon.Activities.WildBattleActivity;
import edu.wisc.ece454.hu_mon.R;


public class PlaceDetectionService extends Service {

    private static final String TAG = "GPS";

    // The entry points to the Places API.
    private PlaceDetectionClient mPlaceDetectionClient;
    private GeoDataClient mGeoDataClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // the location request
    private LocationRequest mLocationRequest;

    // the location callback
    private LocationCallback mLocationCallback;

    // the most recent location
    private Location mostRecentLocation;

    @Override
    public void onCreate() {

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Generate a Location Request object
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // create our callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mostRecentLocation = locationResult.getLastLocation();
                Log.d(TAG,mostRecentLocation.toString());
                checkMostRecentPlace();
            };
        };

    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null/*looper we don't need*/);
        }catch(SecurityException sec) {
            Log.e(TAG, "onStartCommand, no permission for ACCESS_FINE_LOCATION");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void checkMostRecentPlace() {

        Log.d(TAG,"in place detect");

        @SuppressWarnings("MissingPermission")
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> taskReturned) {
                    if(taskReturned.isSuccessful()) {
                        PlaceLikelihoodBufferResponse buff = taskReturned.getResult();
                        Log.d(TAG,"Success!");
                        List<Place> mostLikely = new ArrayList<>();
                        for (PlaceLikelihood placeLikelihood : buff) {
                            Log.i(TAG,placeLikelihood.getPlace().getName() + " : " + placeLikelihood.getLikelihood());
                            if (placeLikelihood.getLikelihood() > 0.0) {
                                mostLikely.add(placeLikelihood.getPlace());
                            }
                        }
                        boolean healthyPlace = false;
                        for (Place loc : mostLikely) {
                            Log.d(TAG,"Place: " + loc.getName());
                            Log.d(TAG,"Place: " + loc.getPlaceTypes().toString());
                            healthyPlace = healthyPlace || loc.getPlaceTypes().contains(Place.TYPE_PARK);
                            healthyPlace = healthyPlace || loc.getPlaceTypes().contains(Place.TYPE_GYM);
                        }

                        if (healthyPlace) {
                            wildHumonNotification();
                        }
                        buff.release();
                    } else{
                        Log.e(TAG,"Failed to get place because of " + taskReturned.getException());
                    }
                }
            });

    }

    //Notifies the user of a wild humon
    private void wildHumonNotification() {
        Log.i(TAG,"location humon found");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, WildBattleActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Battle")
                .setContentText("A Wild Hu-mon appeared")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal_background)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(69, notification);


    }
}