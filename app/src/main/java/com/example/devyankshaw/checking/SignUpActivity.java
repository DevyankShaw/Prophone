package com.example.devyankshaw.checking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private EditText edtName, edtEmail, edtNumber, edtPassword, edtReenterPassword, edtOtp;
    private Button btnNumberVerify,btnSignUp,btnValidate,btnResendOTP;
    private String uCode;
    private LinearLayout otpLayout;
    private TextView txtSignIn;
    private ProgressBar progressBar;

    private String phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mResendToken = forceResendingToken;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                edtOtp.setText(code);
                uCode = code;
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    public static int getCurrentCountryCode(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = telephonyManager.getSimCountryIso().toUpperCase();
        return PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryIso);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        edtName = findViewById(R.id.name_sign_up);
        edtEmail = findViewById(R.id.email_sign_up);
        edtNumber = findViewById(R.id.number_sign_up);
        edtPassword = findViewById(R.id.password_sign_up);
        edtReenterPassword = findViewById(R.id.reenter_sign_up);
        edtOtp = findViewById(R.id.edt_otp);

        progressBar = findViewById(R.id.progressbar);
        otpLayout = findViewById(R.id.otp_layout);
        txtSignIn = findViewById(R.id.link_sign_in);

        btnNumberVerify = findViewById(R.id.btn_verify_number);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnValidate = findViewById(R.id.validate_otp);
        btnResendOTP = findViewById(R.id.resend_otp);

        btnNumberVerify.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        btnResendOTP.setOnClickListener(this);
        btnValidate.setOnClickListener(this);
        txtSignIn.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_verify_number:
                verifyNumber();
                break;
            case R.id.btn_sign_up:
                signUpProcess();
                break;
            case R.id.resend_otp:
                resendVerificationCode(phoneNumber,mResendToken);
            case R.id.validate_otp:
                validateOTP();
                break;
            case R.id.link_sign_in:
                finish();
                startActivity(new Intent(this,SignInActivity.class));
                break;
        }
    }

    private void validateOTP() {
        if(edtOtp.getText().toString().equals(uCode)){
            otpLayout.setVisibility(View.GONE);
            btnNumberVerify.setText("Verified");
            btnNumberVerify.setOnClickListener(null);
            edtNumber.setFocusable(false);
        }else{
            Toast.makeText(this, "Enter valid OTP", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyNumber() {

        String code = String.valueOf(getCurrentCountryCode(this));
        String number = edtNumber.getText().toString().trim();

        if (number.isEmpty()) {
            edtNumber.setError("Phone Number is required");
            edtNumber.requestFocus();
            return;
        }

        if (!Patterns.PHONE.matcher(number).matches() || number.length()!=10){
            edtNumber.setError("Please enter a valid phone number");
            edtNumber.requestFocus();
            return;
        }

        otpLayout.setVisibility(View.VISIBLE);

        phoneNumber = "+" + code + number;
        sendVerificationCode(phoneNumber);
    }

    private void sendVerificationCode(String number) {
        edtNumber.setFocusable(false);
        Toast.makeText(this, "Verification Code has been sent", Toast.LENGTH_SHORT).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBack
        );

    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallBack,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signUpProcess() {
        final String name = edtName.getText().toString().trim();
        final String email = edtEmail.getText().toString().trim();
        final String number = edtNumber.getText().toString().trim();
        final String password = edtPassword.getText().toString().trim();
        String rePassword  = edtReenterPassword.getText().toString().trim();


        //Validating the details of entered email and password
        if (name.isEmpty()) {
            edtName.setError("Name is required");
            edtName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            edtEmail.requestFocus();
            return;
        }



        if (number.isEmpty()) {
            edtNumber.setError("Phone Number is required");
            edtNumber.requestFocus();
            return;
        }

        if (!Patterns.PHONE.matcher(number).matches()){
            edtNumber.setError("Please enter a valid phone number");
            edtNumber.requestFocus();
            return;
        }

        if(!btnNumberVerify.getText().toString().equals("Verified")){
            edtNumber.setError("Phone Number is not verified (Please Verify)");
            edtNumber.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError("Minimum length of password should be 6");
            edtPassword.requestFocus();
            return;
        }

        if (rePassword.isEmpty()) {
            edtReenterPassword.setError("Password is required");
            edtReenterPassword.requestFocus();
            return;
        }

        if (!password.equals(rePassword)) {
            edtReenterPassword.setError("Password doesn't match");
            edtReenterPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {

                    Upload upload = new Upload(name,email,phoneNumber,password);
                    String uploadId = mAuth.getUid();
                    mDatabaseRef.child(uploadId).setValue(upload);

                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Signed Up successfully.Please check your email for verification", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }else{
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You already Signed Up", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
