package com.lmb.fr.lmbapplicationnouga;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            //throw new UnsupportedOperationException("Not yet implemented");
            SmsMessage[] msgs;
            String format = null;
            Bundle bundle = intent.getExtras();
            String strMessage = "";
            String strMessageBody = "";
            String num = "";
            String numGroupe = "";
            Globals S = Globals.getInstance();
            // Get the SMS message.


            if (bundle != null) {
                format = bundle.getString("format");
            }
            // Retrieve the SMS message received.
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                // Check the Android version.
/*
            boolean isVersionM =
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
*/
                // Fill the msgs array.
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    // Check Android version and use appropriate createFromPdu.
//                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
/*
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
*/

                    String numTel = msgs[i].getOriginatingAddress();

                    strMessage += "SMS from " + numTel;
                    strMessageBody = msgs[i].getMessageBody();
                    strMessage += " :" + strMessageBody + "\n";

                    // Log and display the SMS message.
                    Log.d(TAG, "onReceive: " + strMessage);
                    Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();

                    if (strMessageBody.toLowerCase().contains("settel")) { //Vérification si c'est une commande admin
                        SmsManager smsManager = SmsManager.getDefault();
                        if (admin(numTel,getApplicationContext()) == true) {// Vérification d'appartenance au groupe Admin
                            num = strMessageBody.toLowerCase().substring(8);
                            PortalPhoneNumber = num;
                            S.setData(num);
                            smsManager.sendTextMessage(numTel, null, "Le nouveau numero du portail est le: " + num, null, null);
                        }
                        else {
                            smsManager.sendTextMessage(numTel, null, "Vous n'êtes pas autorisé(e) à utiliser cette commande.", null, null);

                        }
                    }

                    else if (strMessageBody.toLowerCase().contains("setgroup")) { // Vérification si c'est une commande admin
                        SmsManager smsManager = SmsManager.getDefault();
                        if (admin(numTel, getApplicationContext()) == true) { // Vérification d'appartenance au groupe Admin
                            numGroupe = strMessageBody.substring(9);
                            GroupName = numGroupe;
                            S.setDatag(numGroupe);
                            smsManager.sendTextMessage(numTel, null, "Le nouveau nom du groupe est le: " + numGroupe, null, null);

                        }
                        else {
                            smsManager.sendTextMessage(numTel, null, "Vous n'êtes pas autorisé(e) à utiliser cette commande.", null, null);
                        }
                    }

                  else if (S.getDatag().length() == 0) { // Vérification si la valeur du groupe est nulle ou non
                        message(context,intent); // Si oui alors pas de vérification
                    }
                    else {
                        if (contactExists(context, numTel)) { // Si non vérification dans les contacts du téléphone
                            if (getGroupsTitle(numTel, getApplicationContext())) {// Vérification de l'appartenance dans le groupe renseigné dans l'application
                                message(context, intent);
                            } else {
                                SmsManager smsManager = SmsManager.getDefault();
                                Log.d(TAG, "Debug avant");
                                smsManager.sendTextMessage(numTel, null, "Vous n'êtes pas adhérent du club. Veuillez contacter un membre du bureau pour plus d'informations.", null, null);
                                Log.d(TAG, "Debug après");
                            }
                            //Toast.makeText(context, "Telephone présent", Toast.LENGTH_SHORT).show();
                            //loadGroups(context,intent);
                        } else {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(numTel, null, "Vous n'êtes pas adhérent du club. Veuillez contacter un membre du bureau pour plus d'informations.", null, null);
                        }

                    }
                }
            }
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
        registerReceiver(LMB_SMS_Receiver,filter);
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

    public boolean contactExists(Context context, String numTel) { // Méthode de vérification dans les contacts
        // number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numTel));

        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public void message(Context context, Intent intent) { // Méthode contenant les différents messages d'envoi possible et leurs retours

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        String strMessageBody = "";
        String format = null;
        String num = "";
        String numGroupe = "";
        Globals S = Globals.getInstance();

        if (bundle != null) {
            format = bundle.getString("format");
        }
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus != null) {
            // Check the Android version.
/*
            boolean isVersionM =
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
*/
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
//                if (isVersionM) {
                // If Android version M or newer:
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
/*
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
*/

                String numTel = msgs[i].getOriginatingAddress();

                // Build the message to show.
                strMessage += "SMS from " + numTel;
                strMessageBody = msgs[i].getMessageBody();
                strMessage += " :" + strMessageBody + "\n";

                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + strMessage);
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();


                if (strMessageBody.toLowerCase().replaceAll("\\s", "").equals("ouvrir")) {
                    // envoie d'un SMS de confirmation de l'ouverture du portail
                    //PendingIntent pi = PendingIntent.getActivity(this, 0 , new Intent(this, sendmessage.class), 0);
                    SmsManager smsManager = SmsManager.getDefault();
                    //Toast.makeText(getApplicationContext(), "SMS sent.",

                    if ("IDLE".equals(CallListening.getCurrent_state()) && CallOnGoing == false) {
                        smsManager.sendTextMessage(numTel, null, "Ouverture du portail en cours. Si rien ne se passe, veillez ré-essayer dans 30 secondes.", null, null);
                        //num = "tel:07000010009796";
                        num = "tel:" + PortalPhoneNumber;

                        Intent appel = new Intent(Intent.ACTION_CALL, Uri.parse(num));
                        //Toast.makeText(context, "le numero est:"+numTel,Toast.LENGTH_LONG).show();
                        appel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(appel);
                    } else {
                        smsManager.sendTextMessage(numTel, null, "Ouverture du portail déjà en cours.", null, null);
                    }

                }

                   /* else if (GroupName !=""){
                        //faire le test d'appartenance
                        Toast.makeText(context, "C'est bien",Toast.LENGTH_SHORT).show();
                    }*/

                else if (strMessageBody.toLowerCase().replaceAll("\\s", "").equals("help")) {
                    // envoie d'un SMS d'aide sur les differentes commandes possible
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(numTel, null, "Texte d'aide:\n" +
                            "settel <numero>\n" +
                            "setgroup <nom>\n" +
                            "test\n" +
                            "ouvrir\n" +
                            "appel", null, null);

                } else if (strMessageBody.toLowerCase().replaceAll("\\s", "").equals("test")) {
                    // envoie d'un SMS de confirmation de l'ouverture du portail
                    //PendingIntent pi = PendingIntent.getActivity(this, 0 , new Intent(this, sendmessage.class), 0);
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(numTel, null, "Test de l'application LMB\n" +
                            "le numero du portail est le: " + S.getData() + ", le nom du groupe est : " + S.getDatag(), null, null);
                    //Toast.makeText(getApplicationContext(), "SMS sent.",


                } else if (strMessageBody.toLowerCase().replaceAll("\\s", "").equals("appel")) {

                    // envoie d'un SMS de confirmation de l'ouverture du portail
                    //PendingIntent pi = PendingIntent.getActivity(this, 0 , new Intent(this, sendmessage.class), 0);
                    SmsManager smsManager = SmsManager.getDefault();
                    //Toast.makeText(getApplicationContext(), "SMS sent.",

                    if (("IDLE".equals(CallListening.getCurrent_state())) && CallOnGoing == false) {
                        // Set the global variable here
                        CallOnGoing = true;

                        smsManager.sendTextMessage(numTel, null, "Appel de votre mobile en cours. Si rien ne se passe, veillez ré-essayer dans 30 secondes.", null, null);
                        num = "tel:" + numTel;
                        Intent appel = new Intent(Intent.ACTION_CALL, Uri.parse(num));
                        //Toast.makeText(context, "le numero est:"+numTel,Toast.LENGTH_LONG).show();
                        appel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(appel);
                    } else {
                        smsManager.sendTextMessage(numTel, null, "Appel de votre mobile déjà en cours.", null, null);
                    }
                }

            }
        }
    }


    public boolean getGroupsTitle(String numtel, Context context) { // Méthode de vérification dans les groupes

        List<String> groupsTitle = new ArrayList<>();
        boolean present = false;
        String contactId = null;
        Globals g = Globals.getInstance();



        Cursor cursorContactId  = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.CONTACT_ID},
                String.format("%s=? AND %s=?", ContactsContract.Data.DATA4, ContactsContract.Data.MIMETYPE),
                new String[]{numtel, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                null);

        while (cursorContactId.moveToNext()) {
            contactId = cursorContactId.getString(0);
        }
        cursorContactId.close();


        if (contactId == null)
            return present;

        List<String> groupIdList = new ArrayList<>();

        Cursor cursorGroupId = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                String.format("%s=? AND %s=?", ContactsContract.Data.CONTACT_ID, ContactsContract.Data.MIMETYPE),
                new String[]{contactId, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE},
                null);

        while (cursorGroupId.moveToNext()) {
            String groupId = cursorGroupId.getString(0);
            groupIdList.add(groupId);
        }
        cursorGroupId.close();

        Cursor cursorGroupTitle = getContentResolver().query(
                ContactsContract.Groups.CONTENT_URI, new String[]{ContactsContract.Groups.TITLE},
                ContactsContract.Groups._ID + " IN (" + TextUtils.join(",", groupIdList) + ")",
                null,
                null);

        while (cursorGroupTitle.moveToNext()) {
            String groupName = cursorGroupTitle.getString(0);
            groupsTitle.add(groupName);
            if (g.getDatag().equals(groupName)){
                present = true;
                return present;
            }
        }
        cursorGroupTitle.close();
        return present;
    }

    public boolean admin(String numtel, Context context) { // Méthode de vérification dans le groupe admin

        List<String> groupsTitle = new ArrayList<>();
        boolean present = false;
        String contactId = null;

        String output;
       /* switch (numtel.length()) {
            case 10:
                output = String.format("(%s) %s-%s", numtel.substring(0,3), numtel.substring(3,6), numtel.substring(6,10));
                break;
            case 12:
                output = String.format("%s0%s %s %s %s %s", numtel.substring(0,3), numtel.substring(3,4), numtel.substring(4,6), numtel.substring(6,8), numtel.substring(8,10), numtel.substring(10,12));
                output = output.replace("+33", "");
                break;
            default:
                return present;
        }*/

        Cursor cursorContactId  = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.CONTACT_ID},
                String.format("%s=? AND %s=?", ContactsContract.Data.DATA4, ContactsContract.Data.MIMETYPE),
                new String[]{numtel, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                null);

        while (cursorContactId.moveToNext()) {
            contactId = cursorContactId.getString(0);
        }
        cursorContactId.close();


        if (contactId == null)
            return present;

        List<String> groupIdList = new ArrayList<>();

        Cursor cursorGroupId = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data.DATA1},
                String.format("%s=? AND %s=?", ContactsContract.Data.CONTACT_ID, ContactsContract.Data.MIMETYPE),
                new String[]{contactId, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE},
                null);

        while (cursorGroupId.moveToNext()) {
            String groupId = cursorGroupId.getString(0);
            groupIdList.add(groupId);
        }
        cursorGroupId.close();

        Cursor cursorGroupTitle = getContentResolver().query(
                ContactsContract.Groups.CONTENT_URI, new String[]{ContactsContract.Groups.TITLE},
                ContactsContract.Groups._ID + " IN (" + TextUtils.join(",", groupIdList) + ")",
                null,
                null);

        while (cursorGroupTitle.moveToNext()) {
            String groupName = cursorGroupTitle.getString(0);
            groupsTitle.add(groupName);
            if (groupName.equals("LMB_Bureau")){
                present = true;
                return present;
            }
        }
        cursorGroupTitle.close();
        return present;
    }
}


