package com.example.devyankshaw.checking.SecurityPassword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.devyankshaw.checking.LockService;
import com.example.devyankshaw.checking.R;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

import static android.accounts.AccountManager.KEY_PASSWORD;
import static com.example.devyankshaw.checking.MainActivity.PREFS_NAME;

public class PatternActivity extends AppCompatActivity {

    public static boolean patternLockStatus;
    PatternLockView patternLockViewConfirm, patternLockViewReconfirm;
    private SharedPreferences prefs, preferences;
    private String patternDotsConfirm;
    private TextView textViewPatternConfirm, textViewPatternReconfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        setTitle("Draw Pattern");

        preferences = getSharedPreferences(PREFS_NAME, 0);
        prefs = PreferenceManager.getDefaultSharedPreferences(PatternActivity.this);


        patternLockViewConfirm = findViewById(R.id.patter_lock_view_confirm);
        patternLockViewReconfirm = findViewById(R.id.patter_lock_view_reconfirm);

        textViewPatternConfirm = findViewById(R.id.textViewPatternConfirm);
        textViewPatternReconfirm = findViewById(R.id.textViewPatternReconfirm);

        patternLockViewConfirm.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (!(pattern.size() > 3)) {
                    FancyToast.makeText(PatternActivity.this, "Connect at least 4 dots", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                    patternLockViewConfirm.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    return;
                }

                patternDotsConfirm = PatternLockUtils.patternToString(patternLockViewConfirm, pattern);
                patternLockViewConfirm.setViewMode(PatternLockView.PatternViewMode.CORRECT);

                patternLockViewConfirm.setVisibility(View.INVISIBLE);
                textViewPatternConfirm.setVisibility(View.INVISIBLE);
                patternLockViewReconfirm.setVisibility(View.VISIBLE);
                textViewPatternReconfirm.setVisibility(View.VISIBLE);

                setTitle("Redraw Pattern");
            }

            @Override
            public void onCleared() {

            }
        });


        patternLockViewReconfirm.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (!(pattern.size() > 3)) {
                    FancyToast.makeText(PatternActivity.this, "Connect at least 4 dots", FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show();
                    patternLockViewReconfirm.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    return;
                }

                String patternDotsReconfirm = PatternLockUtils.patternToString(patternLockViewReconfirm, pattern);
                if(preferences.getBoolean("securityPatternSet", false)) {
                    if (patternDotsReconfirm.equals(patternDotsConfirm)) {
                        patternLockViewReconfirm.setViewMode(PatternLockView.PatternViewMode.CORRECT);

                        SharedPreferences.Editor ed = prefs.edit();
                        ed.putString(KEY_PASSWORD, patternDotsReconfirm);
                        ed.commit();

                        FancyToast.makeText(PatternActivity.this, "Pattern Saved Successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

                        //Here shared preference is used to check the status that if user sets pattern then patternLockScreen must be shown when screen is on
                        preferences.edit().putBoolean("patternLockStatus", true).commit();
                        preferences.edit().putBoolean("passwordLockStatus", false).commit();
                        preferences.edit().putBoolean("pinLockStatus", false).commit();

                        finish();
                    } else {
                        patternLockViewReconfirm.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        FancyToast.makeText(PatternActivity.this, "Pattern doesn't match", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                    }
                }


                    //Activate the service when set up pattern procedure is completed
                    if (preferences.getBoolean("pattern_first_done", true)) {
                        if (patternDotsReconfirm.equals(patternDotsConfirm)) {
                            if (preferences.getBoolean("pattern_first_time", true)) {
                                Intent intent = new Intent(PatternActivity.this, LockService.class);
                                ContextCompat.startForegroundService(PatternActivity.this, intent.setAction(Intent.ACTION_SCREEN_OFF));
                                preferences.edit().putBoolean("pattern_first_time", false).commit();

                                prefs.edit().putString(KEY_PASSWORD, patternDotsReconfirm).commit();
                                FancyToast.makeText(PatternActivity.this, "Pattern Saved Successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();

                            /*Here if the user installs the app for the first time and if they set pattern then next time when they set other securities
                            like pin or password then the first time method in PinActivity and PasswordActivity will not execute again*/
                                preferences.edit().putBoolean("pattern_first_done", true).commit();
                                preferences.edit().putBoolean("password_first_done", false).commit();
                                preferences.edit().putBoolean("pin_first_done", false).commit();

                                //Here shared preference is used to check the status that if user sets pattern then patternLockScreen must be shown when screen is on
                                preferences.edit().putBoolean("patternLockStatus", true).commit();
                                preferences.edit().putBoolean("passwordLockStatus", false).commit();
                                preferences.edit().putBoolean("pinLockStatus", false).commit();

                                //Disable the use of first time code for pin and password activity
                                preferences.edit().putBoolean("pin_first_time", false).commit();
                                preferences.edit().putBoolean("securityPinSet",true).commit();
                                preferences.edit().putBoolean("password_first_time", false).commit();
                                preferences.edit().putBoolean("securityPasswordSet",true).commit();


                                //This is used to block the back button until pattern is set for the first time
                                preferences.edit().putBoolean("securityPatternSet",true).commit();

                                finish();
                            }
                        } else {
                            patternLockViewReconfirm.setViewMode(PatternLockView.PatternViewMode.WRONG);
                            FancyToast.makeText(PatternActivity.this, "Pattern doesn't match", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                        }
                    }

            }

            @Override
            public void onCleared() {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(preferences.getBoolean("securityPatternSet",false)) {
            super.onBackPressed();
        }
    }
}
