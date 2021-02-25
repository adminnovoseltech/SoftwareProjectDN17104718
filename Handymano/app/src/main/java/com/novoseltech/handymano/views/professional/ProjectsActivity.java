package com.novoseltech.handymano.views.professional;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.novoseltech.handymano.R;
import com.novoseltech.handymano.fragments.AddProject;

public class ProjectsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);





        Button btn_createProject = findViewById(R.id.btn_createProject);
        btn_createProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddProject addProject_fragment = new AddProject();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_project_add, addProject_fragment);
                transaction.addToBackStack(null);
                transaction.commit();

                btn_createProject.setVisibility(View.GONE);
            }
        });

    }
}