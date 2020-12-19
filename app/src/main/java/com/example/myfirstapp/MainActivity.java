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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 5; //used to identify the permission request
    Location mLastLocation;

    FusedLocationProviderClient mFusedLocationClient;
    TextView mLocationTextView;
    EditText editText;
    Spinner mySpinner;
    double latitude;
    double longitude;
    Long ts;
    private List<SchoolsSample> schoolsSamples = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationTextView = findViewById(R.id.textView2);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        editText = (EditText) findViewById(R.id.editText);

        mySpinner = (Spinner) findViewById(R.id.spinner);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("RegisteredParents");
        readSchoolsData();

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
                                System.out.println("success");
                                mLastLocation = location;
                                latitude = mLastLocation.getLatitude();
                                longitude = mLastLocation.getLongitude();
                                ts = mLastLocation.getTime();

                                ArrayList<SchoolsSample> nearSchools;
                                nearSchools = getNearSchools(schoolsSamples);

                                ArrayList<String> nearSchoolsNames = new ArrayList<>();

                                nearSchoolsNames.add("Select a school.");

                                for (SchoolsSample school: nearSchools){
                                    nearSchoolsNames.add(school.getName()+String.format(" (%.2f km)", school.distance));
                                }

                                //Adapter that will call the values and will integrate the values with the spinner.
                                ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item, nearSchoolsNames);
                                mySpinner.setAdapter(myAdapter);

                            } else {
                                mLocationTextView.setText(R.string.no_location);
                            }
                        }
                    });

        }

        Button buttonSubmit  = findViewById(R.id.button);
        buttonSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                getLocation();
                RegisteredParents registeredParent = new RegisteredParents();
                registeredParent.setName(editText.getText().toString().trim());
                registeredParent.setSchoolName(mySpinner.getSelectedItem().toString());
                registeredParent.setLocation(mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
                System.out.println("test------->"+registeredParent.getLocation());
                dbRef.push().setValue(registeredParent);

                Toast.makeText(MainActivity.this, "Data inserted successfully", Toast.LENGTH_LONG).show();
            }
        });
    }


    private ArrayList<SchoolsSample> getNearSchools(List<SchoolsSample> schoolsSamples) {
        ArrayList<Double> schoolsDistance = new ArrayList<>();
        ArrayList<SchoolsSample> nearSchools = new ArrayList<>();
        for (SchoolsSample school: schoolsSamples) {
            school.haversineFormula(longitude, latitude);
            schoolsDistance.add(school.distance);
        }

        ArrayList<Double> schoolsDistanceSorted = new ArrayList<>(schoolsDistance);
        Collections.sort(schoolsDistanceSorted);
        for (int i = 0; i < 15; i++) {
            int idx = schoolsDistance.indexOf(schoolsDistanceSorted.get(i));
            nearSchools.add(schoolsSamples.get(idx));
        }
        return nearSchools;
    }

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
                    //Split by ','
                    String[] tokens = line.split(",");

                    //Read the data
                    SchoolsSample sample = new SchoolsSample();
                    sample.setLongitude(Double.parseDouble(tokens[0]));
                    sample.setLatitude(Double.parseDouble(tokens[1]));
                    sample.setOsm_id(Integer.parseInt(tokens[2]));
                    sample.setName(tokens[3]);
                    schoolsSamples.add(sample);

//                    Log.d("MyActivity", "Just created: " + sample);

            } catch (IOException e) {
                Log.wtf("MyActivity", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }

        }
    }

    //TODO: We need to modify this method to add the logic to sent the information (parent name, School, latitude, longitude and time stamp) to the database.
    public void getLocation(){
        mLocationTextView.setText(getString(R.string.textView2,
                editText.getText().toString(),
                mySpinner.getSelectedItem().toString(),
                latitude,
                longitude,
                ts));
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