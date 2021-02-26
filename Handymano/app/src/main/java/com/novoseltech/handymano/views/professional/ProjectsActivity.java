package com.novoseltech.handymano.views.professional;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.fragments.AddProject;
import com.novoseltech.handymano.model.ProjectsModel;
import com.novoseltech.handymano.views.standard.ViewProfessionalActivity;

import java.util.ArrayList;
import java.util.List;

public class ProjectsActivity extends AppCompatActivity {

    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;

    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        fStoreList = findViewById(R.id.firestoreListProjects);

        //Query
        Query query = fStore.collection("user")
                .document(user.getUid())
                .collection("projects")
                .orderBy("creation_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ProjectsModel> options = new FirestoreRecyclerOptions.Builder<ProjectsModel>()
                .setQuery(query, ProjectsModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ProjectsModel, ProjectsViewHolder>(options) {

            @NonNull
            @Override
            public ProjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_list_item_single, parent, false);
                return new ProjectsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProjectsViewHolder holder, int position, @NonNull ProjectsModel model) {
                holder.projectTitle.setText(model.getTitle());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(), holder.projectTitle.getText(), Toast.LENGTH_SHORT).show();

                        Intent intent  = new Intent(getApplicationContext(), ProfessionalProject.class);
                        intent.putExtra("PROJECT_ID", holder.projectTitle.getText());
                        startActivity(intent);
                    }
                });
            }
        };

        fStoreList.setHasFixedSize(true);
        fStoreList.setLayoutManager(new LinearLayoutManager(this));
        fStoreList.setAdapter(adapter);





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
                fStoreList.setVisibility(View.GONE);
            }
        });

    }

    private class ProjectsViewHolder  extends RecyclerView.ViewHolder{

        private TextView projectTitle;

        public ProjectsViewHolder(@NonNull View itemView) {
            super(itemView);

            projectTitle = itemView.findViewById(R.id.list_projectTitle);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
}