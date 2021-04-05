package com.novoseltech.handymano.views.standard.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.ProjectsModel;
import com.novoseltech.handymano.views.professional.project.ProfessionalProjectViewActivity;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;
import com.novoseltech.handymano.views.standard.ViewProfessionalActivity;


public class ProjectList extends AppCompatActivity {

    private RecyclerView rv_tradeProjectList;
    private FirestoreRecyclerAdapter adapter;

    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        String user_id = getIntent().getStringExtra("USER_ID");

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        rv_tradeProjectList = findViewById(R.id.rv_tradeProjectList);

        //Query
        Query query = fStore.collection("user")
                .document(user_id)
                .collection("projects")
                .orderBy("creation_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ProjectsModel> options = new FirestoreRecyclerOptions.Builder<ProjectsModel>()
                .setQuery(query, ProjectsModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ProjectsModel, ProjectList.ProjectsViewHolder>(options) {

            @NonNull
            @Override
            public ProjectList.ProjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_list_item_single, parent, false);
                return new ProjectList.ProjectsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProjectList.ProjectsViewHolder holder, int position, @NonNull ProjectsModel model) {
                holder.projectTitle.setText(model.getTitle());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent  = new Intent(getApplicationContext(), ViewProject.class);
                        intent.putExtra("USER_ID", user_id);
                        intent.putExtra("PROJECT_ID", holder.projectTitle.getText());
                        //intent.putExtra("PROJECT_ID", holder.itemView.getId())
                        startActivity(intent);
                    }
                });
            }
        };

        rv_tradeProjectList.setHasFixedSize(true);
        rv_tradeProjectList.setLayoutManager(new LinearLayoutManager(this));
        rv_tradeProjectList.setAdapter(adapter);
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