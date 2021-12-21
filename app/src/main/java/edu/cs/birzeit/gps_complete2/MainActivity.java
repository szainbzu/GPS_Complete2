package edu.cs.birzeit.gps_complete2;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    LocationCallback locationCallback;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    Switch sw_locationupdates, sw_gps;
    //This is Google Location Service API, this is the heart of our application.
    FusedLocationProviderClient fusedLocationProviderClient;
    // this is configuration class that tells FusedLocationProviderClient how to work
    LocationRequest locationRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        locationRequest =  LocationRequest.create();

        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //this event is triggered whenever the update interval in met
        locationCallback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUI(locationResult.getLastLocation());
            }
        };
        updateGPS();
    }

    public void gps_onClick(View view) {
        if(sw_gps.isChecked()){
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            tv_sensor.setText("Using GPS");
        }
        else{
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            tv_sensor.setText("Using WiFI and Towers");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 99){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }
            else{
                Toast.makeText(this, "this app requires permissions to work properly", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateGPS(){
        //get permissions from the user to track gps
        //get current location from fused client
        //update UI
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            updateUI(location);
                        }
                    });
        }
        else{
            //permission not granted, we will ask for it
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            }
        }
    }

    private void updateUI(Location location) {
        if(location == null){
            Toast.makeText(this, "Location not available",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else{
            tv_altitude.setText("not available");
        }

        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else{
            tv_speed.setText("not available");
        }
        Geocoder geocoder = new Geocoder(this);
        try{
            List<Address> address = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            tv_address.setText(address.get(0).getAddressLine(0));
        }catch(Exception e){
            tv_address.setText("Cannot get Street Address!");
        }

    }

    public void sw_updates_onclick(View view) {
        if(sw_locationupdates.isChecked()){
            startLocationUpdates();
        }
        else{
            stopLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked!");
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback, null);
        updateGPS();
    }
    private void stopLocationUpdates(){
        tv_updates.setText("Location is NOT being tracked!");
        tv_lat.setText("Not Tracking Location");
        tv_lon.setText("Not Tracking Location");
        tv_speed.setText("Not Tracking Location");
        tv_address.setText("Not Tracking Location");
        tv_accuracy.setText("Not Tracking Location");
        tv_altitude.setText("Not Tracking Location");
        tv_sensor.setText("Not Tracking Location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }
}



