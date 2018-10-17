package com.lmb.fr.lmbapplicationnouga;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class LMBService extends Service {

    private final BroadcastReceiver LMB_SMS_Receiver = new BroadcastReceiver() {
        private static final String TAG = "LmbSmsReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            //throw new UnsupportedOperationException("Not yet implemented");

            // Get the SMS message.
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String strMessage = "";
            String strMessageBody = "";
            String format = null;
            String num = "";

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

                        if ("IDLE".equals(CallListening.getCurrent_state())) {
                            smsManager.sendTextMessage(numTel, null, "Ouverture du portail en cours. Si rien ne se passe, veillez ré-essayer dans 30 secondes.", null, null);
                            num = "tel:07000010009796";
                            Intent appel = new Intent(Intent.ACTION_CALL, Uri.parse(num));
                            //Toast.makeText(context, "le numero est:"+numTel,Toast.LENGTH_LONG).show();
                            appel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(appel);
                        } else {
                            smsManager.sendTextMessage(numTel, null, "Ouverture du portail déjà en cours.", null, null);
                        }
                    } else if (strMessageBody.toLowerCase().replaceAll("\\s", "").equals("test")) {
                        // envoie d'un SMS de confirmation de l'ouverture du portail
                        //PendingIntent pi = PendingIntent.getActivity(this, 0 , new Intent(this, sendmessage.class), 0);
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(numTel, null, "Test de l'application LMB", null, null);
                        //Toast.makeText(getApplicationContext(), "SMS sent.",
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
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "LMB service started", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
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
        unregisterReceiver(LMB_SMS_Receiver);
        super.onDestroy();
    }

    public LMBService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}
