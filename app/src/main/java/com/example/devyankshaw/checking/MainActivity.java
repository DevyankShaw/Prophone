package com.example.devyankshaw.checking;


import android.Manifest;
import android.annotation.TargetApi;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.devyankshaw.checking.OneTapLock.TapLock;
import com.example.devyankshaw.checking.SecurityPassword.AddSecurityActivity;
import com.example.devyankshaw.checking.WallpaperChange.Wallpaper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.MalformedURLException;
import java.net.URL;


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(this,SignInActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //moveTaskToBack(true);
    }
}
