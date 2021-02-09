package com.novoseltech.handymano;

import android.app.Activity;
import android.content.Intent;

public class Functions {

    public void redirectActivity(Activity activity, Class cClass){
        //Initialize intent
        Intent intent = new Intent(activity, cClass);
        //Set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }




}
