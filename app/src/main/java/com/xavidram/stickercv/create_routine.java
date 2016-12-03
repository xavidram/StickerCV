package com.xavidram.stickercv;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class create_routine extends AppCompatActivity implements View.OnClickListener{

    private Button cr_btn_Done, cr_btn_addCoordinate, cr_btn_clearCoordinates, cr_btn_back;
    private TextView coordView;
    private String fileName = "";
    //GPS Variables
    private LocationListener locationListener;
    protected LocationManager locationManager;
    double latitude, longitude;
    boolean isGPSEnabled, isNetworkEnabled, canGetLocation = false;
    private static final long Min_Distance_Between_GPS_Updates = 1; //1 meter
    private static final long Min_Time_Between_GPS_Updates = 1000; //1 second or 1000 ms
    private ArrayList<GPScoord> GPSCoordinates;
    private boolean toMain = false;
    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_routine);

        initEntities(); //initialize variables required for this activity

        //start location listener for checking gps changes.
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    GPScoord coord = new GPScoord(location.getLatitude(), location.getLongitude());
                    GPSCoordinates.add(coord); //add gps of current location to array of GPS
                    coordView.append("\n" + coord.latitudeAsString() + "," + coord.longitudeAsDouble());
                } catch (Exception e) {
                    //print error to user frontend
                    Toast.makeText(create_routine.this,
                            "Could not retreive GPS",
                            Toast.LENGTH_SHORT).show();
                    //print stacktrace for error debugging
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {
                //if provider disable or is disabled, take user to settings to turn on provider.
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        //permission check for GPS user, version 23 compiler for android needs additional check.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //ask user for permissions
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                },1); //1 is the error code.
            }
        }



    }

    private void initEntities(){

        //buttons
        cr_btn_back = (Button) findViewById(R.id.cr_btn_back);
        cr_btn_addCoordinate = (Button)findViewById(R.id.cr_btn_addCoordinate);
        cr_btn_clearCoordinates = (Button)findViewById(R.id.cr_btn_clearCoordinates);
        cr_btn_Done = (Button)findViewById(R.id.cr_btn_Done);
        //textviews
        coordView = (TextView)findViewById(R.id.CoordView);
        //array initialization for coordinates;
        GPSCoordinates = new ArrayList<GPScoord>();
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        cr_btn_back.setOnClickListener(this);
        cr_btn_addCoordinate.setOnClickListener(this);
        cr_btn_Done.setOnClickListener(this);
        cr_btn_Done.setEnabled(false);
        cr_btn_clearCoordinates.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cr_btn_addCoordinate:
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                //Toast.makeText(create_routine.this, "Coordinate Button Clicked", Toast.LENGTH_SHORT).show();
                cr_btn_Done.setEnabled(true);
                break;
            case R.id.cr_btn_clearCoordinates:
                coordView.setText("Coordinates:"); //reset textview to no appended text
                GPSCoordinates.clear(); //remove all coordinates if there
                cr_btn_Done.setEnabled(false);
                break;
            case R.id.cr_btn_Done:
                toMain = false;
                try {
                    //get first item and put it at end of gps list so drone returns to initial location
                    GPSCoordinates.add(GPSCoordinates.get(0));
                    //Toast.makeText(create_routine.this, "Done Button Clicked", Toast.LENGTH_SHORT).show();

                    //if we get here, then lets write all the data to a text file and store on local device
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Name the Routine");
                    fileName = ""; //reset filename

                    //set up the input
                    final EditText fileNameBox = new EditText(this);
                    //specify the input type
                    fileNameBox.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(fileNameBox);

                    //set up the buttons
                    builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                       @Override
                        public void onClick(DialogInterface dialog, int which){
                           fileName = fileNameBox.getText().toString(); //this is what we will name the file
                           try {
                               File newRoutine = new File(Environment.getExternalStorageDirectory().getPath()+fileName+".txt");
                               newRoutine.createNewFile();
                               PrintWriter output = new PrintWriter(new FileWriter(newRoutine));
                               for(GPScoord c : GPSCoordinates){
                                   output.println(c.cordsToString());
                               }
                               output.close();
                               toMain = true;
                           }catch (Exception e){
                               e.printStackTrace();
                           }

                       }
                    });
                    builder.setPositiveButton("Cancle",  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            GPSCoordinates.remove(GPSCoordinates.size() - 1); //pop last item
                            toMain = false;
                            dialog.cancel();
                        }
                    });

                    //lets now show the dialoug box
                    builder.show();

                    if(toMain){
                        //return to mainactivity
                        Intent i = new Intent(this, MainActivity.class);
                        startActivity(i);
                    }else {
                        toMain = false;
                    }

                } catch (ArrayIndexOutOfBoundsException e){
                    //array out of bounds when there is no coordinates added
                    Toast.makeText(create_routine.this, "No Corrdinates yet in the list!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cr_btn_back:
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            default:
                break;
        }
    }

    //permissions for location services grab
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        //any error codes we design should pass through here
        switch (requestCode){
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    initEntities();
                return;
        }
    }

}
