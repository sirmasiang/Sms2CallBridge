package com.lmb.fr.lmbapplicationnouga;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class LMBService extends Service {
    private static final String TAG ="LMB_Application";
    private static final String CHANNEL_ID = "LMBChannelID";
    private String PortalPhoneNumber = "";
    public String GroupName = "";
    public String Appel = "", Ouvrir= "", Ouvrir2= "", SetGroup= "", Settel= "", Avertissement= "", Test= "", Help= "", Appel2= "";
    public static boolean CallOnGoing = false;

    public void setCallOnGoing(boolean callOnGoing) {
        this.CallOnGoing = callOnGoing;
    }

    private final BroadcastReceiver LMB_SMS_Receiver = new BroadcastReceiver() {
        private static final String TAG = "LmbSmsReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            String strMessage = "";
            String strMessageBody = "";
            String num = "";
            String numGroupe = "";
            Globals S = Globals.getInstance();

            Log.d(TAG, "LMBService - SMS received");

            if (msgs != null && msgs.length > 0) {
                for (int i = 0; i < msgs.length; i++) {
                    if (msgs[i] == null) continue;

                    String numTel = msgs[i].getOriginatingAddress();
                    if (numTel == null) continue;

                    strMessageBody = msgs[i].getMessageBody();
                    if (strMessageBody == null) continue;

                    strMessage = "SMS from " + numTel + " :" + strMessageBody + "\n";

                    Log.d(TAG, "onReceive: " + strMessage);
                    Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();

                    if (strMessageBody.toLowerCase().contains("settel")) { //Vérification si c'est une commande admin
                        if (admin(numTel, getApplicationContext())) {// Vérification d'appartenance au groupe Admin
                            num = strMessageBody.toLowerCase().substring(8);
                            PortalPhoneNumber = num;
                            S.setData(num);
                            sendSms(context, numTel, "Le nouveau numero du portail est le: " + num);
                        } else {
                            sendSms(context, numTel, "Vous n'êtes pas autorisé(e) à utiliser cette commande.");
                        }
                    }

                    if (strMessageBody.toLowerCase().contains("setgroup")) { // Vérification si c'est une commande admin
                        if (admin(numTel, getApplicationContext())) { // Vérification d'appartenance au groupe Admin
                            numGroupe = strMessageBody.substring(9);
                            GroupName = numGroupe;
                            S.setDatag(numGroupe);
                            sendSms(context, numTel, "Le nouveau nom du groupe est le: " + numGroupe);
                        } else {
                            sendSms(context, numTel, "Vous n'êtes pas autorisé(e) à utiliser cette commande.");
                        }
                    }

                    if (S.getDatag() == null || S.getDatag().isEmpty()) { // Vérification si la valeur du groupe est nulle ou non
                        processCommand(context, numTel, strMessageBody);
                    }
                    else {
                        if (contactExists(context, numTel)) { // Si non vérification dans les contacts du téléphone
                            if (getGroupsTitle(numTel, getApplicationContext())) {// Vérification de l'appartenance dans le groupe renseigné dans l'application
                                processCommand(context, numTel, strMessageBody);
                            } else {
                                Log.d(TAG, "Debug avant");
                                sendSms(context, numTel, "Vous n'êtes pas adhérent du club. Veuillez contacter un membre du bureau pour plus d'informations.");
                                Log.d(TAG, "Debug après");
                            }
                        } else {
                            sendSms(context, numTel, "Vous n'êtes pas adhérent du club. Veuillez contacter un membre du bureau pour plus d'informations.");
                        }
                    }
                }
            }

            Log.d(TAG,"LMBService - SMS treated");

        }
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "My Notification Channel";
            String channelDescription = "Channel description here";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        Log.d(TAG,"LMBService - onCreate");
        registerReceiver(LMB_SMS_Receiver, filter);
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     * <p>
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     * <p>
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link AsyncTask}.</p>
     *
     * @param intent  The Intent supplied to {@link Context#startService},
     *                as given.  This may be null if the service is being restarted after
     *                its process has gone away, and it had previously returned anything
     *                except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags   Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelfResult(int)}.
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     * @see #stopSelfResult(int)
     */
    //@RequiresApi(api = Build.VERSION_CODES.O)



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "LMB service started", Toast.LENGTH_LONG).show();
        Log.d(TAG,"LMBService - onStartCommand");
        // this.startForegroundService();
        super.onStartCommand(intent, flags, startId);
        //return super.onStartCommand(intent, flags, startId);
        Globals g = Globals.getInstance();
        Globals globals = Globals.getInstance();


        String inputgroup = intent.getStringExtra("LMBServiceGroup");
        String input = intent.getStringExtra("LMBService");
        //PortalPhoneNumber = input;
        PortalPhoneNumber = g.getData();
        GroupName = globals.getDatag();

        Intent notificationIntent = new Intent(this, LMB_Application.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("LMB Service")
                .setContentText("Portal number: "+input)
                .setContentText("Group Name: " +inputgroup)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_STICKY;

    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "LMB service stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG,"LMBService - onDestroy");

        unregisterReceiver(LMB_SMS_Receiver);
        super.onDestroy();

        // To stop the automatic service start, comment following two lines, build and flash.
        Intent broadcastIntent = new Intent("RestartLMBService");
        sendBroadcast(broadcastIntent);

    }

    public LMBService() {
    }

    public LMBService(Context applicationContext) {
        super();
        Log.d(TAG, "LMBService - here I am!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    public static void sendSms(Context context, String destination, String text) {
        if (destination == null || destination.trim().isEmpty()) {
            Log.e(TAG, "sendSms: Destination address is null or empty");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Cannot send SMS: SEND_SMS permission not granted");
            Toast.makeText(context, "Erreur: Permission SEND_SMS non accordée", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            if (smsManager == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class);
            }

            if (smsManager == null) {
                Log.e(TAG, "sendSms: Unable to get SmsManager instance");
                Toast.makeText(context, "Erreur: Impossible d'obtenir SmsManager", Toast.LENGTH_LONG).show();
                return;
            }

            if (text.length() > 160) {
                ArrayList<String> parts = smsManager.divideMessage(text);
                smsManager.sendMultipartTextMessage(destination, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(destination, null, text, null, null);
            }

            Log.d(TAG, "SMS sent to " + destination + ": " + text);
            Toast.makeText(context, "SMS envoyé à " + destination, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS to " + destination, e);
            Toast.makeText(context, "Erreur envoi SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static boolean contactExists(Context context, String numTel) { // Méthode de vérification dans les contacts
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numTel));

        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur != null && cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public static void processCommand(Context context, String numTel, String strMessageBody) {
        Globals S = Globals.getInstance();
        String bodyClean = strMessageBody.toLowerCase().replaceAll("\\s", "");

        if (bodyClean.contains("help")) {
            Log.d(TAG, "help dans processCommand");
            sendSms(context, numTel, "Texte d'aide:\n" +
                    "settel <numero>\n" +
                    "setgroup <nom>\n" +
                    "test\n" +
                    "ouvrir\n" +
                    "appel");

        } else if (bodyClean.contains("test")) {
            sendSms(context, numTel, "Test de l'application LMB\n" +
                    "le numero du portail est le: " + S.getData() + ", le nom du groupe est : " + S.getDatag());

            Log.d(TAG, "test avec Intratone -> appuis sur voir les portes");

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MonApplication:WakeLock"
            );
            wakeLock.acquire(10000);

            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.cogelec.notificationpush");
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(launchIntent);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LMBAccessibilityService accessibilityService = LMBAccessibilityService.getInstance();
                        if (accessibilityService != null) {
     /*                       // push the button "voir les portes"
                            accessibilityService.performTapGesture(360f, 660f);*/
                            // push the button "Portailrue St Exupery"
                            accessibilityService.performTapGesture(360f, 660f);
                            Log.d(TAG, "performTapGesture exécuté aux coordonnées (360, 660)");
                        } else {
                            Log.e(TAG, "LMBAccessibilityService non connecté.");
                            Toast.makeText(context, "Veuillez activer l'accessibilité pour LMB Application", Toast.LENGTH_LONG).show();
                            try {
                                Intent settingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(settingsIntent);
                            } catch (Exception e) {
                                Log.e(TAG, "Impossible d'ouvrir les paramètres d'accessibilité", e);
                            }
                        }
                    }
                }, 5000);

            }


        } else if (bodyClean.contains("ouvrir")) {

            Log.d(TAG, "Ouverture de Intratone");

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MonApplication:WakeLock"
            );
            wakeLock.acquire(10000);

            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.cogelec.notificationpush");
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(launchIntent);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LMBAccessibilityService accessibilityService = LMBAccessibilityService.getInstance();
                        if (accessibilityService != null) {
     /*                       // push the button "voir les portes"
                            accessibilityService.performTapGesture(360f, 660f);*/
                            // push the button "Portailrue St Exupery"
                            accessibilityService.performTapGesture(350f, 510f);
                            Log.d(TAG, "performTapGesture exécuté aux coordonnées (350, 510)");
                        } else {
                            Log.e(TAG, "LMBAccessibilityService non connecté.");
                            Toast.makeText(context, "Veuillez activer l'accessibilité pour LMB Application", Toast.LENGTH_LONG).show();
                            try {
                                Intent settingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(settingsIntent);
                            } catch (Exception e) {
                                Log.e(TAG, "Impossible d'ouvrir les paramètres d'accessibilité", e);
                            }
                        }
                    }
                }, 5000);

            } else {
                Log.e(TAG, "Application Intratone (com.cogelec.notificationpush) non trouvée");
            }

