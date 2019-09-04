package com.example.devyankshaw.checking;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.devyankshaw.checking.HomeKeyListener.HomeWatcher;
import com.example.devyankshaw.checking.HomeKeyListener.OnHomePressedListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.accounts.AccountManager.KEY_PASSWORD;
import static com.example.devyankshaw.checking.MainActivity.PREFS_NAME;

public class LockScreenPin extends AppCompatActivity implements View.OnClickListener {


    private SharedPreferences prefs, preferences;
    private boolean switchAlarmTapped;

    private int  pinWrongStatus;
    private MediaPlayer mp;

    // To keep track of activity's window focus
    boolean currentFocus;

    // To keep track of activity's foreground/background status
    boolean isPaused;

    Handler collapseNotificationHandler;
    Runnable runnable;

    public static boolean notificationPanel;
    public ConstraintLayout layoutPin;
    private Button btnSumbit;
    private EditText edtPin;
    private ImageView imgClose;
    private Button btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine, btnZero;

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_POWER));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN|
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_pin);

        layoutPin = findViewById(R.id.layoutPin);

        //Displaying the wallpaper image that is set by the user
        SharedPreferences preferences = getSharedPreferences("WallpaperImage",MODE_PRIVATE);
        int imageReference = preferences.getInt("imageReference", 0);
        if(preferences.getBoolean("isImage",false)==true){
            layoutPin.setBackgroundResource(imageReference);
        }

        mp = MediaPlayer.create(LockScreenPin.this, R.raw.siren);
        preferences = getSharedPreferences(PREFS_NAME, 0);
        switchAlarmTapped = preferences.getBoolean("ENABLE_ALARM", false);


        btnOne = findViewById(R.id.btnOne);
        btnTwo = findViewById(R.id.btnTwo);
        btnThree = findViewById(R.id.btnThree);
        btnFour = findViewById(R.id.btnFour);
        btnFive = findViewById(R.id.btnFive);
        btnSix = findViewById(R.id.btnSix);
        btnSeven = findViewById(R.id.btnSeven);
        btnEight = findViewById(R.id.btnEight);
        btnNine = findViewById(R.id.btnNine);
        btnZero = findViewById(R.id.btnZero);

        edtPin = findViewById(R.id.edtPin);

        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);
        btnFive.setOnClickListener(this);
        btnSix.setOnClickListener(this);
        btnSeven.setOnClickListener(this);
        btnEight.setOnClickListener(this);
        btnNine.setOnClickListener(this);
        btnZero.setOnClickListener(this);



        imgClose = findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtPin.getText().clear();
            }
        });

        btnSumbit = findViewById(R.id.btnSubmit);
        btnSumbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPin.getText().toString().trim();

                if(pwd.isEmpty()){
                    edtPin.setError("Pin required");
                    edtPin.requestFocus();
                    return;
                }

                prefs = PreferenceManager.getDefaultSharedPreferences(LockScreenPin.this);
                int pin = prefs.getInt(KEY_PASSWORD, 0000);
                if(Integer.parseInt(edtPin.getText().toString()) == pin) {
                    if (Build.MANUFACTURER.equalsIgnoreCase("realme") ||
                            Build.MANUFACTURER.equalsIgnoreCase("oppo") ||
                            Build.MANUFACTURER.equalsIgnoreCase("gionee") ||
                            Build.MANUFACTURER.equalsIgnoreCase("micromax") ||
                            Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
                        finishAffinity(); //kill other activities
                    } else {
                        finish();
                    }

                    if(switchAlarmTapped) {
                        pinWrongStatus = 0;
                        if (mp.isPlaying()) {
                            mp.stop();
                        }
                    }
                }else{
                    FancyToast.makeText(LockScreenPin.this, "Wrong Pin!!!", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();

                    if(switchAlarmTapped) {
                        pinWrongStatus++;
                        if (pinWrongStatus == 3) {
                            FancyToast.makeText(LockScreenPin.this, "You exceeded maximum attempts\n  \t\t\tPlease enter correct pin", FancyToast.LENGTH_LONG, FancyToast.WARNING, true).show();
                            mp.setLooping(true);
                            mp.start();
                        }
                    }
                }
            }
        });

        //Blocks Home Button Pressed
        HomeWatcher mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                // do something here...
