package com.xavidram.stickercv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button mRunRoutine, mCreateRoutine, mEditRoutine;
    private Spinner routineSpinner;
    private ArrayList<String> Routines;
    private ArrayAdapter<String> spinnerAdapter;

    //files reader class
    private appFileManager fileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEntities();

    }

    public void initEntities(){
        //buttons
        mRunRoutine = (Button) findViewById(R.id.ma_btn_StartRoutine);
        mCreateRoutine = (Button) findViewById(R.id.ma_btn_CreateRoutine);
        mEditRoutine = (Button) findViewById(R.id.ma_btn_EditRoutine);
        //spinner
        routineSpinner = (Spinner) findViewById(R.id.ma_RoutineSpinner);

        //--populate spinner with names of routines--\\
        try {
            fileManager = new appFileManager(new File(Environment.getExternalStorageDirectory().getPath() ));
            //get names of file paths
            Routines = fileManager.getListFileNames();
            //create the adapter for the spinner
            spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Routines);
            //populate the spinner
            routineSpinner.setAdapter(spinnerAdapter);
        } catch (Exception e){
            e.printStackTrace();
        }

        //set onclick to buttons
        mRunRoutine.setOnClickListener(this);
        mCreateRoutine.setOnClickListener(this);
        mEditRoutine.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ma_btn_StartRoutine:
                String filename;
                try {
                    //lets grab the name of the item
                    filename = routineSpinner.getSelectedItem().toString();
                    Intent intent = new Intent(this, Flight.class);
                    //pass it to next activity as extra
                    intent.putExtra("RoutineName",filename);
                    //gogoshing!!
                    startActivity(intent);
                }catch(Exception e) {
                    Toast.makeText(MainActivity.this,"Error!!!",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            case R.id.ma_btn_CreateRoutine:
                //lets go to create a routine!
                Intent i_to_create = new Intent(this, create_routine.class);
                startActivity(i_to_create);
                break;
            case R.id.ma_btn_EditRoutine:
                /*
                String editFile;
                try {
                    //lets grab the name of the item
                    filename = routineSpinner.getSelectedItem().toString();
                    Intent i_to_edit = new Intent(this, edit_routine.class);
                    //pass it to next activity as extra
                    i_to_edit.putExtra("RoutineName",filename+".txt");
                    //gogoshing!!
                    startActivity(i_to_edit);
                }catch(Exception e) {
                    Toast.makeText(MainActivity.this,"Error!!!",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                */
                Toast.makeText(MainActivity.this,"W.I.P",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private ArrayList<String> getRoutineNames(List<File> routines){
        ArrayList<String> temp = new ArrayList<String>();
        for (File r : routines){
            temp.add(r.getName());
        }
        return temp;
    }

    //get names of routines from storage area
    private List<File> getListFiles(File ParentDir) {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = ParentDir.listFiles();
        for (File f : files){
            if(f.getName().endsWith("txt")){
                inFiles.add(f);
            }
        }
        return inFiles;
    }

}
