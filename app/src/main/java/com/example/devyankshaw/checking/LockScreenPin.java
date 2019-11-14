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
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import com.example.devyankshaw.checking.HomeKeyListener.HomeWatcher;
import com.example.devyankshaw.checking.HomeKeyListener.OnHomePressedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.accounts.AccountManager.KEY_PASSWORD;
import static com.example.devyankshaw.checking.MainActivity.PREFS_NAME;

public class LockScreenPin extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences prefs, preferencesGlobal;
    private boolean switchAlarmTapped;

    DatabaseReference databaseReference;
    private static final String TAG = "LockScreenPin";
    private MediaPlayer mp;
    public String fileName = "";
    Configuration configuration;

    // To keep track of activity's window focus
    boolean currentFocus;

    // To keep track of activity's foreground/background status
    boolean isPaused;

    Handler collapseNotificationHandler;
    Runnable runnable;
    private String usersEmail;

    private SurfaceTexture surfaceTexture;
    private Context mContext;
    private int pinWrongStatus;

    private boolean notificationPanelPin;
    public ConstraintLayout layoutPin;
    private Button btnSumbit;
    private EditText edtPin;
    private TextView txtPin,txtPinStatus;
    private ImageView imgClose;
    private Button btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine, btnZero;
    private FrameLayout frameLayout1,frameLayout2,frameLayout3;
    private GridLayout gridLayout;

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_POWER));

    // method for converting base64 to bitmap
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            new SavePhotoTaskPin().execute(data);

            //Closing camera
            camera.stopFaceDetection();
            camera.stopPreview();
            camera.release();

        }
    };

    //Completely removes the app from the recent task when the user gives his correct pin to unlock the screen
    @Override
    public void finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        } else {
            super.finish();
        }
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
                    notificationPanelPin = preferencesGlobal.getBoolean("notificationPanel", false);
                    if (!currentFocus && !isPaused && !notificationPanelPin) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }
                }
            }, 300L);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_pin);

        configuration = new Configuration()
                .apiKey("72217fce13742590a242a560493ea081-1df6ec32-dbbe568a")
                .from("Test account", "devyankshaw68@gmail.com");


        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        mContext = getApplicationContext();
        surfaceTexture = new SurfaceTexture(0);

        FullScreencall();

        layoutPin = findViewById(R.id.layoutPin);

        //Displaying the wallpaper image that is set by the user
        SharedPreferences preferences = getSharedPreferences("WallpaperImage", MODE_PRIVATE);
        if (preferences.getBoolean("isImage", false) == true && preferences.getBoolean("isImageChooser", false) == false) {
            int imageReference = preferences.getInt("imageReference", 0);
            layoutPin.setBackgroundResource(imageReference);
        }
        //Displaying the wallpaper image that is set by the user from the gallery
        if (preferences.getBoolean("isImage", false) == false && preferences.getBoolean("isImageChooser", false) == true) {
            String imageChooser = preferences.getString("imageChooser", "");
            BitmapDrawable background = new BitmapDrawable(this.getResources(), decodeBase64(imageChooser));
            layoutPin.setBackground(background);
        }

        mp = MediaPlayer.create(LockScreenPin.this, R.raw.siren);
        preferencesGlobal = getSharedPreferences(PREFS_NAME, 0);
        switchAlarmTapped = preferencesGlobal.getBoolean("ENABLE_ALARM", false);


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
        txtPin = findViewById(R.id.textView_Pin);
        txtPinStatus = findViewById(R.id.textView_Pin_Status);

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

        frameLayout1 = findViewById(R.id.frameLayout1);
        frameLayout2 = findViewById(R.id.frameLayout2);
        frameLayout3 = findViewById(R.id.frameLayout3);
        gridLayout = findViewById(R.id.gridLayout);

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

                if (pwd.isEmpty()) {
                    edtPin.setError("Pin required");
                    edtPin.requestFocus();
                    return;
                }

                prefs = PreferenceManager.getDefaultSharedPreferences(LockScreenPin.this);
                int pin = prefs.getInt(KEY_PASSWORD, 0000);
                if (Integer.parseInt(edtPin.getText().toString()) == pin) {
                    if (Build.MANUFACTURER.equalsIgnoreCase("realme") ||
                            Build.MANUFACTURER.equalsIgnoreCase("oppo") ||
                            Build.MANUFACTURER.equalsIgnoreCase("gionee") ||
                            Build.MANUFACTURER.equalsIgnoreCase("micromax") ||
                            Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
                        finishAffinity(); //kill other activities
                    } else {
                        finish();
                    }

                    if (switchAlarmTapped) {
                        pinWrongStatus = 0;
                        if (mp.isPlaying()) {
                            mp.stop();
                        }
                    }
                } else {
                    //Toast.makeText(LockScreenPin.this, "Wrong Pin!!!", Toast.LENGTH_SHORT).show();
                    txtPinStatus.setText("Wrong Pin!!!");
                    pinWrongStatus++;

                    if (pinWrongStatus == 3) {

                        if (preferencesGlobal.getBoolean("ENABLE_SELFIE", false)) {
                            takePictire();
                        }

                        if (switchAlarmTapped) {
                            mp.setLooping(true);
                            mp.start();
                        }

                        //new SendEmail().execute();

                        if (preferencesGlobal.getBoolean("ENABLE_DISPLAY", false)) {
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference uidRef = databaseReference.child(uid);
                            uidRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String number = dataSnapshot.child("number").getValue().toString();
                                    txtPin.setText("Please hand over the device to " + name + "." + "\n" + number + "\nCall Now!");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        //Toast.makeText(mContext, "You exceeded maximum attempts\n  \t\t\tPlease enter correct password", Toast.LENGTH_SHORT).show();
                        txtPinStatus.setText("You have exceeded maximum attempts\n Please enter correct password");
                        pinWrongStatus = 0;
                    }

                }
            }
        });

        //Blocks Home Button Pressed
        blockHomeButton();


    }

    private void getToDetails() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference uidRef = databaseReference.child(uid);
        uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersEmail = dataSnapshot.child("email").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class SendEmail extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            getToDetails();
            Mail.using(configuration)
                    .to(usersEmail)
                    .subject("This message has an text attachment")
                    .text("Please find attached a file.")
                    .multipart()
                    .attachment(new File("/path/to/image.jpg"))
                    .build()
                    .send();
            return null;
        }
    }

    public Uri getImageUri(Bitmap inImage) {
        String FILENAME = BitMapToString(inImage);
        String PATH = "/mnt/sdcard/" + FILENAME;
        File f = new File(PATH);
        Uri yourUri = Uri.fromFile(f);
        return yourUri;
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
            boolean hasCamera = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
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

    public void blockHomeButton() {
        HomeWatcher mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                // do something here...
//                Toast.makeText(LockScreenPin.this, "Home Button Pressed", Toast.LENGTH_LONG).show();
                Intent notificationIntent = new Intent(LockScreenPin.this, LockScreenPin.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(LockScreenPin.this, 0, notificationIntent, 0);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

    public void FullScreencall() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
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

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    class SavePhotoTaskPin extends AsyncTask<byte[], Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           frameLayout1.setVisibility(View.INVISIBLE);
           frameLayout2.setVisibility(View.INVISIBLE);
           gridLayout.setVisibility(View.GONE);
           frameLayout3.setVisibility(View.GONE);
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
            SharedPreferences sharedPreferences = getSharedPreferences("TAKE_SELFIE", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("imageSelfie", encodeTobase64(bitmap));
            editor.commit();

            frameLayout1.setVisibility(View.VISIBLE);
            frameLayout2.setVisibility(View.VISIBLE);
            gridLayout.setVisibility(View.VISIBLE);
            frameLayout3.setVisibility(View.VISIBLE);
        }
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
}
