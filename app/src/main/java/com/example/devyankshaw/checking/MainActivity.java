package com.example.devyankshaw.checking;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Switch swtSwitch;
    private SharedPreferences settings;
    public static final String PREFS_NAME = "MyPrefsFile";
    public final static int REQUEST_CODE = 123;
    private static final int IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002;

    private TextView txtSetPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSetPassword = findViewById(R.id.txtSetPassword);
        txtSetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PasswordActivity.class));
            }
        });

        swtSwitch = findViewById(R.id.swtService);


        settings = getSharedPreferences(PREFS_NAME, 0);
        boolean lockValue = settings.getBoolean("switchKeyLock", false);
        swtSwitch.setChecked(lockValue);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            /*If the android version is >= API 23/Marshmallow && if the settings of the device doesn't allow/gives permission
                to overlay the widget to others then this if blocks executes
             */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            Uri.parse("package:" + getPackageName());
            startActivityForResult(intent, REQUEST_CODE);
            //startActivity(intent);
        } else {
            floatTheViewOnTheScreen();
        }

        if(Build.BRAND.equalsIgnoreCase("xiaomi") ) {

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);


        }



        if (Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
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
        }
        //
        if(Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
            autoLaunchVivo(MainActivity.this);
        }

        //Taking Ignore Battery Optimization permission
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                askIgnoreOptimization();
            } else {
                //accepted;
            }
        } else {

        }

//       Toast.makeText(this, Build.MANUFACTURER, Toast.LENGTH_LONG).show();


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

    private void askIgnoreOptimization() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST);
        }
    }


}
