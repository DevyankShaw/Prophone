package com.example.devyankshaw.checking.SecurityPassword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.devyankshaw.checking.LockService;
import com.example.devyankshaw.checking.R;
import com.shashank.sony.fancytoastlib.FancyToast;

import static android.accounts.AccountManager.KEY_PASSWORD;
import static com.example.devyankshaw.checking.MainActivity.PREFS_NAME;

public class PasswordActivity extends AppCompatActivity {

    public static boolean passwordLockStatus;
    private TextView txtPasswordSetConfirm, txtPasswordSetReconfirm;
    private EditText edtPasswordSetConfirm, edtPasswordSetReconfirm;
    private LinearLayout linearLayoutPasswordConfirm, linearLayoutPasswordReconfirm;
    private Button btnContinuePasswordConfirm, btnContinuePasswordReconfirm;
    private SharedPreferences prefs, preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        //In order to show the soft keyboard as soon as the activity is opened
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


        setTitle("Set Password");

        preferences = getSharedPreferences(PREFS_NAME, 0);
        prefs = PreferenceManager.getDefaultSharedPreferences(PasswordActivity.this);

        txtPasswordSetConfirm = findViewById(R.id.txtPasswordSetConfirm);
        txtPasswordSetReconfirm = findViewById(R.id.txtPasswordSetReconfirm);

        edtPasswordSetConfirm = findViewById(R.id.edtPasswordSetConfirm);
        edtPasswordSetReconfirm = findViewById(R.id.edtPasswordSetReconfirm);

        linearLayoutPasswordConfirm = findViewById(R.id.linearLayoutPasswordConfirm);
        linearLayoutPasswordReconfirm = findViewById(R.id.linearLayoutPasswordReconfirm);

        btnContinuePasswordConfirm = findViewById(R.id.btnContinuePasswordConfirm);
        btnContinuePasswordReconfirm = findViewById(R.id.btnContinuePasswordReconfirm);

        //To request focus when keyboard is opened
        edtPasswordSetConfirm.requestFocus();


        btnContinuePasswordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPasswordSetConfirm.getText().toString().trim();

                //Checking Validation
                if (pwd.isEmpty()) {
                    edtPasswordSetConfirm.setError("Password required");
                    edtPasswordSetConfirm.requestFocus();
                    return;
                }

                if (pwd.length() < 4) {
                    edtPasswordSetConfirm.setError("Minimum length of password should be 4");
                    edtPasswordSetConfirm.requestFocus();
                    return;
                }

                //If the above validations are successfully passed then below codes are executed
                txtPasswordSetConfirm.setVisibility(View.INVISIBLE);
                txtPasswordSetReconfirm.setVisibility(View.VISIBLE);
                edtPasswordSetConfirm.setVisibility(View.INVISIBLE);
                edtPasswordSetReconfirm.setVisibility(View.VISIBLE);
                linearLayoutPasswordConfirm.setVisibility(View.INVISIBLE);
                linearLayoutPasswordReconfirm.setVisibility(View.VISIBLE);
                setTitle("Confirm Password");
                edtPasswordSetReconfirm.requestFocus();
            }
        });

        btnContinuePasswordReconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPasswordSetReconfirm.getText().toString().trim();

                //Checking Validation
                if (pwd.isEmpty()) {
                    edtPasswordSetReconfirm.setError("Renter password");
                    edtPasswordSetReconfirm.requestFocus();
                    return;
                }

                //If the above validations are successfully passed then below codes are executed
                if(preferences.getBoolean("securityPasswordSet",false)) {
                    if (edtPasswordSetConfirm.getText().toString().equals(edtPasswordSetReconfirm.getText().toString())) {

                        SharedPreferences.Editor ed = prefs.edit();
                        ed.putString(KEY_PASSWORD, edtPasswordSetReconfirm.getText().toString());
                        ed.commit();

                        FancyToast.makeText(PasswordActivity.this, "Password Saved Successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

                        //Here shared preference is used to check the status that if user sets password then passwordLockScreen must be shown when screen is on
                        preferences.edit().putBoolean("passwordLockStatus", true).commit();
                        preferences.edit().putBoolean("pinLockStatus", false).commit();
                        preferences.edit().putBoolean("patternLockStatus", false).commit();


                        finish();
                    } else {
                        FancyToast.makeText(PasswordActivity.this, "Password doesn't match", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                    }
                }
                //Activate the service when set up password procedure is completed
                if(preferences.getBoolean("password_first_done",true)) {
                    if (edtPasswordSetConfirm.getText().toString().equals(edtPasswordSetReconfirm.getText().toString())) {
                        if (preferences.getBoolean("pin_first_time", true)) {
                            Intent intent = new Intent(PasswordActivity.this, LockService.class);
                            ContextCompat.startForegroundService(PasswordActivity.this, intent.setAction(Intent.ACTION_SCREEN_OFF));
                            preferences.edit().putBoolean("password_first_time", false).commit();

                            prefs.edit().putString(KEY_PASSWORD, edtPasswordSetReconfirm.getText().toString()).commit();
                            FancyToast.makeText(PasswordActivity.this, "Password Saved Successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

                            /*Here if the user installs the app for the first time and if they set password then next time when they set other securities
                            like pattern or pin then the first time method in PatternActivity and PinActivity will not execute again*/
                            preferences.edit().putBoolean("password_first_done", true).commit();
                            preferences.edit().putBoolean("pin_first_done", false).commit();
                            preferences.edit().putBoolean("pattern_first_done", false).commit();

                            //Here shared preference is used to check the status that if user sets password then passwordLockScreen must be shown when screen is on
                            preferences.edit().putBoolean("passwordLockStatus", true).commit();
                            preferences.edit().putBoolean("pinLockStatus", false).commit();
                            preferences.edit().putBoolean("patternLockStatus", false).commit();

                            //Disable the use of first time code for pin and pattern activity
                            preferences.edit().putBoolean("pin_first_time", false).commit();
                            preferences.edit().putBoolean("securityPinSet",true).commit();
                            preferences.edit().putBoolean("pattern_first_time", false).commit();
                            preferences.edit().putBoolean("securityPatternSet",true).commit();

                            //This is used to block the back button until password is set for the first time
                            preferences.edit().putBoolean("securityPasswordSet",true).commit();

                            finish();
                        }

                    }else {
                        FancyToast.makeText(PasswordActivity.this, "Password doesn't match", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                    }
                }

            }
        });


    }

    public void cancelPasswordActivity(View v){
        switch (v.getId()){
            case R.id.btnCancelPasswordConfirm:
                finish();
                break;
            case R.id.btnCancelPasswordReconfirm:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(preferences.getBoolean("securityPasswordSet",false)) {
            super.onBackPressed();
        }
    }

}
