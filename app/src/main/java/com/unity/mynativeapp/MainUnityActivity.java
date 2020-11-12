package com.unity.mynativeapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.company.product.OverrideUnityActivity;
import com.unity3d.player.UnityPlayer;

import static com.unity.mynativeapp.Database.getPath;

public class MainUnityActivity extends OverrideUnityActivity {

    private final String TAG = "AlmogMainUnityActivity";
    static private String riskAreaJsonPath;

    private  boolean isChanged;
    Button UI_BTN_back;

    Button UI_BTN_tiltPlus;
    Button UI_BTN_resetCamera;
    Button UI_BTN_tiltMinus;

    Button UI_BTN_addLayers;
    Button UI_BTN_removeLayers;

    Button UI_BTN_addRiskArea;

    Button UI_BTN_HeightPlus;
    Button UI_BTN_HeightMinus;
    Button UI_BTN_cubeUp;
    Button UI_BTN_cubeDown;

    Button UI_BTN_export;
    Button UI_BTN_delete;

    ImageView iv;

    private boolean isDialoging;

    int buttonsTestSize = 26;
    int buttonLeftMargin = 10;
    int buttonHeight = 130;
    int buttonWidth = 160;
    int spacer = 70;
    int group = 15;
    int UI_firstSectionHeight = 10;
    int UI_SecondSectionHeight = UI_firstSectionHeight + buttonHeight + spacer;
    int UI_ThirdSectionHeight = UI_SecondSectionHeight + buttonHeight * 3 + spacer;
    int UI_FourthSectionHeight = UI_ThirdSectionHeight + buttonHeight * 2 + spacer;

