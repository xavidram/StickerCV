package com.xavidram.stickercv;

import android.app.Application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by localpuppy on 12/1/16.
 */

public class appFileManager extends Application{

    private File parentDIR;

    public appFileManager(File parentDIR){
        this.parentDIR = parentDIR;
    }

    public ArrayList<File> getListFiles() {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDIR.listFiles();
        for (File f : files)
            if (f.getName().endsWith("txt"))
                inFiles.add(f);

        return inFiles;
    }

    public ArrayList<String> getRoutineNames(List<File> routines) {
        ArrayList<String> routeNames = new ArrayList<String>();
        for (File r : routines) {
            routeNames.add(r.getName());
        }
        return routeNames;
    }

    public ArrayList<String> getListFileNames() {
        ArrayList<String> fileNames = new ArrayList<String>();
        File[] files = parentDIR.listFiles();
        for (File f: files)
            if (f.getName().endsWith("txt"))
                fileNames.add(f.getName());
        return fileNames;
    }

    public File getFile(String filename) {
        ArrayList<File> inFiles = getListFiles();
        for (File f : inFiles)
            if (f.getName().equals(filename))
                return f;
        return null;
    }

}
