package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
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
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 5; //Used to identify the permission's request
    Location mLastLocation; //Location object

    FusedLocationProviderClient mFusedLocationClient; //Location API's in Google Play services.
    TextView mLocationTextView;
    EditText editText;
    Spinner mySpinner;
    private List<SchoolsSample> schoolsSamples = new ArrayList<>();
    //HashMap for school names and school's ids
    HashMap<String, Integer> schoolsIds = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationTextView = findViewById(R.id.textView2);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        editText = (EditText) findViewById(R.id.editText);
        mySpinner = (Spinner) findViewById(R.id.spinner);


        getLocation(); // This is for checking that the user has permissions in the app start up.

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("RegisteredParents");
        readSchoolsData();

        ArrayList<String> nearSchoolsNames = new ArrayList<>();
        nearSchoolsNames.add("Select school...");
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, nearSchoolsNames);
        mySpinner.setAdapter(myAdapter);

        mySpinner.setOnTouchListener(new View.OnTouchListener() {
            /**
             * This method onTouch populate the spinner with the 15 nearest when the user clicks
             * on the spinner.
             * @param view
             * @param event
             * @return a false boolean to allow the spinner to be opened.
             */
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //We implemented this MotionEvent.ACTION_UP with help of
                // next tutorial https://www.manongdao.com/q-75314.html
                getLocation();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ArrayList<SchoolsSample> nearSchools;
                    nearSchools = getNearSchools(schoolsSamples);

                    ArrayList<String> nearSchoolsNames = new ArrayList<>();

                    //For loop implemented to populate spinner with 1
                    for (SchoolsSample school : nearSchools) {
                        nearSchoolsNames.add(school.getName() + String.format(" (%.2f km)", school.distance));
                        //Populate the HashMap schoolsIds
                        schoolsIds.put(school.getName() + String.format(" (%.2f km)", school.distance), new Integer(school.getOsm_id()));
                    }

                    //Adapter that will call the values and will integrate the values with the spinner.
                    ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, nearSchoolsNames);
                    mySpinner.setAdapter(myAdapter);
                }
                return false;
            }
        });

        Button buttonSubmit  = findViewById(R.id.button);
        buttonSubmit.setOnClickListener(new View.OnClickListener(){
            /**
             * This method onClick is called when hitting Submit button. We implemented this with
             * help of the next tutorials:
             * This tutorial for creating firebase connection: https://www.youtube.com/watch?v=lnidtzL71ZA&feature=youtu.be
             * This tutorial for inserting data in firebase database: https://www.youtube.com/watch?v=r-g2R_COMqo&feature=youtu.be
             * @param view
             */
            @Override
            public void onClick(View view) {
                //Validate required fields: name (editText) and school (mySpinner).
                if(TextUtils.isEmpty(editText.getText())){
                    editText.setError( "Name is required!" );
                }else if(mySpinner.getSelectedItem().toString()=="Select school..."){
                    ((TextView)mySpinner.getSelectedView()).setError("Error message");
                }
                else{
                    getLocation();
                    printInformation();
                    RegisteredParents registeredParent = new RegisteredParents();
                    registeredParent.setParentName(editText.getText().toString().trim());
                    registeredParent.setSchoolId(schoolsIds.get(mySpinner.getSelectedItem().toString()));
                    registeredParent.setUserLocation(mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
                    registeredParent.setTimeRegistration(mLastLocation.getTime());
                    System.out.println("test------->"+registeredParent.getUserLocation());
                    dbRef.push().setValue(registeredParent);
                    Toast.makeText(MainActivity.this, "Data inserted successfully", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * The onRequestPermissionResult method is triggered by the callback that is requesting the permissions.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast "Access Denied" (not visible right now, app closes if permissions are denied)
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
//                    TODO: When Denying permissions, the application should not close, instead
//                          the permissions should be checked when clicking on the spinner.  The
//                          permissions are currently checked in the spinner but the app crashes in
//                          the onclick action (presumably for running parallel callbacks).
//                  The first and second code lines, are for closing the application when the permissions are denied.
                    MainActivity.this.finish(); // 1. See comment above.
                    System.exit(0); // 2. See comment above.
                }
                break;
        }
    }

    /**
     * The getLocation method, checks if the application has permissions to access the user's location.
     * In addition triggers a callback (onRequestPermissionsResult) to request the permissions.
     */
    private void getLocation() {
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
                        } else {
                            mLocationTextView.setText(R.string.no_location);
                        }
                    }
                });
        }
    }


    /**
     * This getNearSchools method calculates the distance between the user's location and all schools
     * in the schoolsdata.csv file. The distance is calculating using the Haversine formula
     * implemented in the SchoolsSample class. Then, the method computes the 15 closest schools to
     * the user's location.
     * @param schoolsSamples This contains a list of Sweden's schools.
     * @return ArrayList<SchoolsSample>
     */
    private ArrayList<SchoolsSample> getNearSchools(List<SchoolsSample> schoolsSamples) {
        ArrayList<Double> schoolsDistance = new ArrayList<>();
        ArrayList<SchoolsSample> nearSchools = new ArrayList<>();
        for (SchoolsSample school: schoolsSamples) {
            school.haversineFormula(mLastLocation.getLongitude(), mLastLocation.getLatitude());
            schoolsDistance.add(school.distance);
        }

        ArrayList<Double> schoolsDistanceSorted = new ArrayList<>(schoolsDistance);
        Collections.sort(schoolsDistanceSorted);
        //For loop for obtaining the 15 nearest schools to the user.
        for (int i = 0; i < 15; i++) {
            int idx = schoolsDistance.indexOf(schoolsDistanceSorted.get(i));
            nearSchools.add(schoolsSamples.get(idx));
        }
        return nearSchools;
    }

    /**
     * This readSchoolsData method is reading the file schoolsdata.csv file in the resources folder.
     */
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
                //Read the data line by line
                if (!((line = reader.readLine()) != null)) break;
                    //Split by ','
                    String[] tokens = line.split(",");

                    //Store school attributes (Longitude, Latitude, Id and Name) in an object sample
                    // of the class SchoolsSample.
                    SchoolsSample sample = new SchoolsSample();
                    sample.setLongitude(Double.parseDouble(tokens[0]));
                    sample.setLatitude(Double.parseDouble(tokens[1]));
                    sample.setOsm_id(Integer.parseInt(tokens[2]));
                    sample.setName(tokens[3]);
                    schoolsSamples.add(sample);
            } catch (IOException e) {
                Log.wtf("MyActivity", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }

        }
    }

    /**
     * This printInformation method is for printing in the textView2 component the parent name gotten from
     * the editText component, the school name selected in myspinner component, Latitude and Longitud
     * obtained from the mLastLocation object.
     */
    public void printInformation(){
        mLocationTextView.setText(getString(R.string.textView2,
                editText.getText().toString(),
                mLastLocation.getLatitude(),
                mLastLocation.getLongitude(),
                mySpinner.getSelectedItem().toString(),
                mLastLocation.getTime()));
        mLocationTextView.setVisibility(View.VISIBLE);
    }
}