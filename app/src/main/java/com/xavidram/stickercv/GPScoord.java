package com.xavidram.stickercv;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.products.DJIAircraft;
import dji.sdk.products.DJIHandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by localpuppy on 11/23/16.
 */

public class GPScoord extends Application {
    private double latitude;
    private double longitude;

    public GPScoord(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public String latitudeAsString() {
        return String.valueOf(this.latitude);
    }
    public String longitudeAsString(){
        return String.valueOf(this.longitude);
    }
    public double latitudeAsDouble(){
        return this.latitude;
    }
    public double longitudeAsDouble(){
        return this.longitude;
    }
    public String cordsToString() {
        return String.valueOf(this.latitude) + "," + String.valueOf(this.longitude);
    }
}