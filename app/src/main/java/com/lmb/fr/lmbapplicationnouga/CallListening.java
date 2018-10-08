package com.lmb.fr.lmbapplicationnouga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallListening extends BroadcastReceiver {
//    static CustomPhoneStateListener customPhoneListener;
    private static final String TAG ="broadcast_intent";
    public static String incoming_number;
    private String current_state,previus_state,event;
    public static Boolean dialog= false;
    private Context context;
    private SharedPreferences sp,sp1;
    private SharedPreferences.Editor spEditor,spEditor1;
    public void onReceive(Context context, Intent intent) {
        //Log.d("intent_log", "Intent" + intent);
        dialog=true;
        this.context = context;
        event = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        incoming_number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        Log.d(TAG, "The received event : "+event+", incoming_number : " + incoming_number);
        previus_state = getCallState(context);
        current_state = "IDLE";
        if(incoming_number!=null){
            updateIncomingNumber(incoming_number,context);
        }else {
            incoming_number=getIncomingNumber(context);
        }
        switch (event) {
            case "RINGING":
                Log.d(TAG, "State : Ringing, incoming_number : " + incoming_number);
                if((previus_state.equals("IDLE")) || (previus_state.equals("FIRST_CALL_RINGING"))){
                    current_state ="FIRST_CALL_RINGING";
                }
                if((previus_state.equals("OFFHOOK"))||(previus_state.equals("SECOND_CALL_RINGING"))){
                    current_state = "SECOND_CALL_RINGING";
                }

                break;
            case "OFFHOOK":
                Log.d(TAG, "State : offhook, incoming_number : " + incoming_number);
                if((previus_state.equals("IDLE")) ||(previus_state.equals("FIRST_CALL_RINGING")) || previus_state.equals("OFFHOOK")){
                    current_state = "OFFHOOK";
                }
                if(previus_state.equals("SECOND_CALL_RINGING")){
                    current_state ="OFFHOOK";
//                    startDialog(context);
                }
                break;
            case "IDLE":
                Log.d(TAG, "State : idle and  incoming_number : " + incoming_number);
                if((previus_state.equals("OFFHOOK")) || (previus_state.equals("SECOND_CALL_RINGING")) || (previus_state.equals("IDLE"))){
                    current_state="IDLE";
                }
                if(previus_state.equals("FIRST_CALL_RINGING")){
                    current_state = "IDLE";
//                    startDialog(context);
                }
                updateIncomingNumber("no_number",context);
                Log.d(TAG,"stored incoming number flushed");
                break;
        }
        if(!current_state.equals(previus_state)){
            Log.d(TAG, "Updating  state from "+previus_state +" to "+current_state);
            updateCallState(current_state,context);

        }
    }
/*    public void startDialog(Context context) {
        Log.d(TAG,"Starting Dialog box");
        Intent intent1 = new Intent(context, NotifyHangup.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);

    }*/
    public void updateCallState(String state,Context context){
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        spEditor = sp.edit();
        spEditor.putString("call_state", state);
        spEditor.commit();
        Log.d(TAG, "state updated");

    }
    public void updateIncomingNumber(String inc_num,Context context){
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        spEditor = sp.edit();
        spEditor.putString("inc_num", inc_num);
        spEditor.commit();
        Log.d(TAG, "incoming number updated");
    }
    public String getCallState(Context context){
        sp1 = PreferenceManager.getDefaultSharedPreferences(context);
        String st =sp1.getString("call_state", "IDLE");
        Log.d(TAG,"get previous state as :"+st);
        return st;
    }
    public String getIncomingNumber(Context context){
        sp1 = PreferenceManager.getDefaultSharedPreferences(context);
        String st =sp1.getString("inc_num", "no_num");
        Log.d(TAG,"get incoming number as :"+st);
        return st;
    }
}
