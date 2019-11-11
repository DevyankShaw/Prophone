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

        /*Sent when the user is present after
         * device wakes up (e.g when the keyguard is gone)
         * */
//        if(Intent.ACTION_USER_PRESENT.equals(intent.getAction())){
//            Toast.makeText(context, "User Present", Toast.LENGTH_LONG).show();
//        }
        /*Device is shutting down. This is broadcast when the device
         * is being shut down (completely turned off, not sleeping)
         * */
//        if (Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())) {
//           /* Intent intent1 = new Intent();
//            intent1.setClassName(context.getPackageName(), LockScreenPin.class.getName());
//            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent1);*/
//            Toast.makeText(context, "User Unlocked", Toast.LENGTH_LONG).show();
//        }

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