//                Toast.makeText(LockScreenPin.this, "Home Button Pressed", Toast.LENGTH_LONG).show();
                Intent notificationIntent = new Intent(LockScreenPin.this, LockScreenPin.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(LockScreenPin.this, 0, notificationIntent, 0);
                try
                {
                    pendingIntent.send();
                }
                catch (PendingIntent.CanceledException e)
                {
                    e.printStackTrace();
                }
            }
            @Override
            public void onHomeLongPressed() {
//                Toast.makeText(LockScreenPin.this, "Home Button Long Pressed", Toast.LENGTH_LONG).show();

            }
        });
        mHomeWatcher.startWatch();


    }



    //Blocked back button pressed when the pin/password screen is active
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // Not calling **super**, disables back button in current screen.
    }

    //Block volume button
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {


            return true;
        } else {

            return super.dispatchKeyEvent(event);
        }
    }

    //Completely removes the app from the recent task when the user gives his correct pin to unlock the screen
    @Override
    public void finish() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        }
        else {
            super.finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
            if (!hasFocus) {//If activity/window lost it's focus i.e any system dialog appears etc then this if block executes

                // Method that handles loss of window focus
                    collapseNow();

                // Close every kind of system dialog
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(closeDialog);//Sends signal/message  to the system


            }


        }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Activity's been paused
        isPaused = true;

        //Block recent task when password/pin activity is open
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Activity's been resumed
        isPaused = false;


    }

    public void collapseNow() {


            // Initialize 'collapseNotificationHandler'
            if (collapseNotificationHandler == null) {
                collapseNotificationHandler = new Handler();
            }

            // If window focus has been lost && activity is not in a paused state
            // Its a valid check because showing of notification panel
            // steals the focus from current activity's window, but does not
            // 'pause' the activity
            if (!currentFocus && !isPaused) {


                // Post a Runnable with some delay - currently set to 300 ms
                collapseNotificationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Use reflection to trigger a method from 'StatusBarManager'

                        Object statusBarService = getSystemService("statusbar");
                        Class<?> statusBarManager = null;

                        try {
                            statusBarManager = Class.forName("android.app.StatusBarManager");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        Method collapseStatusBar = null;

                        try {

                            // Prior to API 17, the method to call is 'collapse()'
                            // API 17 onwards, the method to call is `collapsePanels()`

                            if (Build.VERSION.SDK_INT > 16) {
                                collapseStatusBar = statusBarManager.getMethod("collapsePanels");
                            } else {
                                collapseStatusBar = statusBarManager.getMethod("collapse");
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }

                        collapseStatusBar.setAccessible(true);

                        try {
                            collapseStatusBar.invoke(statusBarService);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        // Check if the window focus has been returned
                        // If it hasn't been returned, post this Runnable again
                        // Currently, the delay is 100 ms. You can change this
                        // value to suit your needs.
                        if (!currentFocus && !isPaused && !notificationPanel) {
                            collapseNotificationHandler.postDelayed(this, 100L);
                        }
                    }
                }, 300L);
            }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnOne:
                edtPin.append("1");
                break;
            case R.id.btnTwo:
                edtPin.append("2");
                break;
            case R.id.btnThree:
                edtPin.append("3");
                break;
            case R.id.btnFour:
                edtPin.append("4");
                break;
            case R.id.btnFive:
                edtPin.append("5");
                break;
            case R.id.btnSix:
                edtPin.append("6");
                break;
            case R.id.btnSeven:
                edtPin.append("7");
                break;
            case R.id.btnEight:
                edtPin.append("8");
                break;
            case R.id.btnNine:
                edtPin.append("9");
                break;
            case R.id.btnZero:
                edtPin.append("0");
                break;

        }
    }
}
