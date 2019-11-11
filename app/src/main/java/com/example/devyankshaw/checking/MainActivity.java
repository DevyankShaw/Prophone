package com.example.devyankshaw.checking;


import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.devyankshaw.checking.OneTapLock.TapLock;
import com.example.devyankshaw.checking.SecurityPassword.AddSecurityActivity;
import com.example.devyankshaw.checking.WallpaperChange.Wallpaper;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private SharedPreferences preferences, prefsForDevices;
    private LinearLayout layoutHide;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private SharedPreferences settings;
    public static final String PREFS_NAME = "MyPrefsFile";
    public final static int REQUEST_CODE = 123;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    Runnable checkOverlaySetting;
    Handler handler;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM
    };
    private static final String TAG = "MainActivity";
    public static GoogleAccountCredential mCredential;
    public String fileName = "";
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    URL newUrl;
    private Switch swtSwitch, swtAlarm, swtAutoStart, swtTakeSelfie, swtDisplay;
    private TextView txtSecurity, txtWallpaper, txtOneTapLock, txtViewSelfie;
    private TextView txtLocation;

    //Check GPS Status true/false
    public static boolean checkGPSStatus(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return statusOfGPS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        layoutHide = findViewById(R.id.layoutHide);

        txtSecurity = findViewById(R.id.txtSecurity);
        txtWallpaper = findViewById(R.id.txtWallpaper);
        txtOneTapLock = findViewById(R.id.txtOneTapLock);
        txtViewSelfie = findViewById(R.id.txtViewSelfie);


        checkLocationPermission();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        prefsForDevices = PreferenceManager.getDefaultSharedPreferences(this);

        swtSwitch = findViewById(R.id.swtService);
        swtAlarm = findViewById(R.id.swtAlarm);
        swtAutoStart = findViewById(R.id.swtAutoStart);
        swtTakeSelfie = findViewById(R.id.swtTakeSelfie);
        swtDisplay = findViewById(R.id.swtDisplay);

        settings = getSharedPreferences(PREFS_NAME, 0);
        boolean lockValue = settings.getBoolean("switchKeyLock", false);
        swtSwitch.setChecked(lockValue);

        boolean alarmValue = settings.getBoolean("SWITCH_ALARM", false);
        swtAlarm.setChecked(alarmValue);

        boolean autoStartValue = settings.getBoolean("SWITCH_START", false);
        swtAutoStart.setChecked(autoStartValue);

        boolean takeSelfieValue = settings.getBoolean("SWITCH_SELFIE", false);
        swtTakeSelfie.setChecked(takeSelfieValue);

        boolean displayValue = settings.getBoolean("SWITCH_DISPLAY", false);
        swtDisplay.setChecked(displayValue);

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

        if(!(isNetworkAvailable() && checkGPSStatus(this))) {
            alertDialog.show();
        }

        getResultsFromApi();

        //Opens the MainActivity as soon as the user gives the overlay permission
        handler = new Handler();
        checkOverlaySetting = new Runnable() {
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
                }
            }
        };

        //Overlay Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            /*If the android version is >= API 23/Marshmallow && if the settings of the device doesn't allow/gives permission
                to overlay the widget to others then this if blocks executes*/
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
            handler.postDelayed(checkOverlaySetting, 4000);
            finish();

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                }else{
                    floatTheViewOnTheScreen();
                }
            }
        }



        /*//For XIAOMI devices
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
        }*/


    }

    private void chooseAccount() {
        if (com.example.devyankshaw.checking.Utils.checkPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(), com.example.devyankshaw.checking.Utils.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.GET_ACCOUNTS}, Utils.REQUEST_PERMISSION_GET_ACCOUNTS);
        }
    }

    private void getResultsFromApi() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_PERMISSION_GET_ACCOUNTS:
                chooseAccount();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
        }
    }



    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String url = "https://www.google.com/maps/dir/?api=1";
        //String origin = "&origin=" + "YourLocation";
        String destination = "&destination=" + location.getLatitude() + "," + location.getLongitude();
        try {
            newUrl = new URL(url + destination);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        txtLocation = findViewById(R.id.txt_location);
        txtLocation.setMovementMethod(LinkMovementMethod.getInstance());
        txtLocation.setText(newUrl.toString());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //This is for the first time to block Set Password when user clicks
        if(settings.getInt("switchFirst", 0) == 101){
            layoutHide.setAlpha(1.0f);
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

            txtViewSelfie.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SelfieActivity.class));
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

            swtTakeSelfie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        settings.edit().putBoolean("ENABLE_SELFIE", true).commit();
                    }else{
                        settings.edit().putBoolean("ENABLE_SELFIE", false).commit();
                    }
                    //Saving the state of the alarm switch
                    settings.edit().putBoolean("SWITCH_SELFIE", isChecked).commit();
                }
            });

            swtDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        settings.edit().putBoolean("ENABLE_DISPLAY", true).commit();
                    }else{
                        settings.edit().putBoolean("ENABLE_DISPLAY", false).commit();
                    }
                    //Saving the state of the alarm switch
                    settings.edit().putBoolean("SWITCH_DISPLAY", isChecked).commit();
                }
            });

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
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //This if is executed when the app is opened for the first time
                    if (settings.getBoolean("my_first_time", true)) {
                        layoutHide.setAlpha(1.0f);
                        startActivity(new Intent(MainActivity.this, AddSecurityActivity.class));

                        SharedPreferences.Editor editorLock = settings.edit();
                        editorLock.putInt("switchFirst", 101);
                        editorLock.commit();


                        settings.edit().putBoolean("my_first_time", false).commit();
                    }
                    else if(settings.getInt("switchFirst", 0) == 101) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //moveTaskToBack(true);
    }
}
