package com.example.devyankshaw.checking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class LockScreen extends AppCompatActivity {


    // To keep track of activity's window focus
    boolean currentFocus;

    // To keep track of activity's foreground/background status
    boolean isPaused;

    Handler collapseNotificationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_lock_screen);

        /*InnerRecevier innerReceiver = new InnerRecevier();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(innerReceiver, intentFilter);*/

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



                //Toast.makeText(LockScreen.this, "Your LongPress Power Button", Toast.LENGTH_SHORT).show();

            }


        }

    @Override
    protected void onPause() {
        super.onPause();

        // Activity's been paused
        isPaused = true;
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
                            collapseStatusBar = statusBarManager .getMethod("collapsePanels");
                        } else {
                            collapseStatusBar = statusBarManager .getMethod("collapse");
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
                    if (!currentFocus && !isPaused) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }

                }
            }, 300L);
        }
    }

    /*// for home listen
    class InnerRecevier extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        // home is Pressed
                        Intent intentone = new Intent(context.getApplicationContext(), LockScreen.class);
                        intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentone);
//                        Toast.makeText(context, "Home Button Pressed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }*/


}
