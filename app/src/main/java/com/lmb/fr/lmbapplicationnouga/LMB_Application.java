package com.lmb.fr.lmbapplicationnouga;

import android.app.Activity;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class LMB_Application extends Activity {
    private static final String TAG ="LMB_Application";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lmb__application);
        Log.d(TAG,"LMB Application - onCreate");
    }

    /**
     * This is the same as {@link #onSaveInstanceState} but is called for activitiesÍ
     * created with the attribute {@link android.R.attr#persistableMode} set to
     * <code>persistAcrossReboots</code>. The {@link PersistableBundle} passed
     * in will be saved and presented in {@link #onCreate(Bundle, PersistableBundle)}
     * the first time that this activity is restarted following the next device reboot.
     *
     * @param outState           Bundle in which to place your saved state.
     * @param outPersistentState State which will be saved across reboots.
     * @see #onSaveInstanceState(Bundle)
     * @see #onCreate
     * @see #onRestoreInstanceState(Bundle, PersistableBundle)
     * @see #onPause
     */
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.d(TAG,"LMB Application - onSaveInstanceState");
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"LMB Application - onDestroy");
        super.onDestroy();
    }

    public void startService(View view) {
        Log.d(TAG,"LMB Application - startService");
        startService(new Intent(getBaseContext(), LMBService.class));
    }

    // Method to stop the service
    public void stopService(View view) {
        Log.d(TAG,"LMB Application - stopService");
        stopService(new Intent(getBaseContext(), LMBService.class));
    }


}
