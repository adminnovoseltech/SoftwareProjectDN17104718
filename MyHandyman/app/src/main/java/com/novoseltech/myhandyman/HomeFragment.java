package com.novoseltech.myhandyman;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    private FirebaseFirestore fStore;
    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        fStore = FirebaseFirestore.getInstance();

        //Query
        Query query = fStore.collection("user").whereEqualTo("accountType", "Professional");
        //Recycler options
        FirestoreRecyclerOptions<ServicesModel> options = new FirestoreRecyclerOptions.Builder<ServicesModel>()
                .setQuery(query, ServicesModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ServicesModel, ServicesViewHolder>(options) {
            @NonNull
            @Override
            public ServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single, parent, false);
                return new ServicesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ServicesViewHolder holder, int position, @NonNull ServicesModel model) {
                holder.list_username.setText(model.getUsername());
                holder.list_category.setText(model.getCategory());
            }
        };



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_home, container, false);

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        fStoreList = (RecyclerView) view.findViewById(R.id.firestoreList);



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fStoreList.setHasFixedSize(true);
        fStoreList.setLayoutManager(new LinearLayoutManager(getContext()));
        fStoreList.setAdapter(adapter);

    }

    @Override
    public void onStop(){
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    private class ServicesViewHolder extends RecyclerView.ViewHolder{

        private TextView list_username;
        private TextView list_category;
        public ServicesViewHolder(@NonNull View itemView) {
            super(itemView);

            list_username = itemView.findViewById(R.id.list_username);
            list_category = itemView.findViewById(R.id.list_category);
        }
    }
}