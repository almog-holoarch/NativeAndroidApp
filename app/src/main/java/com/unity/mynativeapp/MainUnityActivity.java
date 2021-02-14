package com.unity.mynativeapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;
import com.company.product.OverrideUnityActivity;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.opencsv.CSVReader;
import com.unity3d.player.UnityPlayer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainUnityActivity extends OverrideUnityActivity {

    private final String TAG = "AlmogMainUnityActivity";
    static private String riskAreaJsonPath;
    private Database db;

    //////////////////////////
    //      SETTINGS        //
    //////////////////////////

    private static final int number_of_points_to_export = 8;
    private static final int dimension = 3;
    private static final int bytes_per_float = 4;
    private int cubes_counter;
    private int current_group_cubes_counter;

    UsbSerialDevice serial;
    String chosenGroup = "";

    ///////////////////////
    //      DEBUG        //
    ///////////////////////

    TextView x_text = null;
    TextView y_text = null;
    TextView z_text = null;
    TextView delta_x_text = null;
    TextView delta_y_text = null;
    TextView delta_z_text = null;

    float max_x, max_y, max_z = Float.NEGATIVE_INFINITY;
    float min_x, min_y, min_z = Float.POSITIVE_INFINITY;

    boolean debug_created = false;

    ////////////////////
    //      UI        //
    ////////////////////

    private boolean isChanged;
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

    Button UI_BTN_choose_group;
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

    void isChangesTrue() {
        isChanged = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database();

        String cmdLine = updateUnityCommandLineArguments(getIntent().getStringExtra("unity"));
        getIntent().putExtra("unity", cmdLine);
        mUnityPlayer = new UnityPlayer(this, this);
        setContentView(com.unity3d.player.R.layout.activity_unity);
        FrameLayout frameLayout = (FrameLayout) findViewById(com.unity3d.player.R.id.unity_player_layout);
        frameLayout.addView(mUnityPlayer.getView());
        mUnityPlayer.requestFocus();

        isChanged = false;

        String lasFilePath = Database.getPath();
        String offset_vector = Database.getOffsetX() + "," + Database.getOffsetY() + "," + Database.getOffsetZ();
        String rotate_vector = Database.getRotationX() + "," + Database.getRotationY() + "," + Database.getRotationZ();

        UnitySendMessage("Main Camera", "update3DModelPath", lasFilePath);
        UnitySendMessage("Main Camera", "updateRiskAreaJsonPath", riskAreaJsonPath);

        Log.d(TAG,"offset from android = " + offset_vector);
        Log.d(TAG,"rotate from android = " + rotate_vector);

        UnitySendMessage("Main Camera", "updateOffsetVector", offset_vector);
        UnitySendMessage("Main Camera", "updateRotateVector", rotate_vector);
        UnitySendMessage("Main Camera", "start", "");

        ////////////////////
        //     UI Back    //
        ////////////////////

        UI_BTN_back = new Button(getApplicationContext());
        UI_BTN_back.setText(getString(R.string.BUTTON_U_back));
        UI_BTN_back.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_back.setX(buttonLeftMargin);
        UI_BTN_back.setY(UI_firstSectionHeight);
        UI_BTN_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                UnitySendMessage("Main Camera", "checkIfChanged", "");
                if (isChanged) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {

                                case DialogInterface.BUTTON_POSITIVE:
                                    UnitySendMessage("Main Camera", "jsonUpdate", "");
                                    Log.d(TAG, "closing unity and saving changes");
                                    showMainActivity("");
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    Log.d(TAG, "closing unity");
                                    showMainActivity("");
                                    break;

                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getUnityFrameLayout().getContext());
                    builder
                            .setMessage(getString(R.string.MESSAGE_save_changes) + "\n\n ")
                            .setPositiveButton(getString(R.string.BUTTON_yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.BUTTON_no), dialogClickListener)
                            .setNeutralButton(getString(R.string.BUTTON_cancel), dialogClickListener)
                            .setCancelable(true)
                            .show();

                } else {
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
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(600, 240);
        lp.setMargins(width - 650, height - 200, 0, 0);
        iv.setLayoutParams(lp);
        getUnityFrameLayout().addView(iv);

        ////////////////////////////////////////
        //     Adding hidden cubs controls    //
        ////////////////////////////////////////

        /////////////////////////////
        //     UI Height Plus      //
        /////////////////////////////

        UI_BTN_HeightPlus = new Button(getApplicationContext());
        UI_BTN_HeightPlus.setText(getString(R.string.BUTTON_U_height_plus));
        UI_BTN_HeightPlus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_HeightPlus.setX(buttonLeftMargin);
        UI_BTN_HeightPlus.setY(height - buttonHeight - 10);
        UI_BTN_HeightPlus.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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
                @Override
                public void run() {
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
        UI_BTN_HeightMinus.setText(getString(R.string.BUTTON_U_height_minus));
        UI_BTN_HeightMinus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_HeightMinus.setX(buttonLeftMargin);
        UI_BTN_HeightMinus.setY(height - group - 10);
        UI_BTN_HeightMinus.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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
                @Override
                public void run() {
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
        UI_BTN_cubeUp.setText(getString(R.string.BUTTON_U_move_up));
        UI_BTN_cubeUp.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_cubeUp.setX(buttonLeftMargin + buttonHeight + 0.5f * spacer);
        UI_BTN_cubeUp.setY(height - buttonHeight - 10);
        UI_BTN_cubeUp.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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
                @Override
                public void run() {
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
        UI_BTN_cubeDown.setText(getString(R.string.BUTTON_U_move_down));
        UI_BTN_cubeDown.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
        UI_BTN_cubeDown.setX(buttonLeftMargin + buttonHeight + 0.5f * spacer);
        UI_BTN_cubeDown.setY(height - group - 10);
        UI_BTN_cubeDown.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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
                @Override
                public void run() {
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

    public void vibrate() {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
    }

    public void showDeleteButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UI_BTN_delete.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideDeleteButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UI_BTN_delete.setVisibility(View.GONE);
            }
        });
    }

    public void hideCubeControls() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UI_BTN_HeightPlus != null) {
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
                if (UI_BTN_HeightPlus != null) {
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
                UI_BTN_tiltPlus.setText(getString(R.string.BUTTON_U_tilt_plus));
                UI_BTN_tiltPlus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_tiltPlus.setX(buttonLeftMargin);
                UI_BTN_tiltPlus.setY(UI_SecondSectionHeight);
                UI_BTN_tiltPlus.setOnTouchListener(new View.OnTouchListener() {
                    private Handler mHandler;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
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
                        @Override
                        public void run() {
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
                UI_BTN_resetCamera.setText(getString(R.string.BUTTON_U_cam_reset));
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
                UI_BTN_tiltMinus.setText(getString(R.string.BUTTON_U_tilt_minus));
                UI_BTN_tiltMinus.setX(buttonLeftMargin);
                UI_BTN_tiltMinus.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_tiltMinus.setY(UI_SecondSectionHeight + (buttonHeight - group) * 2);
                UI_BTN_tiltMinus.setOnTouchListener(new View.OnTouchListener() {
                    private Handler mHandler;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
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
                        @Override
                        public void run() {
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
                UI_BTN_addLayers.setText(getString(R.string.BUTTON_U_add_layers));
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
                UI_BTN_removeLayers.setText(getString(R.string.BUTTON_U_remove_layers));
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
                UI_BTN_addRiskArea.setText(getString(R.string.BUTTON_U_new_risk_area));
                UI_BTN_addRiskArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_addRiskArea.setX(buttonLeftMargin);
                UI_BTN_addRiskArea.setY(UI_FourthSectionHeight);
                UI_BTN_addRiskArea.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        Map<String, Group> groupsMap = db.getGroupsMap();;

                        if(groupsMap==null || groupsMap.size() == 0){
                            Toast.makeText(getApplicationContext(), getString(R.string.TOAST_cant_add_cube_cause_no_groups), Toast.LENGTH_LONG).show();

                        } else{

                            Set<String> keys = groupsMap.keySet();
                            final String[] items = keys.toArray(new String[keys.size()]);
                            Arrays.sort(items);

                            final ArrayList selectedItems = new ArrayList();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainUnityActivity.this);
                            builder.setTitle("Please choose groups for this risk area");

//                            builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                        selectedItems.add(which);
//
//                                }
//                            });

                            builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    if (isChecked) {
                                        selectedItems.add(which);
                                    } else if (selectedItems.contains(which)) {
                                        selectedItems.remove(Integer.valueOf(which));
                                    }
                                }
                            });

                            builder.setPositiveButton(getString(R.string.BUTTON_done), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StringBuilder arg = new StringBuilder();
                                    int i = 0;
                                    for (Object index : selectedItems)
                                    {
                                        i++;
                                        arg.append(items[(int)index]);
                                        if(i < selectedItems.size()){
                                            arg.append(",");
                                        }
                                    }
                                    UnitySendMessage("Main Camera", "addCube", arg.toString());
                                }
                            });

                            builder.setNegativeButton(getString(R.string.BUTTON_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            builder.show();
                        }

                    }
                });
                getUnityFrameLayout().addView(UI_BTN_addRiskArea, buttonWidth, buttonHeight);

                ////////////////////////
                //     UI Export      //
                ////////////////////////

                UI_BTN_export = new Button(getApplicationContext());
                UI_BTN_export.setText(getString(R.string.BUTTON_U_export));
                UI_BTN_export.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_export.setX(width - 200);
                UI_BTN_export.setY(UI_firstSectionHeight);
                UI_BTN_export.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        if(cubes_counter == 0){
                            Toast.makeText(MainUnityActivity.this, getString(R.string.TOAST_no_risk_area_to_export), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(chosenGroup.equals("")){
                            Toast.makeText(MainUnityActivity.this, getString(R.string.TOAST_chose_group_to_export), Toast.LENGTH_LONG).show();
                            return;
                        }

                        Map<String, Group> groupsMap = db.getGroupsMap();;

                        if(groupsMap==null || groupsMap.size() == 0){
                            Toast.makeText(getApplicationContext(), getString(R.string.TOAST_error), Toast.LENGTH_LONG).show();

                        } else{

//                            Set<String> keys = groupsMap.keySet();
//                            final String[] items = keys.toArray(new String[keys.size()]);
//                            Arrays.sort(items);
//
//                            final String[] chosen_item = new String[1];
//                            chosen_item[0] = items[0];
//
//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainUnityActivity.this);
//                            builder.setTitle("Please choose A group to export");
//
//                            builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    chosen_item[0] = items[which];
//
//                                }
//                            });
//
//                            builder.setPositiveButton(getString(R.string.BUTTON_done), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    UnitySendMessage("Main Camera", "exportCubes", chosen_item[0]);
//                                }
//                            });
//
//                            builder.setNegativeButton(getString(R.string.BUTTON_cancel), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//
//                            builder.show();
                            UnitySendMessage("Main Camera", "exportCubes", chosenGroup);
                        }
                    }
                });
                getUnityFrameLayout().addView(UI_BTN_export, buttonWidth, buttonHeight);

                //////////////////////////////
                //     UI choose group      //
                //////////////////////////////

                UI_BTN_choose_group = new Button(getApplicationContext());
                UI_BTN_choose_group.setText(getString(R.string.BUTTON_U_choose_group));
                UI_BTN_choose_group.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_choose_group.setX(width - 400);
                UI_BTN_choose_group.setY(UI_firstSectionHeight);
                UI_BTN_choose_group.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        //TODO: check if changed
                        UnitySendMessage("Main Camera", "jsonUpdate", "");
                        chooseGroup();
//                        UnitySendMessage("Main Camera", "checkIfChanged", "");
//                        if (isChanged) {
//
//                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    switch (which) {
//
//                                        case DialogInterface.BUTTON_POSITIVE:
//                                            UnitySendMessage("Main Camera", "jsonUpdate", "");
//                                            chooseGroup();
//                                            break;
//
//                                        case DialogInterface.BUTTON_NEGATIVE:
//                                            break;
//                                    }
//                                }
//                            };
//
//                            AlertDialog.Builder builder = new AlertDialog.Builder(getUnityFrameLayout().getContext());
//                            builder
//                                    .setMessage(getString(R.string.MESSAGE_save_changes) + "\n\n ")
//                                    .setPositiveButton(getString(R.string.BUTTON_yes), dialogClickListener)
//                                    .setNegativeButton(getString(R.string.BUTTON_no), dialogClickListener)
//                                    .setNeutralButton(getString(R.string.BUTTON_cancel), dialogClickListener)
//                                    .setCancelable(true)
//                                    .show();
//
//                        } else {
//                            chooseGroup();
//                        }
                    }
                });
                getUnityFrameLayout().addView(UI_BTN_choose_group, buttonWidth, buttonHeight);

                //////////////////////
                //     UI Delete    //
                //////////////////////

                UI_BTN_delete = new Button(getApplicationContext());
                UI_BTN_delete.setText(getString(R.string.BUTTON_U_delete));
                UI_BTN_delete.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize);
                UI_BTN_delete.setX(width - 200);
                UI_BTN_delete.setY(height - 50);
                UI_BTN_delete.setVisibility(View.GONE);
                getUnityFrameLayout().addView(UI_BTN_delete, buttonWidth + 20, buttonHeight);
            }
        });
    }

    public void chooseGroup(){
        Map<String, Group> groupsMap = db.getGroupsMap();;

        if(groupsMap==null || groupsMap.size() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.TOAST_no_groups), Toast.LENGTH_LONG).show();

        } else{

            Group all = new Group();
            all.setName("All Groups");
            groupsMap.put("All Groups",all);

            Set<String> keys = groupsMap.keySet();
            final String[] items = keys.toArray(new String[keys.size()]);
            Arrays.sort(items);

            final String[] chosen_item = new String[1];
            chosen_item[0] = items[0];

            AlertDialog.Builder builder = new AlertDialog.Builder(MainUnityActivity.this);
            builder.setTitle("Please choose A group to display");

            builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    chosen_item[0] = items[which];

                }
            });

            builder.setPositiveButton(getString(R.string.BUTTON_done), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    chosenGroup = chosen_item[0];
                    UnitySendMessage("Main Camera", "showCubes", chosen_item[0]);
                }
            });

            builder.setNegativeButton(getString(R.string.BUTTON_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }
    }

    public void deleteDialog() {
        if (!isDialoging) {
            isDialoging = true;
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            UnitySendMessage("Main Camera", "decreaseCubeCounter", "");
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
            builder.setMessage(getString(R.string.MESSAGE_are_you_sure) + "\n\n" + getString(R.string.MESSAGE_riskArea_delete)).setPositiveButton(getString(R.string.BUTTON_yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.BUTTON_no), dialogClickListener).show();
        }
    }

    public void EditBlockMessage() {
        Toast.makeText(getApplicationContext(), getString(R.string.TOAST_edit_block), Toast.LENGTH_SHORT).show();
    }

    public void EditUnblockMessage() {
        Toast.makeText(getApplicationContext(), getString(R.string.TOAST_edit_enable), Toast.LENGTH_SHORT).show();
    }

    public void updateControlsToUnityFrame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;

                UI_BTN_delete.setX(width - 200);
                UI_BTN_delete.setY(height - 50);

                UI_BTN_export.setX(width - 200);
                UI_BTN_export.setY(UI_firstSectionHeight);

                UI_BTN_HeightPlus.setY(height - buttonHeight - 10);
                UI_BTN_HeightMinus.setY(height - group - 10);
                UI_BTN_cubeUp.setY(height - buttonHeight - 10);
                UI_BTN_cubeDown.setY(height - group - 10);
            }
        });
    }

    static public void updateRiskAreaJsonPath(String str) {
        riskAreaJsonPath = str;
    }

    void exportPoints() {

        if(current_group_cubes_counter == 0){
            Toast.makeText(MainUnityActivity.this, getString(R.string.TOAST_no_GROUP_risk_area_to_export), Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCOUNTER = " + current_group_cubes_counter);

        // DEBUG - TODO : remove
        if(debug_created)
        {
            clearDebugText();
            max_x = max_y = max_z = Float.MIN_VALUE;
            min_x = min_y = min_z = Float.MAX_VALUE;
        } else
        {
            addDebugText();
            debug_created = true;
        }

//       //// START
        // port setup & open
        UsbDeviceConnection usbConnection;

        String ACTION_USB_PERMISSION = "com.almog.usbpermission";
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        UsbDevice device;

        if (!usbDevices.isEmpty()) {

            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                if (!usbManager.hasPermission(device)) {

                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                }

                else {
                    usbConnection = usbManager.openDevice(device);
                    serial = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);

                    serial.open();

                    if (!serial.isOpen()) {
                        Log.d(TAG, "serial is not open.");
                    }

                    serial.setBaudRate(115200);
                    serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serial.setParity(UsbSerialInterface.PARITY_NONE);
                    serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                    Log.d(TAG, "sending hello to device " + device.getDeviceId() + " " + device.getProductId() + " " + device.getDeviceName());
                    Log.d(TAG, "counter = " + current_group_cubes_counter);

                    byte[] arr = new byte[4];
                    arr[0] = '#';
                    arr[1] = arr[2] = arr[3] =  '*';
                    serial.write(arr);

                    final String[] cmd = {""};

                    UsbSerialInterface.UsbReadCallback cb = new UsbSerialInterface.UsbReadCallback() {
                        @Override
                        public void onReceivedData(byte[] arg0) {

                            String received = new String(arg0, StandardCharsets.UTF_8);

                            //split by delimiter
                            String[] splitter = received.split("\\*",2);

                            // did not reached delimiter
                            if(splitter.length == 1){
                                if(!splitter[0].equals("")) cmd[0] += splitter[0];
                            }

                            // reached delimiter
                            else{
                                if(!splitter[0].equals("")) cmd[0] += splitter[0];
                                runCommand(cmd[0]);
                                cmd[0] = splitter[1];
                            }
                        }
                    };

                    serial.read(cb);
                }
            }
        }

    }

    void runCommand(final String cmd){

        // Sending anchor and risk areas to hardware
        if (cmd.startsWith("OK")) {

            try {

                // send anchor's position
                sendAnchorPosition();

                // read csv file for exported risk areas and send to hardware
                String export_path = Environment.getExternalStorageDirectory() + File.separator + "Android/data/com.unity.mynativeapp" + File.separator + "files" + File.separator + "export.csv";
                CSVReader reader = new CSVReader(new FileReader(export_path));
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(TAG, "device ID is: " + cmd);
                            Toast.makeText(MainUnityActivity.this, getString(R.string.TOAST_exporting_risk_areas_to_device) + cmd.substring(2), Toast.LENGTH_LONG).show();
                        }
                    });

                    short[] shorts = new short[number_of_points_to_export * dimension];

                    if (nextLine[0].length() > 0) {

                        for (int i = 0; i < number_of_points_to_export * dimension; i++) {
                            shorts[i] = floatToShort(Float.parseFloat(nextLine[i])*100); // * 100 for meter to cm
                        }

                        byte[]  buf = ShortToByte_Twiddle_Method(shorts);

                        serial.write(buf);
                    }
                }

            } catch (IOException e) {
                Toast.makeText(MainUnityActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }

        // Updates debug mode on tablet (blue dot real time position)
        else if(cmd.startsWith("P")) {
            Log.d(TAG, "string is: " + cmd);

            // update DEBUG text TODO: remove
            updateDebugText(cmd);
            UnitySendMessage("Main Camera", "devicePositionUpdate",  cmd.substring(2));
        }

    }

    void sendAnchorPosition(){

        Point3D first  = new Point3D(2.1f, 2.55f, -0.1f);
        Point3D second = new Point3D(2f,   2.55f, 0.073f);
        Point3D third  = new Point3D(1.8f, 2.55f, 0.073f);
        Point3D fourth = new Point3D(1.7f, 2.55f, -0.1f);
        Point3D fifth  = new Point3D(1.8f, 2.55f, -0.273f);
        Point3D sixth  = new Point3D(2f,   2.55f, -0.273f);

        byte[] arr = new byte[42];

        // opening
        arr[0] = '#';

        // counter
        arr[1] = (byte) ((current_group_cubes_counter & 0xFF00) >> 8);
        arr[2] = (byte)  (current_group_cubes_counter & 0x00FF);

        // first
        arr[3] =  (byte) ((floatToShort(first.x * 100) & 0xFF00) >> 8);
        arr[4] =  (byte)  (floatToShort(first.x * 100) & 0x00FF);
        arr[5] =  (byte) ((floatToShort(first.y * 100) & 0xFF00) >> 8);
        arr[6] =  (byte)  (floatToShort(first.y * 100) & 0x00FF);
        arr[7] =  (byte) ((floatToShort(first.z * 100) & 0xFF00) >> 8);
        arr[8] =  (byte)  (floatToShort(first.z * 100) & 0x00FF);
        // second
        arr[9] =  (byte) ((floatToShort(second.x * 100) & 0xFF00) >> 8);
        arr[10] = (byte)  (floatToShort(second.x * 100) & 0x00FF);
        arr[11] = (byte) ((floatToShort(second.y * 100) & 0xFF00) >> 8);
        arr[12] = (byte)  (floatToShort(second.y * 100) & 0x00FF);
        arr[13] = (byte) ((floatToShort(second.z * 100) & 0xFF00) >> 8);
        arr[14] = (byte)  (floatToShort(second.z * 100) & 0x00FF);
        // third
        arr[15] = (byte) ((floatToShort(third.x * 100) & 0xFF00) >> 8);
        arr[16] = (byte)  (floatToShort(third.x * 100) & 0x00FF);
        arr[17] = (byte) ((floatToShort(third.y * 100) & 0xFF00) >> 8);
        arr[18] = (byte)  (floatToShort(third.y * 100) & 0x00FF);
        arr[19] = (byte) ((floatToShort(third.z * 100) & 0xFF00) >> 8);
        arr[20] = (byte)  (floatToShort(third.z * 100) & 0x00FF);
        // fourth
        arr[21] = (byte) ((floatToShort(fourth.x * 100) & 0xFF00) >> 8);
        arr[22] = (byte)  (floatToShort(fourth.x * 100) & 0x00FF);
        arr[23] = (byte) ((floatToShort(fourth.y * 100) & 0xFF00) >> 8);
        arr[24] = (byte)  (floatToShort(fourth.y * 100) & 0x00FF);
        arr[25] = (byte) ((floatToShort(fourth.z * 100) & 0xFF00) >> 8);
        arr[26] = (byte)  (floatToShort(fourth.z * 100) & 0x00FF);
        // fifth
        arr[27] = (byte) ((floatToShort(fifth.x * 100) & 0xFF00) >> 8);
        arr[28] = (byte)  (floatToShort(fifth.x * 100) & 0x00FF);
        arr[29] = (byte) ((floatToShort(fifth.y * 100) & 0xFF00) >> 8);
        arr[30] = (byte)  (floatToShort(fifth.y * 100) & 0x00FF);
        arr[31] = (byte) ((floatToShort(fifth.z * 100) & 0xFF00) >> 8);
        arr[32] = (byte)  (floatToShort(fifth.z * 100) & 0x00FF);
        // sixth
        arr[33] = (byte) ((floatToShort(sixth.x * 100) & 0xFF00) >> 8);
        arr[34] = (byte)  (floatToShort(sixth.x * 100) & 0x00FF);
        arr[35] = (byte) ((floatToShort(sixth.y * 100) & 0xFF00) >> 8);
        arr[36] = (byte)  (floatToShort(sixth.y * 100) & 0x00FF);
        arr[37] = (byte) ((floatToShort(sixth.z * 100) & 0xFF00) >> 8);
        arr[38] = (byte)  (floatToShort(sixth.z * 100) & 0x00FF);

        // ending
        arr[39] = arr[40] = arr[41] =  '*';

        //send
        serial.write(arr);
    }

    void updateDebugText(String s){

        final String[] split = s.split(",");

        String d1 = split[1], d2 = split[2] ,d3 = split[3];
        String d4 = split[4], d5 = split[5] ,d6 = split[6];

        float f1 = Float.parseFloat(d1)/100, f2 = Float.parseFloat(d2)/100, f3 = Float.parseFloat(d3)/100;
        float f4 = Float.parseFloat(d4)/100, f5 = Float.parseFloat(d5)/100, f6 = Float.parseFloat(d6)/100;

        float max_range = 4;

        if(f1 > max_range || f2 > max_range || f3 > max_range || f4 > max_range || f5 > max_range || f6 > max_range)
        {
            runOnUiThread(new Runnable() {
                public void run() {
                    x_text.setText(getString(R.string.OTHER_lost_signal));
                    y_text.setText(getString(R.string.OTHER_lost_signal));
                    z_text.setText(getString(R.string.OTHER_lost_signal));
                }
            });
        }

        else
        {
            final float x = Float.parseFloat(split[7]) / 100;
            final float y = Float.parseFloat(split[8]) / 100;
            final float z = Float.parseFloat(split[9]) / 100;

            if(x < min_x) min_x = x;
            if(y < min_y) min_y = y;
            if(z < min_z) min_z = z;

            if(x > max_x) max_x = x;
            if(y > max_y) max_y = y;
            if(z > max_z) max_z = z;

            runOnUiThread(new Runnable() {
                public void run() {
                    x_text.setText(String.format("%.2f", x));
                    y_text.setText(String.format("%.2f", y));
                    z_text.setText(String.format("%.2f", z));
                    delta_x_text.setText(String.format("%.2f", max_x - min_x));
                    delta_y_text.setText(String.format("%.2f", max_y - min_y));
                    delta_z_text.setText(String.format("%.2f", max_z - min_z));
                }
            });
        }
    }

    void clearDebugText(){
        runOnUiThread(new Runnable() {
            public void run() {
                x_text.setText("");
                y_text.setText("");
                z_text.setText("");
                delta_x_text.setText("");
                delta_y_text.setText("");
                delta_z_text.setText("");
            }
        });
    }

    void addDebugText(){

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // ADD TEXT FOR X DEBUG
        x_text = new TextView(getApplicationContext());
        x_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize + 10);
        x_text.setX(width - 600);
        x_text.setY(height - 150);
        delta_x_text = new TextView(getApplicationContext());
        delta_x_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize + 10);
        delta_x_text.setX(width - 600);
        delta_x_text.setY(height - 100);

        // ADD TEXT FOR Y DEBUG
        y_text = new TextView(getApplicationContext());
        y_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize + 10);
        y_text.setX(width - 400);
        y_text.setY(height - 150);
        delta_y_text = new TextView(getApplicationContext());
        delta_y_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize + 10);
        delta_y_text.setX(width - 400);
        delta_y_text.setY(height - 100);

        // ADD TEXT FOR Z DEBUG
        z_text = new TextView(getApplicationContext());
        z_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize + 10);
        z_text.setX(width - 200);
        z_text.setY(height - 150);
        delta_z_text = new TextView(getApplicationContext());
        delta_z_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonsTestSize + 10);
        delta_z_text.setX(width - 200);
        delta_z_text.setY(height - 100);

        runOnUiThread(new Runnable() {
            public void run() {
                getUnityFrameLayout().addView(x_text, buttonWidth + 20, buttonHeight);
                getUnityFrameLayout().addView(y_text, buttonWidth + 20, buttonHeight);
                getUnityFrameLayout().addView(z_text, buttonWidth + 20, buttonHeight);
                getUnityFrameLayout().addView(delta_x_text, buttonWidth + 20, buttonHeight);
                getUnityFrameLayout().addView(delta_y_text, buttonWidth + 20, buttonHeight);
                getUnityFrameLayout().addView(delta_z_text, buttonWidth + 20, buttonHeight);

            }
        });

    }

    byte [] ShortToByte_Twiddle_Method(short [] input)
    {
        int short_index, byte_index;
        int iterations = input.length;

        int size = (input.length * 2) + 4;
        byte [] buffer = new byte[size];

        buffer[0] = '#';
        short_index = 0;
        byte_index = 1;

        for(/*NOP*/; short_index != iterations; /*NOP*/)
        {
            buffer[byte_index]     = (byte) ((input[short_index] & 0xFF00) >> 8);
            buffer[byte_index + 1] = (byte) (input[short_index] & 0x00FF);

            ++short_index; byte_index += 2;
        }

        buffer[size-3] = buffer[size-2] = buffer[size-1] = '*';

        return buffer;
    }

    public static short floatToShort(float x) {
        if (x < Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        }
        if (x > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        return (short) Math.round(x);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: dispose serials serial.close();
    }

    public void messageCantAddCubeIn3D(){
        Toast.makeText(getApplicationContext(), getString(R.string.TOAST_cant_add_cube_in_3D), Toast.LENGTH_SHORT).show();
    }


    public void increaseCubeCounter(){
        cubes_counter++;
    }

    public void decreaseCubeCounter(){
        cubes_counter--;
    }

    public void increaseCURRENTCubeCounter(){
        current_group_cubes_counter++;
        Log.d(TAG,"increasing current cubes counter");
    }

    public void zeroCURRENTCubeCounter(){
        current_group_cubes_counter = 0;
        Log.d(TAG,"reset current cubes counter");
    }


//    public static byte[] floatArrayToBytes(float[] d) {
//        byte[] r = new byte[d.length * bytes_per_float];
//        for (int i = 0; i < d.length; i++) {
//            byte[] s = floatToBytes(d[i]);
//            for (int j = 0; j < bytes_per_float; j++)
//                r[4 * i + j] = s[j];
//        }
//        return r;
//    }
//
//    public static byte[] floatToBytes(float d) {
//        int i = Float.floatToRawIntBits(d);
//        return intToBytes(i);
//    }
//
//    public static byte[] intToBytes(int v) {
//        byte[] r = new byte[bytes_per_float];
//        for (int i = 0; i < bytes_per_float; i++) {
//            r[i] = (byte) ((v >>> (i * 8)) & 0xFF);
//        }
//        return r;
//    }

}
