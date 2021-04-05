package com.lmb.fr.lmbapplicationnouga;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class ManageSettings {

    public static Boolean isRecord;
    // Nom du fichier shareprefs
    public static final String PREFS_PRIVATE = "data";
    private SharedPreferences prefsPrivate;

    public void saveData(Context context, HashMap<String, String> data){
        prefsPrivate = context.getSharedPreferences(PREFS_PRIVATE, Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsPrivateEditor = prefsPrivate.edit();
        prefsPrivateEditor.putString("TEL_PORTAIL", data.get("TEL_PORTAIL").trim());
        prefsPrivateEditor.putString("GROUPE", data.get("GROUPE").trim());
    }

    public void restoreData(Context context, HashMap<String, String> data){
        SharedPreferences myPrefs = context.getSharedPreferences("data", Context.MODE_PRIVATE);

        data.put("TEL_PORTAIL", myPrefs.getString("TEL_PORTAIL", null));
        data.put("GROUPE", myPrefs.getString("GROUPE", null));
    }

}
