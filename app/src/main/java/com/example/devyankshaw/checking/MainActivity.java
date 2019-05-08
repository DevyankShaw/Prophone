package com.example.devyankshaw.checking;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private Switch swtSwitch;
    private boolean checkLock;
    private SharedPreferences settings;
    public static final String PREFS_NAME = "MyPrefsFile";
    private Context context = MainActivity.this;
    public final static int REQUEST_CODE = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_FULLSCREEN|
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swtSwitch = findViewById(R.id.swtService);


        settings = getSharedPreferences(PREFS_NAME, 0);
        boolean lockValue = settings.getBoolean("switchKeyLock", false);
        swtSwitch.setChecked(lockValue);

//        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
//        registerReceiver(userPresentBroadcastReceiver, filter);
//
//        IntentFilter filter1 = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        registerReceiver(userPresentBroadcastReceiver, filter1);

//        IntentFilter filter1 = new IntentFilter(Intent.ACTION_USER_UNLOCKED);
//        registerReceiver(userPresentBroadcastReceiver, filter1);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)){
            /*If the android version is >= API 23/Marshmallow && if the settings of the device doesn't allow/gives permission
                to overlay the widget to others then this if blocks executes
             */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            Uri.parse("package:" + getPackageName());
            startActivityForResult(intent, REQUEST_CODE);
            //startActivity(intent);
        }else {
            floatTheViewOnTheScreen();
        }
    }

    private void floatTheViewOnTheScreen() {
        swtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Intent intent = new Intent(MainActivity.this, LockService.class);
                    //intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    ContextCompat.startForegroundService(MainActivity.this, intent.setAction(Intent.ACTION_SCREEN_OFF));

                }else {
                    stopService(new Intent(MainActivity.this,LockService.class));
                }
                //Saving the state of the switch i.e swtLock
                SharedPreferences.Editor editorLock = settings.edit();
                editorLock.putBoolean("switchKeyLock", isChecked);
                editorLock.commit();
            }
        });
    }

}
