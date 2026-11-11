package com.lmb.fr.lmbapplicationnouga;

import android.app.Notification;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

public class LMBNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "LMBNotifListener";

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "LMBNotificationListenerService connecté");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "LMBNotificationListenerService déconnecté");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;

        // Ignorer nos propres notifications
        if (getPackageName().equals(sbn.getPackageName())) {
            return;
        }

        Notification notification = sbn.getNotification();
        if (notification == null || notification.extras == null) {
            return;
        }

        Bundle extras = notification.extras;
        CharSequence titleCS = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence textCS = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (textCS == null || textCS.length() == 0) {
            textCS = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        }

        if (textCS == null) {
            return;
        }

        String sender = (titleCS != null) ? titleCS.toString().trim() : "";
        String messageBody = textCS.toString().trim();

        Log.d(TAG, "Notification reçue de [" + sender + "] via package [" + sbn.getPackageName() + "] : " + messageBody);

        String messageLower = messageBody.toLowerCase();

        // 1. "ouvir" / "ouvrir" -> Affiche le message "ouverture du portail en cours"
        if (messageLower.contains("ouvir") || messageLower.contains("ouvrir")) {
            Log.d(TAG, "Commande 'ouvrir' détectée via notification");
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), "ouverture du portail en cours", Toast.LENGTH_LONG).show()
            );
            // Traiter la commande d'ouverture
            LMBService.processCommand(getApplicationContext(), resolvePhoneNumber(sender), messageBody);
        }
        // 2. "test" -> Renvoie le message "message test bien reçu" à l'émetteur du message
        else if (messageLower.contains("test")) {
            Log.d(TAG, "Commande 'test' détectée via notification");
            String targetPhone = resolvePhoneNumber(sender);
            if (targetPhone != null && !targetPhone.isEmpty()) {
                LMBService.sendSms(getApplicationContext(), targetPhone, "message test bien reçu");
            } else {
                Log.e(TAG, "Impossible d'obtenir le numéro de téléphone pour l'émetteur : " + sender);
            }
        }
        // 3. "help" -> Renvoie le texte "ouvrir, test, help" à l'émetteur du message
        else if (messageLower.contains("help")) {
            Log.d(TAG, "Commande 'help' détectée via notification");
            String targetPhone = resolvePhoneNumber(sender);
            if (targetPhone != null && !targetPhone.isEmpty()) {
                LMBService.sendSms(getApplicationContext(), targetPhone, "ouvrir, test, help");
            } else {
                Log.e(TAG, "Impossible d'obtenir le numéro de téléphone pour l'émetteur : " + sender);
            }
        }
    }

    /**
     * Tente de déterminer le numéro de téléphone à partir de la chaîne de l'émetteur.
     */
    private String resolvePhoneNumber(String sender) {
        if (sender == null || sender.isEmpty()) {
            return "";
        }
        // Si la chaîne contient déjà un numéro de téléphone
        if (sender.matches("^[+\\d\\s\\-()]{3,}$")) {
            return sender.replaceAll("[^+\\d]", "");
        }

        // Sinon, chercher dans les contacts
        try {
            Cursor cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ? OR " +
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                    new String[]{sender, "%" + sender + "%"},
                    null
            );
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (index != -1) {
                            return cursor.getString(index);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la recherche du contact pour " + sender, e);
        }

        return sender;
    }
}
