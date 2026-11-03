package com.lmb.fr.lmbapplicationnouga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class RestartLMBService extends BroadcastReceiver {

    private static final String TAG = "RestartLMBService";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast reçu: " + (intent != null ? intent.getAction() : "null"));

        // Réactiver automatiquement le service d'accessibilité si WRITE_SECURE_SETTINGS est accordé
        LMBAccessibilityService.autoEnableAccessibilityService(context);

        // Relancer le service LMB
        Intent serviceIntent = new Intent(context, LMBService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du démarrage de LMBService depuis le receiver", e);
        }
    }
}
