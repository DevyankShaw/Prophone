package com.example.devyankshaw.checking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import java.util.concurrent.locks.Lock;

public class UserPresentBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        /*Sent when the user is present after
         * device wakes up (e.g when the keyguard is gone)
         * */
        if(Intent.ACTION_USER_PRESENT.equals(intent.getAction())){
            Toast.makeText(context, "User Present", Toast.LENGTH_LONG).show();
        }
        /*Device is shutting down. This is broadcast when the device
         * is being shut down (completely turned off, not sleeping)
         * */
//        if (Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())) {
//           /* Intent intent1 = new Intent();
//            intent1.setClassName(context.getPackageName(), LockScreen.class.getName());
//            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent1);*/
//            Toast.makeText(context, "User Unlocked", Toast.LENGTH_LONG).show();
//        }

        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            Intent intentone = new Intent(context, LockScreen.class);
            intentone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_RECEIVER_FOREGROUND);
            context.startActivity(intentone);
//            Toast.makeText(context, "Action Screen On", Toast.LENGTH_LONG).show();
        }
    }
}
