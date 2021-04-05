package com.lmb.fr.lmbapplicationnouga;

import android.content.Context;

public class Controller {
    Context myContext;
    String tel;
    String groupe;
    Boolean modeInit = false;
    Boolean unlock = false;
    ManageSettings manageData;
    LMB_Application lmbApplication;

    Controller(Context myContext, LMB_Application lmbApplication){
        this.myContext = myContext;
        this.lmbApplication = lmbApplication;

        manageData = new ManageSettings();

        // chargeenr des parametres
        loadSettings();
    }

    public Boolean getRecord(){
        return ManageSettings.isRecord;
    }

    public String getTel(){
        return tel;
    }

    public String getGroupe(){
        return groupe;
    }

    public void loadSettings(){

    }
}