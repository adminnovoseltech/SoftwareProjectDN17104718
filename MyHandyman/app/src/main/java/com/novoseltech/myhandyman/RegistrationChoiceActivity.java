package com.novoseltech.myhandyman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class RegistrationChoiceActivity extends AppCompatActivity {

    Functions func = new Functions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_choice);
    }

    public void ClickLooking(View view){
        func.redirectActivity(this, UserRegistrationActivity.class);
    }

    public void ClickOffer(View view){
        func.redirectActivity(this, BusinessRegistrationActivity.class);
    }

    public void ClickCancel(View view){
        func.redirectActivity(this, MainActivity.class);
    }
}
