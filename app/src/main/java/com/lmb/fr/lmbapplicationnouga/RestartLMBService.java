package com.lmb.fr.lmbapplicationnouga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartLMBService extends BroadcastReceiver {

//    @Override
    /*public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(RestartLMBService.class.getSimpleName(), "LMBService Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, LMBService.class));;
    }
}
