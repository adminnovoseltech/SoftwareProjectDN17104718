package com.novoseltech.handymano;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class RegistrationChoiceActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_choice);
    }

    public void ClickLooking(View view){

        Intent intent = new Intent(this, UserRegistrationActivity.class);
        startActivity(intent);
    }

    public void ClickOffer(View view){
        Intent intent = new Intent(this, BusinessRegistrationActivity.class);
        startActivity(intent);
    }

    public void ClickCancel(View view){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
