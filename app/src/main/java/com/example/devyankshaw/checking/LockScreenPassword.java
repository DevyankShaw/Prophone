package com.example.devyankshaw.checking;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.devyankshaw.checking.HomeKeyListener.HomeWatcher;
import com.example.devyankshaw.checking.HomeKeyListener.OnHomePressedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.accounts.AccountManager.KEY_PASSWORD;
import static com.example.devyankshaw.checking.MainActivity.PREFS_NAME;

public class LockScreenPassword extends AppCompatActivity {

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_POWER));
    public RelativeLayout layoutPassword;
    FancyToast fancyToastWrong, fancyToastExceed;
    // To keep track of activity's window focus
    boolean currentFocus;
    // To keep track of activity's foreground/background status
    boolean isPaused;
    Handler collapseNotificationHandler;
    DatabaseReference databaseReference;
    private boolean notificationPanelPassword;
    private SharedPreferences prefs, preferencesGlobal;
    private boolean switchAlarmTapped;
    private EditText edtPassword;
    private Button btnSubmitPassword;
    private TextView txtPassword;
    private int passwordWrongStatus;
    private MediaPlayer mp;
    private SurfaceTexture surfaceTexture;
    private Context mContext;
    private TextView txtPasswordStatus;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            new SavePhotoTaskPassword().execute(data);

            //Closing camera
            camera.stopFaceDetection();
            camera.stopPreview();
            camera.release();

        }
    };

    // method for converting base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    // method for converting bitmap to base64
    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    /* Check if this device has a camera */
    private static Camera openFrontCamera(Context context) {
        try {
            boolean hasCamera = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
            if (hasCamera) {
                int cameraCount = 0;
                Camera cam = null;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                cameraCount = Camera.getNumberOfCameras();
                for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                    Camera.getCameraInfo(camIdx, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        try {
                            cam = Camera.open(camIdx);
                        } catch (RuntimeException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                return cam;
            }
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                |WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_password);

        mContext = getApplicationContext();
        surfaceTexture = new SurfaceTexture(0);
        txtPassword = findViewById(R.id.textView_Password);
        txtPasswordStatus = findViewById(R.id.textView_Password_Status);

        FullScreencall();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        layoutPassword = findViewById(R.id.layoutPassword);
        layoutPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //Hides the mobile keyboard whenever tapped on the blank/empty screen space
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        //Displaying the wallpaper image that is set by the user from the set of wallpapers
        SharedPreferences preferences = getSharedPreferences("WallpaperImage",MODE_PRIVATE);
        if(preferences.getBoolean("isImage",false)==true && preferences.getBoolean("isImageChooser",false)==false){
            int imageReference = preferences.getInt("imageReference", 0);
            layoutPassword.setBackgroundResource(imageReference);
        }
        //Displaying the wallpaper image that is set by the user from the gallery
        if(preferences.getBoolean("isImage",false)==false && preferences.getBoolean("isImageChooser",false)==true){
            String imageChooser = preferences.getString("imageChooser","");
            BitmapDrawable background = new BitmapDrawable(this.getResources(), decodeBase64(imageChooser));
            layoutPassword.setBackground(background);
        }

        mp = MediaPlayer.create(LockScreenPassword.this, R.raw.siren);
        preferencesGlobal = getSharedPreferences(PREFS_NAME, 0);
        switchAlarmTapped = preferencesGlobal.getBoolean("ENABLE_ALARM", false);

        //In order to show the soft keyboard as soon as the activity is opened
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        edtPassword = findViewById(R.id.edtPassword);
        btnSubmitPassword = findViewById(R.id.btnSubmitPassword);

        //To request focus when keyboard is opened
        edtPassword.requestFocus();

        btnSubmitPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPassword.getText().toString().trim();

                if(pwd.isEmpty()){
                    edtPassword.setError("Password required");
                    edtPassword.requestFocus();
                    return;
                }

                prefs = PreferenceManager.getDefaultSharedPreferences(LockScreenPassword.this);
                String password = prefs.getString(KEY_PASSWORD, "");
                if(edtPassword.getText().toString().equals(password)) {
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
                        passwordWrongStatus = 0;
                        if (mp.isPlaying()) {
                            mp.stop();
                        }
                    }
                }else{
                    //fancyToastWrong.makeText(LockScreenPassword.this, "Wrong Password!!!", fancyToastWrong.LENGTH_SHORT, fancyToastWrong.ERROR, true).show();
                    txtPasswordStatus.setText("Wrong Password!!!");
                    passwordWrongStatus++;
                    if (passwordWrongStatus == 3) {

                        if(preferencesGlobal.getBoolean("ENABLE_SELFIE",false)) {
                            takePictire();
                        }

                        if(switchAlarmTapped) {
                            mp.setLooping(true);
                            mp.start();
                        }

                        if (preferencesGlobal.getBoolean("ENABLE_DISPLAY", false)) {
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference uidRef = databaseReference.child(uid);
                            uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String number = dataSnapshot.child("number").getValue().toString();
                                    txtPassword.setText("Please hand over the device to " + name + "." + "\n" + number + "\nCall Now!");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        //fancyToastExceed.makeText(LockScreenPassword.this, "You exceeded maximum attempts\n  \t\t\tPlease enter correct password", fancyToastExceed.LENGTH_LONG, fancyToastExceed.WARNING, true).show();
                        txtPasswordStatus.setText("You have exceeded maximum attempts\n Please enter correct password");
                        passwordWrongStatus = 0;
                    }
                }
            }
        });

        //Blocks Home Button Pressed
        blockHomeButton();

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


        //Hide the Navigation bar
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

        executeDelayed();
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

                    @SuppressLint("WrongConstant") Object statusBarService = getSystemService("statusbar");
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
                    notificationPanelPassword = preferencesGlobal.getBoolean("notificationPanel",false);
                    if (!currentFocus && !isPaused && !notificationPanelPassword) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }
                }
            }, 300L);
        }
    }

    public void takePictire() {
        Camera cam = openFrontCamera(mContext);
        if (cam != null) {
            try {
                cam.setPreviewTexture(surfaceTexture);
                cam.startPreview();
                cam.startFaceDetection();
                cam.takePicture(null, null, mPicture);
            } catch (Exception ex) {
                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void blockHomeButton(){
        HomeWatcher mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                // do something here...
//                Toast.makeText(LockScreenPin.this, "Home Button Pressed", Toast.LENGTH_LONG).show();
                Intent notificationIntent = new Intent(LockScreenPassword.this, LockScreenPassword.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(LockScreenPassword.this, 0, notificationIntent, 0);
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
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

    public void FullScreencall() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });

    }

    private void executeDelayed() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // execute after 500ms
                hideNavBar();
            }
        }, 5);
    }

    private void hideNavBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    class SavePhotoTaskPassword extends AsyncTask<byte[], Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            edtPassword.setVisibility(View.INVISIBLE);
            btnSubmitPassword.setVisibility(View.GONE);
        }

        @Override
        protected Bitmap doInBackground(byte[]... bytes) {

            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inPreferredConfig = Bitmap.Config.RGB_565;
            Matrix mat = new Matrix();
            mat.postRotate(270);
            Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes[0]), null, bfo);
            Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), mat, true);
            ByteArrayOutputStream outstudentstreamOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                    outstudentstreamOutputStream);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            SharedPreferences sharedPreferences=getSharedPreferences("TAKE_SELFIE",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("imageSelfie", encodeTobase64(bitmap));
            editor.commit();

            edtPassword.setVisibility(View.VISIBLE);
            edtPassword.requestFocus();
            btnSubmitPassword.setVisibility(View.VISIBLE);
        }
    }
}
