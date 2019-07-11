package com.example.devyankshaw.checking;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


import static android.content.Intent.ACTION_SCREEN_OFF;
import static com.example.devyankshaw.checking.App.CHANNEL_ID;
import static com.example.devyankshaw.checking.LockScreenPin.notificationPanel;


public class LockService extends Service {

    TelephonyManager telephonyManager;
    PhoneStateListener listener;

    private SharedPreferences settings;

    UserPresentBroadcastReceiver userPresentBroadcastReceiver = null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Registering Broadcast Receiver
        userPresentBroadcastReceiver = new UserPresentBroadcastReceiver();
        IntentFilter filter1 = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter1.addAction(Intent.ACTION_USER_PRESENT);
        filter1.setPriority(999);
        registerReceiver(userPresentBroadcastReceiver, filter1);


        if(intent == null || intent.getAction() == null) {
            //Toast.makeText(this, "Null", Toast.LENGTH_LONG).show();
            return START_STICKY;
        }

        String action = intent.getAction();
        //Disabling the slide to unlock event
        if(action.equals(ACTION_SCREEN_OFF))
        {
            KeyguardManager.KeyguardLock k1;
            KeyguardManager km =(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
            k1= km.newKeyguardLock("IN");
            k1.disableKeyguard();
        }

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Create a new PhoneStateListener
        listener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        notificationPanel = false;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        notificationPanel = true;
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        notificationPanel = true;
                        break;
                }
            }
        };

        // Register the listener with the telephony manager
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);



        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        // Here pending intent is used because when i press the notification then it will open the MainActivity of our app otherwise it will not open anything
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Lock Service")
                .setContentText("service running")
                .setSmallIcon(R.drawable.ic_android)
//                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);//By putting this line the system will understand that it is not a normal background service which will get killed within 1 min rather it will remain after 1 min also until and unless i kill it




        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(userPresentBroadcastReceiver != null) {
            unregisterReceiver(userPresentBroadcastReceiver);
        }
    }
}
