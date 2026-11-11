package com.lmb.fr.lmbapplicationnouga;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

        checkAndRequestPermissions();

        Globals g = Globals.getInstance();
        a=g.getDatag();
        b=g.getData();

        Phone = (TextView) this.findViewById(R.id.PhoneString);
        Phone.setText(" " + b + " ");

        Group = (TextView) this.findViewById(R.id.GroupString);
        Group.setText(" " + a + " ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LMBAccessibilityService.getInstance() == null) {
            android.widget.Toast.makeText(this, "Veuillez activer l'accessibilité pour LMB Application", android.widget.Toast.LENGTH_LONG).show();
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Erreur ouverture écran accessibilité", e);
            }
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE
        };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 100);
        }
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
        Log.d(TAG,"LMB Application - onDestroy");
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

        // Demande d'exclusion de l'optimisation batterie et démarrage du service d'écoute des notifications (SMS / RCS)
        requestIgnoreBatteryOptimizations();

        if (!isNotificationServiceEnabled()) {
            checkAndRequestNotificationListenerPermission();
        } else {
            toggleNotificationListenerService();
        }
    }

    private void requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Erreur demande exclusion optimisation batterie", e);
                }
            }
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(pkgName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAndRequestNotificationListenerPermission() {
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Veuillez accorder l'accès aux notifications pour LMB Application", Toast.LENGTH_LONG).show();
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Erreur ouverture écran paramètres d'accès aux notifications", e);
            }
        }
    }

    private void toggleNotificationListenerService() {
        try {
            ComponentName componentName = new ComponentName(this, LMBNotificationListenerService.class);
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Log.d(TAG, "LMBNotificationListenerService réactivé/reconnecté");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la réactivation du service NotificationListener", e);
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
