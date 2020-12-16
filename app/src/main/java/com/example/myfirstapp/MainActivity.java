package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

//import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {
    
    private static final int REQUEST_LOCATION_PERMISSION = 5; //used to identify the permission request
    Location mLastLocation;

    FusedLocationProviderClient mFusedLocationClient;
    TextView mLocationTextView;
    EditText editText;
    Spinner mySpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationTextView = findViewById(R.id.textView2);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        editText = (EditText) findViewById(R.id.editText);

        mySpinner = (Spinner) findViewById(R.id.spinner);

        readSchoolsData();

        ArrayList<String> listtenfirstschools = new ArrayList<>();
        for (int i=0; i<10; i++){
            listtenfirstschools.add(schoolsSamples.get(i).getName());
        }

        //Adapter that will call the values and will integrate the values with the spinner.
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, listtenfirstschools);
        mySpinner.setAdapter(myAdapter);


        Button buttonSubmit  = findViewById(R.id.button);
        buttonSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
    }

    private List<SchoolsSample> schoolsSamples = new ArrayList<>();

    private void readSchoolsData() {
        InputStream is = getResources().openRawResource(R.raw.schoolsdata);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";

        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {

                if (!((line = reader.readLine()) != null)) break;
                    Log.d("MyActivity", "Line: " + line);
                    //Split by ','
                    String[] tokens = line.split(",");

                    //Read the data
                    SchoolsSample sample = new SchoolsSample();
                    sample.setLongitude(Double.parseDouble(tokens[0]));
                    sample.setLatitude(Double.parseDouble(tokens[1]));
                    sample.setOsm_id(Integer.parseInt(tokens[2]));
                    sample.setName(tokens[3]);
                    schoolsSamples.add(sample);

                    Log.d("MyActivity", "Just created: " + sample);

            } catch (IOException e) {
                Log.wtf("MyActivity", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }

        }


    }

    public void getLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mLastLocation = location;
                                mLocationTextView.setText(
                                        getString(R.string.textView2,
                                                editText.getText().toString(),
                                                mySpinner.getSelectedItem().toString(),
                                                mLastLocation.getLatitude(),
                                                mLastLocation.getLongitude(),
                                                mLastLocation.getTime()));
                            } else {
                                mLocationTextView.setText(R.string.no_location);
                            }
                        }
            });

        }
        mLocationTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}