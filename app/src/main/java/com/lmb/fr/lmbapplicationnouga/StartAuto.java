package com.lmb.fr.lmbapplicationnouga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartAuto extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d("LMB_Application","LMB Application - Service Auto start");
        Intent i = new Intent(context, LMBService.class);
        context.startService(i);
    }
}