    void isChangesTrue(){
        Log.d(TAG,"entering chanig = true func");
        isChanged = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String cmdLine = updateUnityCommandLineArguments(getIntent().getStringExtra("unity"));
        getIntent().putExtra("unity", cmdLine);
        mUnityPlayer = new UnityPlayer(this, this);
        setContentView(com.unity3d.player.R.layout.activity_unity);
        FrameLayout frameLayout = (FrameLayout)findViewById(com.unity3d.player.R.id.unity_player_layout);
        frameLayout.addView(mUnityPlayer.getView());
        mUnityPlayer.requestFocus();

        isChanged = false;

        String lasFilePath = Database.getPath();
        UnitySendMessage("Main Camera", "update3DModelPath", lasFilePath);
        UnitySendMessage("Main Camera", "updateRiskAreaJsonPath", riskAreaJsonPath);
        UnitySendMessage("Main Camera", "start", "");

        ////////////////////
        //     UI Back    //
        ////////////////////

        UI_BTN_back = new Button(getApplicationContext());
        UI_BTN_back.setText("Back");
        UI_BTN_back.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_back.setX(buttonLeftMargin);
        UI_BTN_back.setY(UI_firstSectionHeight);
        UI_BTN_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                UnitySendMessage("Main Camera", "checkIfChanged", "");
                if(isChanged){

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which){

                                    case DialogInterface.BUTTON_POSITIVE:
                                        UnitySendMessage("Main Camera", "jsonUpdate", "");
                                        Log.d(TAG,"closing unity and saving changes");
                                        showMainActivity("");
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        Log.d(TAG,"closing unity and saving changes");
                                        showMainActivity("");
                                        break;

                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getUnityFrameLayout().getContext());
                        builder
                                .setMessage("Do you want to save changes ?\n\n ")
                                .setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener)
                                .setNeutralButton("Cancel",dialogClickListener)
                                .setCancelable(true)
                                .show();

                } else{
                    showMainActivity("");
                }

            }
        });
        getUnityFrameLayout().addView(UI_BTN_back, buttonWidth, buttonHeight);

        /////////////////////////////
        //     By Holoarch LOGO    //
        /////////////////////////////

        iv = new ImageView(getApplicationContext());
        iv.setId(0);
        iv.setImageDrawable(getDrawable(R.drawable.by_holoarch));
        iv.setId(0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(600,240);
        lp.setMargins(width - 650, height - 200 ,0, 0);
        iv.setLayoutParams(lp);
        getUnityFrameLayout().addView(iv);

        ////////////////////////////////////////
        //     Adding hidden cubs controls    //
        ////////////////////////////////////////

        /////////////////////////////
        //     UI Height Plus      //
        /////////////////////////////

        UI_BTN_HeightPlus = new Button(getApplicationContext());
        UI_BTN_HeightPlus.setText("Height +");
        UI_BTN_HeightPlus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_HeightPlus.setX(buttonLeftMargin);
        UI_BTN_HeightPlus.setY(height - buttonHeight - 10);
        UI_BTN_HeightPlus.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override public void run() {
                    UnitySendMessage("Main Camera", "cubeActions", "heightUp");
                    mHandler.postDelayed(this, 30);
                }
            };
        });
        UI_BTN_HeightPlus.setVisibility(View.GONE);
        getUnityFrameLayout().addView(UI_BTN_HeightPlus, buttonWidth, buttonHeight);

        /////////////////////////////
        //     UI Height Minus     //
        /////////////////////////////

        UI_BTN_HeightMinus = new Button(getApplicationContext());
        UI_BTN_HeightMinus.setText("Height -");
        UI_BTN_HeightMinus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_HeightMinus.setX(buttonLeftMargin);
        UI_BTN_HeightMinus.setY(height - group - 10);
        UI_BTN_HeightMinus.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override public void run() {
                    UnitySendMessage("Main Camera", "cubeActions", "heightDown");
                    mHandler.postDelayed(this, 30);
                }
            };
        });
        UI_BTN_HeightMinus.setVisibility(View.GONE);
        getUnityFrameLayout().addView(UI_BTN_HeightMinus, buttonWidth, buttonHeight);

        /////////////////////////////
        //     UI Cube UP          //
        /////////////////////////////

        UI_BTN_cubeUp = new Button(getApplicationContext());
        UI_BTN_cubeUp.setText("Move Up");
        UI_BTN_cubeUp.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_cubeUp.setX(buttonLeftMargin + buttonHeight + 0.5f * spacer);
        UI_BTN_cubeUp.setY(height - buttonHeight - 10);
        UI_BTN_cubeUp.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override public void run() {
                    UnitySendMessage("Main Camera", "cubeActions", "moveUp");
                    mHandler.postDelayed(this, 30);
                }
            };
        });
        UI_BTN_cubeUp.setVisibility(View.GONE);
        getUnityFrameLayout().addView(UI_BTN_cubeUp, buttonWidth, buttonHeight);

        /////////////////////////////
        //     UI Cube Down        //
        /////////////////////////////

        UI_BTN_cubeDown = new Button(getApplicationContext());
        UI_BTN_cubeDown.setText("Move Down");
        UI_BTN_cubeDown.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_cubeDown.setX(buttonLeftMargin + buttonHeight + 0.5f * spacer);
        UI_BTN_cubeDown.setY(height - group - 10);
        UI_BTN_cubeDown.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override public void run() {
                    UnitySendMessage("Main Camera", "cubeActions", "moveDown");
                    mHandler.postDelayed(this, 30);
                }
            };
        });
        UI_BTN_cubeDown.setVisibility(View.GONE);
        getUnityFrameLayout().addView(UI_BTN_cubeDown, buttonWidth, buttonHeight);


    }

    @Override
    protected void showMainActivity(String s) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateControlsToUnityFrame();
    }

    public void vibrate(){
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
    }

    public void showDeleteButton(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UI_BTN_delete.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideDeleteButton(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UI_BTN_delete.setVisibility(View.GONE);
            }
        });
    }

    public void hideCubeControls(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(UI_BTN_HeightPlus != null){
                    UI_BTN_HeightPlus.setVisibility(View.GONE);
                    UI_BTN_HeightMinus.setVisibility(View.GONE);
                    UI_BTN_cubeUp.setVisibility(View.GONE);
                    UI_BTN_cubeDown.setVisibility(View.GONE);
                }
            }
        });
    }

    public void showCubeControls() {
        runOnUiThread(new Runnable() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void run() {
                if(UI_BTN_HeightPlus != null){
                    UI_BTN_HeightPlus.setVisibility(View.VISIBLE);
                    UI_BTN_HeightMinus.setVisibility(View.VISIBLE);
                    UI_BTN_cubeUp.setVisibility(View.VISIBLE);
                    UI_BTN_cubeDown.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void addControlsToUnityFrame() {

        runOnUiThread(new Runnable() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void run() {

                // Remove "By Holoarch" LOGO
                getUnityFrameLayout().removeView(iv);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;

                //////////////////////
                //     UI TILT +    //
                //////////////////////

                UI_BTN_tiltPlus = new Button(getApplicationContext());
                UI_BTN_tiltPlus.setText("Tilt +");
                UI_BTN_tiltPlus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_tiltPlus.setX(buttonLeftMargin);
                UI_BTN_tiltPlus.setY(UI_SecondSectionHeight);
                UI_BTN_tiltPlus.setOnTouchListener(new View.OnTouchListener() {
                    private Handler mHandler;
                    @Override public boolean onTouch(View v, MotionEvent event) {
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (mHandler != null) return true;
                                mHandler = new Handler();
                                mHandler.postDelayed(mAction, 0);
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mHandler == null) return true;
                                mHandler.removeCallbacks(mAction);
                                mHandler = null;
                                break;
                        }
                        return false;
                    }
                    Runnable mAction = new Runnable() {
                        @Override public void run() {
                            UnitySendMessage("Main Camera", "TiltCamera", "-");
                            mHandler.postDelayed(this, 30);
                        }
                    };
                });
                getUnityFrameLayout().addView(UI_BTN_tiltPlus, buttonWidth, buttonHeight);

                ////////////////////////
                //     UI Reset Cam   //
                ////////////////////////

                UI_BTN_resetCamera = new Button(getApplicationContext());
                UI_BTN_resetCamera.setText("Cam Reset");
                UI_BTN_resetCamera.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_resetCamera.setX(buttonLeftMargin);
                UI_BTN_resetCamera.setY(UI_SecondSectionHeight + buttonHeight - group);
                UI_BTN_resetCamera.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        UnitySendMessage("Main Camera", "ResetCamera", "-");
                    }
                });
                getUnityFrameLayout().addView(UI_BTN_resetCamera, buttonWidth, buttonHeight);

                //////////////////////
                //     UI TILT -    //
                //////////////////////

                UI_BTN_tiltMinus = new Button(getApplicationContext());
                UI_BTN_tiltMinus.setText("Tilt -");
                UI_BTN_tiltMinus.setX(buttonLeftMargin);
                UI_BTN_tiltMinus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_tiltMinus.setY(UI_SecondSectionHeight + (buttonHeight - group) * 2);
                UI_BTN_tiltMinus.setOnTouchListener(new View.OnTouchListener() {
                    private Handler mHandler;
                    @Override public boolean onTouch(View v, MotionEvent event) {
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (mHandler != null) return true;
                                mHandler = new Handler();
                                mHandler.postDelayed(mAction, 0);
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mHandler == null) return true;
                                mHandler.removeCallbacks(mAction);
                                mHandler = null;
                                break;
                        }
                        return false;
                    }
                    Runnable mAction = new Runnable() {
                        @Override public void run() {
                            UnitySendMessage("Main Camera", "TiltCamera", "+");
                            mHandler.postDelayed(this, 30);
                        }
                    };
                });

                getUnityFrameLayout().addView(UI_BTN_tiltMinus, buttonWidth, buttonHeight);

                //////////////////////////
                //     UI Add Layers    //
                //////////////////////////

                UI_BTN_addLayers = new Button(getApplicationContext());
                UI_BTN_addLayers.setText("Add Layers");
                UI_BTN_addLayers.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_addLayers.setX(buttonLeftMargin);
                UI_BTN_addLayers.setY(UI_ThirdSectionHeight);
                UI_BTN_addLayers.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        UnitySendMessage("Main Camera", "ClippingPlanes", "-");
                    }
                });
                getUnityFrameLayout().addView(UI_BTN_addLayers, buttonWidth, buttonHeight);

                /////////////////////////////
                //     UI Remove Layers    //
                /////////////////////////////

                UI_BTN_removeLayers = new Button(getApplicationContext());
                UI_BTN_removeLayers.setText("Remove Layers");
                UI_BTN_removeLayers.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_removeLayers.setX(buttonLeftMargin);
                UI_BTN_removeLayers.setY(UI_ThirdSectionHeight + buttonHeight - group);
                UI_BTN_removeLayers.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        UnitySendMessage("Main Camera", "ClippingPlanes", "+");
                    }
                });
                getUnityFrameLayout().addView(UI_BTN_removeLayers, buttonWidth, buttonHeight);

                /////////////////////////////
                //     UI Add Risk Area    //
                /////////////////////////////

                UI_BTN_addRiskArea = new Button(getApplicationContext());
                UI_BTN_addRiskArea.setText("Add A Risk Area");
                UI_BTN_addRiskArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_addRiskArea.setX(buttonLeftMargin);
                UI_BTN_addRiskArea.setY(UI_FourthSectionHeight);
                UI_BTN_addRiskArea.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        UnitySendMessage("Main Camera", "addCube", "");
                    }
                });
                getUnityFrameLayout().addView(UI_BTN_addRiskArea, buttonWidth, buttonHeight);

                ////////////////////////
                //     UI Export      //
                ////////////////////////

                UI_BTN_export = new Button(getApplicationContext());
                UI_BTN_export.setText("Export");
                UI_BTN_export.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_export.setX(width - 200);
                UI_BTN_export.setY(UI_firstSectionHeight);
                UI_BTN_export.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        UnitySendMessage("Main Camera", "exportCubes", "");
                    }
                });
                getUnityFrameLayout().addView(UI_BTN_export, buttonWidth, buttonHeight);

                //////////////////////
                //     UI Delete    //
                //////////////////////

                UI_BTN_delete = new Button(getApplicationContext());
                UI_BTN_delete.setText("Delete");
                UI_BTN_delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_delete.setX(width-200);
                UI_BTN_delete.setY(height-50);
                UI_BTN_delete.setVisibility(View.GONE);
                getUnityFrameLayout().addView(UI_BTN_delete, buttonWidth + 20, buttonHeight);
            }
        });
    }

    public void deleteDialog(){
        if(!isDialoging){
            isDialoging = true;
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            UnitySendMessage("Main Camera", "deleteRiskArea", "");
                            isDialoging = false;
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            isDialoging = false;
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getUnityFrameLayout().getContext());
            builder.setMessage("Are you sure ?\n\n Risk area will be deleted permanently").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    public void EditBlockMessage(){
        Toast.makeText(getApplicationContext(),"Risk Area editing will be blocked while camera is tilt",Toast.LENGTH_SHORT).show();
    }

    public void EditUnblockMessage(){
        Toast.makeText(getApplicationContext(),"Risk Area editing is available",Toast.LENGTH_SHORT).show();
    }

    public void updateControlsToUnityFrame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;

                UI_BTN_delete.setX(width-200);
                UI_BTN_delete.setY(height-50);

                UI_BTN_export.setX(width - 200);
                UI_BTN_export.setY(UI_firstSectionHeight);

                UI_BTN_HeightPlus.setY(height - buttonHeight - 10);
                UI_BTN_HeightMinus.setY(height - group - 10);
                UI_BTN_cubeUp.setY(height - buttonHeight - 10);
                UI_BTN_cubeDown.setY(height - group - 10);
            }
         });
    }

    static public void updateRiskAreaJsonPath(String str){
        riskAreaJsonPath = str;
    }
}
