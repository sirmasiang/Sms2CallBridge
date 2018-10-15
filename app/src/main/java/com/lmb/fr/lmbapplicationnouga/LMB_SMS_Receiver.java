package com.lmb.fr.lmbapplicationnouga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class LMB_SMS_Receiver extends BroadcastReceiver {
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

                String numTel=msgs[i].getOriginatingAddress();

                // Build the message to show.
                strMessage += "SMS from " + numTel;
                strMessageBody = msgs[i].getMessageBody();
                strMessage += " :" + strMessageBody + "\n";

                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + strMessage);
                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();

                if(strMessageBody.toLowerCase().replaceAll("\\s", "").equals("ouvrir")) {
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
                    }
                    else {
                        smsManager.sendTextMessage(numTel, null, "Ouverture du portail déjà en cours.", null, null);
                    }
                }
                else if(strMessageBody.toLowerCase().replaceAll("\\s", "").equals("test")) {
                    // envoie d'un SMS de confirmation de l'ouverture du portail
                    //PendingIntent pi = PendingIntent.getActivity(this, 0 , new Intent(this, sendmessage.class), 0);
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(numTel, null, "Test de l'application LMB", null, null);
                    //Toast.makeText(getApplicationContext(), "SMS sent.",
                }

            }
        }
    }
}
