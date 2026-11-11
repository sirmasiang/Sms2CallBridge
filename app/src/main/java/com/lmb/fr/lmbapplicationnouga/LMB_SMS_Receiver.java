package com.lmb.fr.lmbapplicationnouga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LMB_SMS_Receiver extends BroadcastReceiver {
    private static final String TAG = "LmbSmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Désactivé au profit de LMBNotificationListenerService qui gère à la fois SMS et RCS
        Log.d(TAG, "LMB_SMS_Receiver est désactivé.");
    }
}