/*            if ("IDLE".equals(CallListening.getCurrent_state())) {
                CallOnGoing = true;

                sendSms(context, numTel, "Ouverture du portail en cours. Si rien ne se passe, veillez ré-essayer dans 30 secondes.");
                String num = "tel:" + S.getData();

                Intent appel = new Intent(Intent.ACTION_CALL, Uri.parse(num));
                appel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    context.startActivity(appel);
                } else {
                    Log.d(TAG, "L'application n'a pas les droits necessaires pour lancer un appel !");
                }

            } else {
                sendSms(context, numTel, "Ouverture du portail déjà en cours.");
            }*/

        } else if (bodyClean.contains("appel")) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MonApplication:WakeLock"
            );
            wakeLock.acquire();

            if ("IDLE".equals(CallListening.getCurrent_state())) {
                CallOnGoing = true;

                sendSms(context, numTel, "Appel de votre mobile en cours. Si rien ne se passe, veillez ré-essayer dans 30 secondes.");
                String num = "tel:" + numTel;

                Log.d(TAG, "Call the SMS emitter :" + num);

                Intent appel = new Intent(Intent.ACTION_CALL, Uri.parse(num));
                appel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    context.startActivity(appel);
                } else {
                    Log.d(TAG, "L'application n'a pas les droits necessaires pour lancer un appel !");
                }

            } else {
                sendSms(context, numTel, "Appel de votre mobile déjà en cours.");
            }
        }
    }

    public void message(Context context, Intent intent) { // Méthode contenant les différents messages d'envoi possible et leurs retours
        Log.d(TAG, "Begin message method");
        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (msgs != null && msgs.length > 0) {
            for (int i = 0; i < msgs.length; i++) {
                if (msgs[i] == null) continue;
                String numTel = msgs[i].getOriginatingAddress();
                String strMessageBody = msgs[i].getMessageBody();
                if (numTel != null && strMessageBody != null) {
                    processCommand(context, numTel, strMessageBody);
                }
            }
        }
        Log.d(TAG, "End message method");
    }


    public static boolean getGroupsTitle(String numtel, Context context) { // Méthode de vérification dans les groupes

        List<String> groupsTitle = new ArrayList<>();
        boolean present = false;
        String contactId = null;
        Globals g = Globals.getInstance();

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numtel));
        String[] projection = {ContactsContract.PhoneLookup._ID};
        Cursor cursorContactId = context.getContentResolver().query(lookupUri, projection, null, null, null);
        if (cursorContactId != null) {
            try {
                if (cursorContactId.moveToFirst()) {
                    contactId = cursorContactId.getString(0);
                }
            } finally {
                cursorContactId.close();
            }
        }

        if (contactId == null)
            return present;

        List<String> groupIdList = new ArrayList<>();

        Cursor cursorGroupId = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                String.format("%s=? AND %s=?", ContactsContract.Data.CONTACT_ID, ContactsContract.Data.MIMETYPE),
                new String[]{contactId, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE},
                null);

        if (cursorGroupId != null) {
            while (cursorGroupId.moveToNext()) {
                String groupId = cursorGroupId.getString(0);
                groupIdList.add(groupId);
            }
            cursorGroupId.close();
        }

        if (groupIdList.isEmpty()) {
            return present;
        }

        Cursor cursorGroupTitle = context.getContentResolver().query(
                ContactsContract.Groups.CONTENT_URI, new String[]{ContactsContract.Groups.TITLE},
                ContactsContract.Groups._ID + " IN (" + TextUtils.join(",", groupIdList) + ")",
                null,
                null);

        if (cursorGroupTitle != null) {
            while (cursorGroupTitle.moveToNext()) {
                String groupName = cursorGroupTitle.getString(0);
                groupsTitle.add(groupName);
                if (g.getDatag() != null && g.getDatag().equals(groupName)){
                    present = true;
                    cursorGroupTitle.close();
                    return present;
                }
            }
            cursorGroupTitle.close();
        }
        return present;
    }

    public static boolean admin(String numtel, Context context) { // Méthode de vérification dans le groupe admin

        List<String> groupsTitle = new ArrayList<>();
        boolean present = false;
        String contactId = null;

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numtel));
        String[] projection = {ContactsContract.PhoneLookup._ID};
        Cursor cursorContactId = context.getContentResolver().query(lookupUri, projection, null, null, null);
        if (cursorContactId != null) {
            try {
                if (cursorContactId.moveToFirst()) {
                    contactId = cursorContactId.getString(0);
                }
            } finally {
                cursorContactId.close();
            }
        }

        if (contactId == null)
            return present;

        List<String> groupIdList = new ArrayList<>();

        Cursor cursorGroupId = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                String.format("%s=? AND %s=?", ContactsContract.Data.CONTACT_ID, ContactsContract.Data.MIMETYPE),
                new String[]{contactId, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE},
                null);

        if (cursorGroupId != null) {
            while (cursorGroupId.moveToNext()) {
                String groupId = cursorGroupId.getString(0);
                groupIdList.add(groupId);
            }
            cursorGroupId.close();
        }

        if (groupIdList.isEmpty()) {
            return present;
        }

        Cursor cursorGroupTitle = context.getContentResolver().query(
                ContactsContract.Groups.CONTENT_URI, new String[]{ContactsContract.Groups.TITLE},
                ContactsContract.Groups._ID + " IN (" + TextUtils.join(",", groupIdList) + ")",
                null,
                null);

        if (cursorGroupTitle != null) {
            while (cursorGroupTitle.moveToNext()) {
                String groupName = cursorGroupTitle.getString(0);
                groupsTitle.add(groupName);
                if ("LMB_Bureau".equals(groupName)){
                    present = true;
                    cursorGroupTitle.close();
                    return present;
                }
            }
            cursorGroupTitle.close();
        }
        return present;
    }
}


