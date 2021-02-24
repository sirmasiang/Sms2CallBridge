package com.lmb.fr.lmbapplicationnouga;

import android.app.Notification;
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
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class LMBService extends Service {
    private static final String TAG ="LMB_Application";
    private static final String CHANNEL_ID = "LMBChannelID";
    private String PortalPhoneNumber = "";
    private String GroupName = "";
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
            // Get the SMS message.
            Bundle bundle = intent.getExtras();


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

                    if (GroupName.length() == 0) {

                        message(context,intent);
                    }

                    else{
                        if (contactExists(context, numTel)) {
                            Toast.makeText(context, "Telephone présent", Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"Téléphone détecté" + numTel);
                            message(context,intent);
                        } else {
                            Toast.makeText(context, "Téléphone absent", Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"Non détecté");
                        }
                        //Toast.makeText(context, "C'est bien",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    };

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
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
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

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

    public boolean contactExists(Context context, String numTel) {
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

    public void message(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        String strMessageBody = "";
        String format = null;
        String num = "";
        String numGroupe = "";

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

                if (strMessageBody.toLowerCase().contains("settel")) {
                    num = strMessageBody.toLowerCase().substring(7);
                    PortalPhoneNumber = num;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(numTel, null, "le nouveau numero du portail est le: " + num, null, null);

                }

                else if (strMessageBody.toLowerCase().contains("setgroup")){
                    numGroupe = strMessageBody.toLowerCase().substring(8);
                    GroupName = numGroupe;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(numTel, null, "le nouveau nom du groupe est le: " + numGroupe, null, null);
                }

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
                            "le numero du portail est le: " + PortalPhoneNumber + ", le nom du groupe est : " + GroupName, null, null);
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
}

