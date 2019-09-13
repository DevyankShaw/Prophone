package com.example.devyankshaw.checking;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.devyankshaw.checking.OneTapLock.TapLock;
import com.example.devyankshaw.checking.WallpaperChange.Wallpaper;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences, prefsForDevices;
    private Switch swtSwitch, swtAlarm, swtAutoStart;
    private SharedPreferences settings;
    public static final String PREFS_NAME = "MyPrefsFile";
    public final static int REQUEST_CODE = 123;

    private TextView txtSecurity, txtWallpaper,txtOneTapLock;

    //Check GPS Status true/false
    public static boolean checkGPSStatus(Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return statusOfGPS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSecurity = findViewById(R.id.txtSecurity);
        txtWallpaper = findViewById(R.id.txtWallpaper);
        txtOneTapLock = findViewById(R.id.txtOneTapLock);

        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        prefsForDevices = PreferenceManager.getDefaultSharedPreferences(this);

        swtSwitch = findViewById(R.id.swtService);
        swtAlarm = findViewById(R.id.swtAlarm);
        swtAutoStart = findViewById(R.id.swtAutoStart);

        settings = getSharedPreferences(PREFS_NAME, 0);
        boolean lockValue = settings.getBoolean("switchKeyLock", false);
        swtSwitch.setChecked(lockValue);

        boolean alarmValue = settings.getBoolean("SWITCH_ALARM", false);
        swtAlarm.setChecked(alarmValue);

        boolean autoStartValue = settings.getBoolean("SWITCH_START", false);
        swtAutoStart.setChecked(autoStartValue);

        //This is for the first time to block Set Password when user clicks
        if(settings.getInt("switchFirst", 0) == 101){
            txtSecurity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, AddSecurityActivity.class));
                }
            });

            txtWallpaper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, Wallpaper.class));
                }
            });

            txtOneTapLock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, TapLock.class));
                }
            });
        }

        //Dialog for network and GPS is not available
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Network and GPS Alert!!!");
        alertDialog.setMessage("Internet Connection and GPS is disabled in your device. Kindly enable it.");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.create();

        if(!(isNetworkAvailable() && checkGPSStatus(this))){
            alertDialog.show();
        }


        //Opens the MainActivity as soon as the user gives the overlay permission
        final Handler handler = new Handler();
        Runnable checkOverlaySetting = new Runnable() {
            @Override
            @TargetApi(23)
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                }
                if (Settings.canDrawOverlays(MainActivity.this)) {
                    //You have the permission, re-launch MainActivity
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        };

        //Overlay Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            /*If the android version is >= API 23/Marshmallow && if the settings of the device doesn't allow/gives permission
                to overlay the widget to others then this if blocks executes
             */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            Uri.parse("package:" + getPackageName());
            startActivityForResult(intent, REQUEST_CODE);
            handler.postDelayed(checkOverlaySetting, 1000);
            finish();
            //startActivity(intent);
        } else {
            floatTheViewOnTheScreen();
        }



        //For XIAOMI devices
        if(Build.BRAND.equalsIgnoreCase("xiaomi") ) {

            if(!prefsForDevices.getBoolean("oneTimeDeviceXiaomi", false)) {
                // run your one time code
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                startActivity(intent);

                SharedPreferences.Editor editor = prefsForDevices.edit();
                editor.putBoolean("oneTimeDeviceXiaomi", true);
                editor.commit();
            }
        }


        //For OPPO devices
        if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {

            if(!prefsForDevices.getBoolean("oneTimeDeviceOppo", false)) {
                // run your one time code
                try {
                    Intent intent = new Intent();
                    intent.setClassName("com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                    startActivity(intent);
                } catch (Exception e) {
                    try {
                        Intent intent = new Intent();
                        intent.setClassName("com.oppo.safe",
                                "com.oppo.safe.permission.startup.StartupAppListActivity");
                        startActivity(intent);

                    } catch (Exception ex) {
                        try {
                            Intent intent = new Intent();
                            intent.setClassName("com.coloros.safecenter",
                                    "com.coloros.safecenter.startupapp.StartupAppListActivity");
                            startActivity(intent);
                        } catch (Exception exx) {

                        }
                    }
                }

                SharedPreferences.Editor editor = prefsForDevices.edit();
                editor.putBoolean("oneTimeDeviceOppo", true);
                editor.commit();
            }
        }


        //AutoStart permission for VIVO devices
        if(Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
            if(!prefsForDevices.getBoolean("oneTimeDeviceVivo", false)) {
                // run your one time code
                autoLaunchVivo(MainActivity.this);
                SharedPreferences.Editor editor = prefsForDevices.edit();
                editor.putBoolean("oneTimeDeviceVivo", true);
                editor.commit();
            }
        }


    }

    //Check Internet Connection Status true/false
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void floatTheViewOnTheScreen() {
        swtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //This if is executed when the app is opened for the first time
                    if (settings.getBoolean("my_first_time", true)) {
                        startActivity(new Intent(MainActivity.this, AddSecurityActivity.class));

                            SharedPreferences.Editor editorLock = settings.edit();
                            editorLock.putInt("switchFirst", 101);
                            editorLock.commit();


                            settings.edit().putBoolean("my_first_time", false).commit();
                    }
                    else if(settings.getInt("switchFirst", 0) == 101){
                        Intent intent = new Intent(MainActivity.this, LockService.class);
                        //intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                        ContextCompat.startForegroundService(MainActivity.this, intent.setAction(Intent.ACTION_SCREEN_OFF));
                    }

                }else {
                    stopService(new Intent(MainActivity.this,LockService.class));
                }
                //Saving the state of the switch i.e swtLock
                    SharedPreferences.Editor editorLock = settings.edit();
                    editorLock.putBoolean("switchKeyLock", isChecked);
                    editorLock.commit();
            }
        });

        swtAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    settings.edit().putBoolean("ENABLE_ALARM", true).commit();
                }else{
                    settings.edit().putBoolean("ENABLE_ALARM", false).commit();
                }
                //Saving the state of the alarm switch
                settings.edit().putBoolean("SWITCH_ALARM", isChecked).commit();
            }
        });

        swtAutoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    settings.edit().putBoolean("AUTO_START", true).commit();
                }else{
                    settings.edit().putBoolean("AUTO_START", false).commit();
                }
                //Saving the state of the alarm switch
                settings.edit().putBoolean("SWITCH_START", isChecked).commit();
            }
        });

    }


    private static void autoLaunchVivo(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                context.startActivity(intent);
            } catch (Exception ex) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName("com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                    context.startActivity(intent);
                } catch (Exception exx) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
