package com.example.devyankshaw.checking;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int REQUEST_CODE = 123;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    Runnable checkOverlaySetting;
    Handler handler;
    private FirebaseAuth mAuth;
    private TextView txtSignUp;
    private EditText edtEmailSignIn,edtPasswordSignIn;
    private Button btnSignIn;
    private ProgressBar progressBarSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        edtEmailSignIn = findViewById(R.id.email_sign_in);
        edtPasswordSignIn = findViewById(R.id.password_sign_in);
        btnSignIn = findViewById(R.id.btn_sign_in);
        txtSignUp = findViewById(R.id.link_sign_up);
        progressBarSignIn = findViewById(R.id.progressBarSignIn);

        btnSignIn.setOnClickListener(this);
        txtSignUp.setOnClickListener(this);

        //Opens the MainActivity as soon as the user gives the overlay permission
        handler = new Handler();
        checkOverlaySetting = new Runnable() {
            @Override
            @TargetApi(23)
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                }
                if (Settings.canDrawOverlays(SignInActivity.this)) {
                    //You have the permission, re-launch MainActivity
                    Intent i = new Intent(SignInActivity.this, SignInActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
        };

        //Overlay Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(SignInActivity.this)) {
            /*If the android version is >= API 23/Marshmallow && if the settings of the device doesn't allow/gives permission
                to overlay the widget to others then this if blocks executes
             */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
            handler.postDelayed(checkOverlaySetting, 5000);
            finish();

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                }else{

                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == MY_CAMERA_REQUEST_CODE && Settings.canDrawOverlays(SignInActivity.this)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                }
            }
        }
    }

    private void userLogin(){
        final String email = edtEmailSignIn.getText().toString().trim();
        final String password = edtPasswordSignIn.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmailSignIn.setError("Email is required");
            edtEmailSignIn.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailSignIn.setError("Please enter a valid email");
            edtEmailSignIn.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPasswordSignIn.setError("Password is required");
            edtPasswordSignIn.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPasswordSignIn.setError("Minimum length of password should be 6");
            edtPasswordSignIn.requestFocus();
            return;
        }


        progressBarSignIn.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBarSignIn.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    if(!mAuth.getCurrentUser().isEmailVerified()){
                        Toast.makeText(SignInActivity.this, "Check your email and verify", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        return;
                    }else {
                        finish();
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//By adding this we will clear all the open activities or on the top of stack
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.link_sign_up:
                finish();
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
                break;
            case R.id.btn_sign_in:
                userLogin();
                break;
        }
    }
}
