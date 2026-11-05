package com.lmb.fr.lmbapplicationnouga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class LMB_SMS_Receiver extends BroadcastReceiver {
    private static final String TAG = "LmbSmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Begin the onReceive in LMB_SMS_Receiver");
        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        Globals S = Globals.getInstance();

        if (msgs != null && msgs.length > 0) {
            for (int i = 0; i < msgs.length; i++) {
                if (msgs[i] == null) continue;
                String numTel = msgs[i].getOriginatingAddress();
                String strMessageBody = msgs[i].getMessageBody();
                if (numTel == null || strMessageBody == null) continue;

                Log.d(TAG, "SMS received from " + numTel + " : " + strMessageBody);
                Toast.makeText(context, "SMS de " + numTel + " : " + strMessageBody, Toast.LENGTH_LONG).show();

                if (strMessageBody.toLowerCase().contains("settel")) {
                    if (LMBService.admin(numTel, context)) {
                        String num = strMessageBody.toLowerCase().substring(8);
                        S.setData(num);
                        LMBService.sendSms(context, numTel, "Le nouveau numero du portail est le: " + num);
                    } else {
                        LMBService.sendSms(context, numTel, "Vous n'êtes pas autorisé(e) à utiliser cette commande.");
                    }
                } else if (strMessageBody.toLowerCase().contains("setgroup")) {
                    if (LMBService.admin(numTel, context)) {
                        String numGroupe = strMessageBody.substring(9);
                        S.setDatag(numGroupe);
                        LMBService.sendSms(context, numTel, "Le nouveau nom du groupe est le: " + numGroupe);
                    } else {
                        LMBService.sendSms(context, numTel, "Vous n'êtes pas autorisé(e) à utiliser cette commande.");
                    }
                } else if (S.getDatag() == null || S.getDatag().isEmpty()) {
                    LMBService.processCommand(context, numTel, strMessageBody);
                } else {
                    if (LMBService.contactExists(context, numTel) && LMBService.getGroupsTitle(numTel, context)) {
                        LMBService.processCommand(context, numTel, strMessageBody);
                    } else {
                        LMBService.sendSms(context, numTel, "Vous n'êtes pas adhérent du club. Veuillez contacter un membre du bureau pour plus d'informations.");
                    }
                }
            }
        }

        Log.d(TAG, "End the onReceive in LMB_SMS_Receiver");
    }
}
