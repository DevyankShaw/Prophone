package com.example.devyankshaw.checking.SecurityPassword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
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

public class PinActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean pinLockStatus;
    private SharedPreferences prefs, preferences;
    private EditText edtPinConfirm, edtPinReconfirm;
    private Button btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine, btnZero,
            btnContinuePinConfirm, btnContinuePinReconfirm;
    private TextView txtPinConfirm, txtPinReconfirm;
    private LinearLayout linearLayoutPinConfirm, linearLayoutPinReconfirm, linearLayoutPinButtonConfirm, linearLayoutPinButtonReconfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        preferences = getSharedPreferences(PREFS_NAME, 0);
        prefs = PreferenceManager.getDefaultSharedPreferences(PinActivity.this);


        btnContinuePinConfirm = findViewById(R.id.btnContinuePinConfirm);
        btnContinuePinReconfirm = findViewById(R.id.btnContinuePinReconfirm);

        linearLayoutPinConfirm = findViewById(R.id.linearLayoutPinConfirm);
        linearLayoutPinReconfirm = findViewById(R.id.linearLayoutReconfirm);

        txtPinConfirm = findViewById(R.id.txtPinConfirm);
        txtPinReconfirm = findViewById(R.id.txtPinReconfirm);


        edtPinConfirm = findViewById(R.id.edtPinConfirm);
        edtPinReconfirm = findViewById(R.id.edtPinReconfirm);

        linearLayoutPinButtonConfirm = findViewById(R.id.linearLayoutPinButtonConfirm);
        linearLayoutPinButtonReconfirm = findViewById(R.id.linearLayoutPinButtonReconfirm);


        btnOne = findViewById(R.id.btnOneChange);
        btnTwo = findViewById(R.id.btnTwoChange);
        btnThree = findViewById(R.id.btnThreeChange);
        btnFour = findViewById(R.id.btnFourChange);
        btnFive = findViewById(R.id.btnFiveChange);
        btnSix = findViewById(R.id.btnSixChange);
        btnSeven = findViewById(R.id.btnSevenChange);
        btnEight = findViewById(R.id.btnEightChange);
        btnNine = findViewById(R.id.btnNineChange);
        btnZero = findViewById(R.id.btnZeroChange);

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


        setTitle("Set Pin");


        btnContinuePinConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPinConfirm.getText().toString().trim();

                //Checking Validation
                if (pwd.isEmpty()) {
                    edtPinConfirm.setError("Pin required");
                    edtPinConfirm.requestFocus();
                    return;
                }

                if (pwd.length() < 4) {
                    edtPinConfirm.setError("Minimum length of pin should be 4");
                    edtPinConfirm.requestFocus();
                    return;
                }

                //If the above validations are successfully passed then below codes are executed
                linearLayoutPinConfirm.setVisibility(View.GONE);
                txtPinConfirm.setVisibility(View.INVISIBLE);
                linearLayoutPinReconfirm.setVisibility(View.VISIBLE);
                txtPinReconfirm.setVisibility(View.VISIBLE);
                linearLayoutPinButtonConfirm.setVisibility(View.INVISIBLE);
                linearLayoutPinButtonReconfirm.setVisibility(View.VISIBLE);
                setTitle("Confirm Pin");
            }
        });

        btnContinuePinReconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPinReconfirm.getText().toString().trim();

                //Checking Validation
                if (pwd.isEmpty()) {
                    edtPinReconfirm.setError("Renter pin");
                    edtPinReconfirm.requestFocus();
                    return;
                }

                //If the above validations are successfully passed then below codes are executed
                if(preferences.getBoolean("securityPinSet",false)) {
                    if (edtPinConfirm.getText().toString().equals(edtPinReconfirm.getText().toString())) {

                        SharedPreferences.Editor ed = prefs.edit();
                        ed.putInt(KEY_PASSWORD, Integer.parseInt(edtPinReconfirm.getText().toString()));
                        ed.commit();

                        FancyToast.makeText(PinActivity.this, "Pin Saved Successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

                        //Here shared preference is used to check the status that if user sets pin then pinLockScreen must be shown when screen is on
                        preferences.edit().putBoolean("pinLockStatus", true).commit();
                        preferences.edit().putBoolean("passwordLockStatus", false).commit();
                        preferences.edit().putBoolean("patternLockStatus", false).commit();

                        finish();
                    } else {
                        FancyToast.makeText(PinActivity.this, "Pin doesn't match", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                }

                //Activate the service when set up pin procedure is completed
                if(preferences.getBoolean("pin_first_done",true)) {
                    if (edtPinConfirm.getText().toString().equals(edtPinReconfirm.getText().toString())) {
                        if (preferences.getBoolean("pin_first_time", true)) {
                            Intent intent = new Intent(PinActivity.this, LockService.class);
                            ContextCompat.startForegroundService(PinActivity.this, intent.setAction(Intent.ACTION_SCREEN_OFF));
                            preferences.edit().putBoolean("pin_first_time", false).commit();


                            prefs.edit().putInt(KEY_PASSWORD, Integer.parseInt(edtPinReconfirm.getText().toString())).commit();
                            FancyToast.makeText(PinActivity.this, "Pin Saved Successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

                            /*Here if the user installs the app for the first time and if they set pin then next time when they set other securities
                            like pattern or password then the first time method in PatternActivity and PasswordActivity will not execute again*/
                            preferences.edit().putBoolean("pin_first_done", true).commit();
                            preferences.edit().putBoolean("password_first_done", false).commit();
                            preferences.edit().putBoolean("pattern_first_done", false).commit();

                            //Here shared preference is used to check the status that if user sets pin then pinLockScreen must be shown when screen is on
                            preferences.edit().putBoolean("pinLockStatus", true).commit();
                            preferences.edit().putBoolean("passwordLockStatus", false).commit();
                            preferences.edit().putBoolean("patternLockStatus", false).commit();


                            //Disable the use of first time code for pattern and password activity
                            preferences.edit().putBoolean("pattern_first_time", false).commit();
                            preferences.edit().putBoolean("securityPatternSet",true).commit();
                            preferences.edit().putBoolean("password_first_time", false).commit();
                            preferences.edit().putBoolean("securityPasswordSet",true).commit();


                            //This is used to block the back button until pin is set for the first time
                            preferences.edit().putBoolean("securityPinSet",true).commit();

                            finish();
                        }

                    }else {
                        FancyToast.makeText(PinActivity.this, "Pin doesn't match", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                }

            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOneChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("1");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("1");
                }
                break;
            case R.id.btnTwoChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("2");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("2");
                }
                break;
            case R.id.btnThreeChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("3");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("3");
                }
                break;
            case R.id.btnFourChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("4");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("4");
                }
                break;
            case R.id.btnFiveChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("5");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("5");
                }
                break;
            case R.id.btnSixChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("6");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("6");
                }
                break;
            case R.id.btnSevenChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("7");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("7");
                }
                break;
            case R.id.btnEightChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("8");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("8");
                }
                break;
            case R.id.btnNineChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("9");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("9");
                }
                break;
            case R.id.btnZeroChange:
                if (linearLayoutPinConfirm.getVisibility() == View.VISIBLE) {
                    edtPinConfirm.append("0");
                }
                if (linearLayoutPinReconfirm.getVisibility() == View.VISIBLE) {
                    edtPinReconfirm.append("0");
                }
                break;

        }
    }

    public void clearPinEditText(View view){
        switch (view.getId()){
            case R.id.clearPinConfirm:
                edtPinConfirm.getText().clear();
                break;
            case R.id.clearPinReconfirm:
                edtPinReconfirm.getText().clear();
                break;
        }
    }

    public void cancelPinActivity(View v){
        switch (v.getId()){
            case R.id.btnCancelPinConfirm:
                finish();
                break;
            case R.id.btnCancelPinReconfirm:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(preferences.getBoolean("securityPinSet",false)) {
            super.onBackPressed();
        }
    }
}
