package com.novoseltech.handymano.views.standard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.novoseltech.handymano.R;

public class ViewProfessionalActivity extends AppCompatActivity {

    TextView tv_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_professional);

        String user_id = getIntent().getStringExtra("USER_ID");

        tv_uid = findViewById(R.id.tv_uid);

        tv_uid.setText(user_id);
    }


}