package com.lmb.fr.lmbapplicationnouga;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
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

        // Ignorer les notifications de résumé de groupe (summary notifications)
        if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            return;
        }

        Bundle extras = notification.extras;
        CharSequence titleCS = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence textCS = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (textCS == null || textCS.length() == 0) {
            textCS = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        }

        if (textCS == null || textCS.length() == 0) {
            return;
        }

        String sender = (titleCS != null) ? titleCS.toString().trim() : "";
        String messageBody = textCS.toString().trim();

        // Ignorer si le message contient le texte de nos propres réponses (anti-boucle)
        if (isSelfReply(messageBody)) {
            Log.d(TAG, "Notification ignorée (anti-boucle) : contient notre texte de réponse.");
            return;
        }

        String messageLower = messageBody.toLowerCase();

        // 1. "ouvir" / "ouvrir" -> Affiche le message "ouverture du portail en cours"
        if (messageLower.contains("ouvir") || messageLower.contains("ouvrir")) {
            if (LMBService.shouldProcessCommand(getApplicationContext(), sender, "ouvrir")) {
                Log.d(TAG, "Commande 'ouvrir' détectée via notification");
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(getApplicationContext(), "ouverture du portail en cours", Toast.LENGTH_LONG).show()
                );
                sendReply(notification, sender, "Ouverture du portail en cours");

                // Ouverture du portail via l'application Intratone
                Log.d(TAG, "Ouverture par l'application Intratone");

                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (powerManager != null) {
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                            "MonApplication:WakeLock"
                    );
                    wakeLock.acquire(10000);
                }

                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.cogelec.notificationpush");
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(launchIntent);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        LMBAccessibilityService accessibilityService = LMBAccessibilityService.getInstance();
                        if (accessibilityService != null) {
                            // push the button "voir les portes"
                            accessibilityService.performTapGesture(360f, 660f);
                            // push the button "Portailrue St Exupery"
                            //accessibilityService.performTapGesture(350f, 510f);
                            Log.d(TAG, "performTapGesture exécuté aux coordonnées (350, 510)");
                        } else {
                            Log.e(TAG, "LMBAccessibilityService non connecté.");
                            Toast.makeText(getApplicationContext(), "Veuillez activer l'accessibilité pour LMB Application", Toast.LENGTH_LONG).show();
                            try {
                                Intent settingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(settingsIntent);
                            } catch (Exception e) {
                                Log.e(TAG, "Impossible d'ouvrir les paramètres d'accessibilité", e);
                            }
                        }
                    }, 5000);

                } else {
                    Log.e(TAG, "Application Intratone (com.cogelec.notificationpush) non trouvée");
                }
            }
        }
        // 2. "test" -> Renvoie le message "message test bien reçu" à l'émetteur du message
        else if (messageLower.contains("test")) {
            if (LMBService.shouldProcessCommand(getApplicationContext(), sender, "test")) {
                Log.d(TAG, "Commande 'test' détectée via notification");
                sendReply(notification, sender, "message test bien reçu");
            }
        }
        // 3. "help" -> Renvoie le texte "ouvrir, test, help" à l'émetteur du message
        else if (messageLower.contains("help")) {
            if (LMBService.shouldProcessCommand(getApplicationContext(), sender, "help")) {
                Log.d(TAG, "Commande 'help' détectée via notification");
                sendReply(notification, sender, "ouvrir, test, help");
            }
        }
    }

    /**
     * Vérifie si le texte du message correspond à un message émis par notre bot/application.
     */
    private boolean isSelfReply(String messageBody) {
        String messageLower = messageBody.toLowerCase();
        return messageLower.contains("message test bien reçu") ||
               messageLower.contains("ouverture du portail en cours") ||
               messageLower.contains("ouvrir, test, help") ||
               messageLower.contains("le nouveau numero") ||
               messageLower.contains("le nouveau nom") ||
               messageLower.contains("test de l'application lmb") ||
               messageLower.contains("texte d'aide") ||
               messageLower.contains("appel de votre mobile");
    }

    private void sendReply(Notification notification, String sender, String replyText) {
        if (replyViaNotification(notification, replyText)) {
            Log.d(TAG, "Réponse envoyée directement via le canal de notification (RCS/Messagerie)");
            return;
        }

        String targetPhone = resolvePhoneNumberStatic(getApplicationContext(), sender);
        if (targetPhone != null && !targetPhone.isEmpty()) {
            Log.d(TAG, "Repli sur l'envoi par SMS vers : " + targetPhone);
            LMBService.sendSms(getApplicationContext(), targetPhone, replyText);
        } else {
            Log.e(TAG, "Impossible d'obtenir le numéro de téléphone pour l'émetteur : " + sender);
        }
    }

    private boolean replyViaNotification(Notification notification, String replyText) {
        if (notification == null || notification.actions == null) {
            return false;
        }

        for (Notification.Action action : notification.actions) {
            if (action == null || action.getRemoteInputs() == null) {
                continue;
            }

            for (RemoteInput remoteInput : action.getRemoteInputs()) {
                if (remoteInput != null && action.actionIntent != null) {
                    try {
                        Bundle results = new Bundle();
                        results.putCharSequence(remoteInput.getResultKey(), replyText);

                        Intent fillInIntent = new Intent();
                        RemoteInput.addResultsToIntent(action.getRemoteInputs(), fillInIntent, results);

                        action.actionIntent.send(getApplicationContext(), 0, fillInIntent);
                        return true;
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "Erreur lors de l'envoi de la réponse via RemoteInput", e);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Tente de déterminer le numéro de téléphone à partir de la chaîne de l'émetteur.
     */
    public static String resolvePhoneNumberStatic(Context context, String sender) {
        if (sender == null || sender.isEmpty()) {
            return "";
        }
        if (sender.matches("^[+\\d\\s\\-()]{3,}$")) {
            return sender.replaceAll("[^+\\d]", "");
        }

        try {
            Cursor cursor = context.getContentResolver().query(
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
