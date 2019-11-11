package com.example.devyankshaw.checking.OneTapLock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.devyankshaw.checking.R;

public class TapLock extends AppCompatActivity implements View.OnClickListener {

    public static final int RESULT_ENABLE = 11;
    private Button lock, disable, enable;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_lock);

        setTitle("Tap Lock");

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        lock = findViewById(R.id.lock);
        enable = findViewById(R.id.enableBtn);
        disable = findViewById(R.id.disableBtn);
        lock.setOnClickListener(this);
        enable.setOnClickListener(this);
        disable.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isActive = devicePolicyManager.isAdminActive(compName);
        disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
        enable.setVisibility(isActive ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (view == lock) {
            boolean active = devicePolicyManager.isAdminActive(compName);

            if (active) {
                //Remove the current activity
                Intent intent = getIntent();
                if (intent.hasExtra("clicked")) {
                    finish();
                }
                //locks the phone
                devicePolicyManager.lockNow();
            } else {
                Toast.makeText(this, "You need to enable the Device Admin", Toast.LENGTH_SHORT).show();
            }

        } else if (view == enable) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
            startActivityForResult(intent, RESULT_ENABLE);

        } else if (view == disable) {
            devicePolicyManager.removeActiveAdmin(compName);
            disable.setVisibility(View.GONE);
            enable.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(TapLock.this, "You have enabled the Device Admin", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TapLock.this, "Problem to enable the Device Admin", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
