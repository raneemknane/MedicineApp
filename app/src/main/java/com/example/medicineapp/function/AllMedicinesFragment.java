package com.example.medicineapp.function;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.medicineapp.Java.FirebaseServices;
import com.example.medicineapp.Java.Medicine;
import com.example.medicineapp.Java.MyAdapter;
import com.example.medicineapp.Mains.MainPageFragment;
import com.example.medicineapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class AllMedicinesFragment extends Fragment implements MyAdapter.OnItemClickListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    /*private FirebaseServices fbs;*/
    private ArrayList<Medicine> meds;
    private RecyclerView rvMeds;
    private MyAdapter adapter;
    private ProgressBar progressBar;


    public AllMedicinesFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_medicines, container, false);
        // Connect the back arrow ImageView
        ImageView ivBackArrow = view.findViewById(R.id.ivBackArrowAllMeds);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MainPageFragment()) // Replace with your container ID
                        .addToBackStack(null) // Optional: Add to back stack if you want users to return
                        .commit();
            }
        });
        rvMeds = view.findViewById(R.id.rvMedicinesMedFragment);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if the user is logged in
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid(); // Get the current user's ID

        /*fbs = FirebaseServices.getInstance();*/
        meds = new ArrayList<>();
        adapter = new MyAdapter(getActivity(), meds,this);
        rvMeds.setAdapter(adapter);
        rvMeds.setHasFixedSize(true);
        rvMeds.setLayoutManager(new LinearLayoutManager(getActivity()));





        progressBar.setVisibility(View.VISIBLE);


        // Reference the user's medicines subcollection
        db.collection("users")
                .document(userId)
                .collection("medicines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    meds.clear(); // Clear the list before adding new data
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        Medicine med = documentSnapshot.toObject(Medicine.class);
                        if (med != null) {
                            meds.add(med);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Hide progress bar after data is loaded
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to fetch medicines: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AllMedicinesFragment", "Error fetching medicines", e);

                    // Hide progress bar on failure
                    progressBar.setVisibility(View.GONE);
                });
    }

    // Handle item click
    @Override
    public void onItemClick(Medicine medicine) {
        // Navigate to the detail fragment
        MedicineDetailFragment detailFragment = MedicineDetailFragment.newInstance(medicine);

        // Use Fragment Transactions to replace the current fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment) // Replace with your container ID
                .addToBackStack(null) // Add to back stack so users can return
                .commit();
    }
}