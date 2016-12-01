package com.xavidram.stickercv;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import dji.common.camera.CameraSystemState;
import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.missionmanager.DJICustomMission;
import dji.sdk.missionmanager.DJIMission;
import dji.sdk.missionmanager.DJIMissionManager;
import dji.sdk.missionmanager.missionstep.DJIGoHomeStep;
import dji.sdk.missionmanager.missionstep.DJIMissionStep;
import dji.sdk.missionmanager.missionstep.DJITakeoffStep;

public class Flight extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private static final String TAG = Flight.class.getName();
    private Button btn_flight_start;
    protected TextureView mVideoSurface;
    private String RoutineName;

    //DJI Variables
    protected DJIMission mDJIMission;
    private DJIMissionManager mMissionManager;
    private DJIFlightController mFlightController;
    private Animation mFadeoutAnimation;
    protected ProgressBar mPB;
    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    private TextView recordingTime;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        RoutineName = getIntent().getExtras().getString("RoutineName");



        initUI();

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new DJICamera.CameraReceivedVideoDataCallback() {

            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if(mCodecManager != null){
                    // Send the raw H264 video data to codec manager for decoding
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }else {
                    Log.e(TAG, "mCodecManager is null");
                }
            }
        };

        DJICamera camera = FPVDemoApplication.getCameraInstance();

        if (camera != null) {

            camera.setDJICameraUpdatedSystemStateCallback(new DJICamera.CameraUpdatedSystemStateCallback() {
                @Override
                public void onResult(CameraSystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        Flight.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordingTime.setText(timeString);

                                /*
                                 * Update recordingTime TextView visibility and mRecordBtn's check state
                                 */
                                if (isVideoRecording){
                                    recordingTime.setVisibility(View.VISIBLE);
                                }else
                                {
                                    recordingTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });

        }


    }

    protected void onProductChange() {
        initPreviewer();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();

        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.mVideoSurface);

        recordingTime = (TextView) findViewById(R.id.timer);
        btn_flight_start = (Button) findViewById(R.id.btn_flight_start);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        btn_flight_start.setOnClickListener(this);

        recordingTime.setVisibility(View.INVISIBLE);
    }

    private void initPreviewer() {

        DJIBaseProduct product = FPVDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UnknownAircraft)) {
                DJICamera camera = product.getCamera();
                if (camera != null){
                    // Set the callback
                    camera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {
        DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            FPVDemoApplication.getCameraInstance().setDJICameraReceivedVideoDataCallback(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Flight.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    protected DJIMission initMission() {

        LinkedList<DJIMissionStep> steps = new LinkedList<DJIMissionStep>();

        DJITakeoffStep takingoff = new DJITakeoffStep(new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                Toast.makeText(Flight.this, "Takeoff", Toast.LENGTH_SHORT).show();
            }
        });
        DJIGoHomeStep goinghome = new DJIGoHomeStep(new DJICommonCallbacks.DJICompletionCallback() {

            @Override
            public void onResult(DJIError error) {
                Toast.makeText(Flight.this, "Landing", Toast.LENGTH_SHORT).show();
            }
        });

        steps.add(takingoff);
        steps.add(goinghome);


        DJICustomMission damission = new DJICustomMission(steps);

        return damission;
    }
    private void setProgressBar(final int progress) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (progress >= 100) {
                    mPB.setVisibility(View.VISIBLE);
                    mPB.setProgress(100);
                    mFadeoutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mPB.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPB.startAnimation(mFadeoutAnimation);
                        }
                    });

                } else if (progress < 0) {
                    mPB.setVisibility(View.INVISIBLE);
                    mPB.setProgress(0);
                } else {
                    mPB.setVisibility(View.VISIBLE);
                    mPB.setProgress(0);
                }
            }
        });
    }
    @Override
    public void onClick(View v) {
        mMissionManager = DJIMissionManager.getInstance();

        switch (v.getId()) {
            //PREPARE
            case R.id.btn_flight_start:{
                //   Thread.sleep(100);
                Toast.makeText(Flight.this, "i am here", Toast.LENGTH_SHORT).show();
                mDJIMission = initMission();
                if (mDJIMission == null) {
                    Toast.makeText(Flight.this, "choose a mission type", Toast.LENGTH_SHORT).show();
                }mMissionManager.prepareMission(mDJIMission, new DJIMission.DJIMissionProgressHandler() {

                            @Override
                            public void onProgress(DJIMission.DJIProgressType type, float progress) {
                                setProgressBar((int)(progress * 100f));
                            }

                        }
                        ,new DJICommonCallbacks.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError error) {
                                if (error == null) {
                                    Toast.makeText(Flight.this, "success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Flight.this, "prepare", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            }
            default:
                break;
        }
    }


    private void switchCameraMode(DJICameraSettingsDef.CameraMode cameraMode){

        DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.setCameraMode(cameraMode, new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });
        }

    }

    // Method for taking photo
    private void captureAction(){

        DJICameraSettingsDef.CameraMode cameraMode = DJICameraSettingsDef.CameraMode.ShootPhoto;

        final DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {

            DJICameraSettingsDef.CameraShootPhotoMode photoMode = DJICameraSettingsDef.CameraShootPhotoMode.Single; // Set the camera capture mode as Single mode
            camera.startShootPhoto(photoMode, new DJICommonCallbacks.DJICompletionCallback() {

                @Override
                public void onResult(DJIError error) {
                    if (error == null) {
                        showToast("take photo: success");
                    } else {
                        showToast(error.getDescription());
                    }
                }

            }); // Execute the startShootPhoto API
        }
    }

    // Method for starting recording
    private void startRecord(){

        DJICameraSettingsDef.CameraMode cameraMode = DJICameraSettingsDef.CameraMode.RecordVideo;
        final DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new DJICommonCallbacks.DJICompletionCallback(){
                @Override
                public void onResult(DJIError error)
                {
                    if (error == null) {
                        showToast("Record video: success");
                    }else {
                        showToast(error.getDescription());
                    }
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord(){

        DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new DJICommonCallbacks.DJICompletionCallback(){

                @Override
                public void onResult(DJIError error)
                {
                    if(error == null) {
                        showToast("Stop recording: success");
                    }else {
                        showToast(error.getDescription());
                    }
                }
            }); // Execute the stopRecordVideo API
        }

    }

}
