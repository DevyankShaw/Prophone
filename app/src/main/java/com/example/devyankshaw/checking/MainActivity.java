package com.example.devyankshaw.checking;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import static android.accounts.AccountManager.KEY_PASSWORD;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private Switch swtSwitch;
    private SharedPreferences settings;
    public static final String PREFS_NAME = "MyPrefsFile";
    public final static int REQUEST_CODE = 123;

    private TextView txtSetPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        txtSetPassword = findViewById(R.id.txtSetPassword);
        swtSwitch = findViewById(R.id.swtService);

        settings = getSharedPreferences(PREFS_NAME, 0);
        boolean lockValue = settings.getBoolean("switchKeyLock", false);
        swtSwitch.setChecked(lockValue);

        //This is for the first time to block Set Password when user clicks
        if(settings.getInt("switchFirst", 0) == 101){
            txtSetPassword.setText("Change Password");
            txtSetPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, PasswordActivity.class));

                }
            });
        }

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

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);
            return;
        }


        //For OPPO devices
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
            return;
        }


        //For VIVO devices
        if(Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
            autoLaunchVivo(MainActivity.this);
            return;
        }


    }

    private void floatTheViewOnTheScreen() {
        swtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //This if is executed when the app is opened for the first time
                    if (settings.getBoolean("my_first_time", true)) {
                        startActivity(new Intent(MainActivity.this, PasswordActivity.class));

                            SharedPreferences.Editor editorLock = settings.edit();
                            editorLock.putInt("switchFirst", 101);
                            editorLock.commit();

                            txtSetPassword.setText("Change Password");
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
