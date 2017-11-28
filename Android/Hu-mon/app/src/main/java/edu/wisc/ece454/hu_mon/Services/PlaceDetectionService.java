package edu.wisc.ece454.hu_mon.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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
                            //emrgency debug log statement
                            //Log.d(TAG,placeLikelihood.getPlace().getName() + " : " + placeLikelihood.getLikelihood());
                            if (placeLikelihood.getLikelihood() > 0.0) {
                                mostLikely.add(placeLikelihood.getPlace());
                            }
                        }
                        boolean healthyPlace = false;
                        for (Place loc : mostLikely) {
                            Log.d(TAG,"Place: " + loc.getName());
                            Log.d(TAG,"Want places " + Place.TYPE_PARK + " and " + Place.TYPE_GYM);
                            Log.d(TAG,"Place: " + loc.getPlaceTypes().toString());
                            healthyPlace = healthyPlace || loc.getPlaceTypes().contains(Place.TYPE_PARK);
                            healthyPlace = healthyPlace || loc.getPlaceTypes().contains(Place.TYPE_GYM);
                        }

                        if (healthyPlace) {
                            SharedPreferences sharedPref = getSharedPreferences(
                                    getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("inHealthyPlace",true);
                        }else{
                            SharedPreferences sharedPref = getSharedPreferences(
                                    getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("inHealthyPlace",false);
                        }
                        buff.release();
                    } else{
                        Log.e(TAG,"Failed to get place because of " + taskReturned.getException());
                    }
                }
            });

    }
}