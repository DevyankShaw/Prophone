package com.example.devyankshaw.checking.SecurityPassword;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.devyankshaw.checking.R;

import static com.example.devyankshaw.checking.MainActivity.PREFS_NAME;

public class AddSecurityActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_security);

        setTitle("Enable Security");

        preference = getSharedPreferences(PREFS_NAME, 0);

        findViewById(R.id.txtPin).setOnClickListener(this);
        findViewById(R.id.txtPattern).setOnClickListener(this);
        findViewById(R.id.txtPassword).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtPin:
                startActivity(new Intent(this, PinActivity.class));
                preference.edit().putBoolean("SecurityItemClicked",true).commit();
                break;
            case R.id.txtPattern:
                startActivity(new Intent(this, PatternActivity.class));
                preference.edit().putBoolean("SecurityItemClicked",true).commit();
                break;
            case R.id.txtPassword:
                startActivity(new Intent(this, PasswordActivity.class));
                preference.edit().putBoolean("SecurityItemClicked",true).commit();
                break;
        }
    }

    //Blocked back button pressed when the pin/password screen is active
    @Override
    public void onBackPressed() {
        if(preference.getBoolean("SecurityItemClicked",false)) {
            super.onBackPressed();
        }
        // Not calling **super**, disables back button in current screen.
    }
}
