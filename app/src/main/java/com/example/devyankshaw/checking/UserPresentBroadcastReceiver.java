package com.example.devyankshaw.checking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import static com.example.devyankshaw.checking.MainActivity.PREFS_NAME;

public class UserPresentBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferences prefs;
    @Override
    public void onReceive(Context context, Intent intent) {

        prefs = context.getSharedPreferences(PREFS_NAME, 0);

        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            if(prefs.getBoolean("pinLockStatus", false)) {
                Intent intentOne = new Intent(context, LockScreenPin.class);
                intentOne.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_RECEIVER_FOREGROUND);
                context.startActivity(intentOne);
            }else if(prefs.getBoolean("patternLockStatus", false)){
                Intent intentTwo = new Intent(context, LockScreenPattern.class);
                intentTwo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_RECEIVER_FOREGROUND);
                context.startActivity(intentTwo);
            }else if(prefs.getBoolean("passwordLockStatus", false)){
                Intent intentThree = new Intent(context, LockScreenPassword.class);
                intentThree.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_RECEIVER_FOREGROUND);
                context.startActivity(intentThree);
            }
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if(prefs.getBoolean("AUTO_START", false)) {
                Intent i = new Intent(context, LockService.class);
                ContextCompat.startForegroundService(context, i.setAction(Intent.ACTION_SCREEN_OFF));
                prefs.edit().putBoolean("switchKeyLock", true).commit();
            }
        }

    }
}
