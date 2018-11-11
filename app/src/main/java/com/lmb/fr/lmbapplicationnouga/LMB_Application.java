package com.lmb.fr.lmbapplicationnouga;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class LMB_Application extends Activity {
    private static final String TAG ="LMB_Application";
    private EditText PhoneNumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lmb__application);
        Log.d(TAG,"LMB Application - onCreate");
        PhoneNumer = findViewById(R.id.PortalPhoneNumber);
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
        Log.d(TAG,"LMB Application - onDestroy and stop service");
        stopService(new Intent(getBaseContext(), LMBService.class));
        super.onDestroy();
    }

    public void startService(View view) {
        Log.d(TAG,"LMB Application - startService");
        LMBService mLMBService = new LMBService();

        //String input = editTextInput.getText().toString();
        //String input = "Hello World";
        String input = PhoneNumer.getText().toString();

        Intent serviceIntent = new Intent(this, LMBService.class);
        serviceIntent.putExtra("LMBService", input);

        if(!isMyServiceRunning(mLMBService.getClass())) {
            //startService(new Intent(getBaseContext(), LMBService.class));
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    // Method to stop the service
    public void stopService(View view) {
        Log.d(TAG,"LMB Application - stopService");
        stopService(new Intent(getBaseContext(), LMBService.class));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
