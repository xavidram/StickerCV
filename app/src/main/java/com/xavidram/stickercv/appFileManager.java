package com.xavidram.stickercv;

import android.app.Application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by localpuppy on 12/1/16.
 */

public class appFileManager extends Application{

    File parentDIR;

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

}
