package com.lmb.fr.lmbapplicationnouga;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LMB_Application extends Activity implements View.OnClickListener{
    private static final String TAG ="LMB_Application";
    private EditText PhoneNumer;
    private EditText GroupName;
    private TextView Group, Phone;
    private Button btnmessages,btnsave,btnrefresh;
    private String a,b;
    Intent save,messages,refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lmb__application);
        Log.d(TAG,"LMB Application - onCreate");
        PhoneNumer = findViewById(R.id.PortalPhoneNumber);
        GroupName = findViewById(R.id.GroupID);
        btnmessages = findViewById(R.id.messages);
        btnmessages.setOnClickListener(this);
        btnsave = findViewById(R.id.Sauvegarder);
        btnsave.setOnClickListener(this);
        btnrefresh = findViewById(R.id.Refresh);
        btnrefresh.setOnClickListener(this);

        Globals g = Globals.getInstance();
        a=g.getDatag();
        b=g.getData();

        Phone = (TextView) this.findViewById(R.id.PhoneString);
        Phone.setText(" " + b + " ");

        Group = (TextView) this.findViewById(R.id.GroupString);
        Group.setText(" " + a + " ");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            /*case R.id.messages :
                messages = new Intent (this, Messages.class);
                this.startActivity(messages);
                break;*/
            case R.id.Sauvegarder :
                save();
                save = new Intent (this,LMB_Application.class);
                this.startActivity(save);
                break;
            case R.id.Refresh :
                refresh();
                refresh = new Intent (this,LMB_Application.class);
                this.startActivity(refresh);
                break;
        }
    }


    public void refresh(){
        Globals R = Globals.getInstance();

        R.getData();
        R.getDatag();
    }

    public void save(){
        Globals N = Globals.getInstance();
        Globals G = Globals.getInstance();

        String inputNumber = PhoneNumer.getText().toString();
        N.setData(inputNumber);
        String inputGroup = GroupName.getText().toString();
        G.setDatag(inputGroup);
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
        Globals g = Globals.getInstance();
        Globals x = Globals.getInstance();

        //String input = editTextInput.getText().toString();
        //String input = "Hello World";
        String input = PhoneNumer.getText().toString();
        g.setData(input);

        String inputgroup = GroupName.getText().toString();
        x.setDatag(inputgroup);

        Intent serviceIntent = new Intent(this, LMBService.class);
        serviceIntent.putExtra("LMBService", input);

        Intent IntentGroup = new Intent(this, LMBService.class);
        IntentGroup.putExtra("LMBServiceGroup", inputgroup);

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
