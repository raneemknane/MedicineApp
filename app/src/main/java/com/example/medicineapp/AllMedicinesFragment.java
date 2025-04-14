package com.example.medicineapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;


public class AllMedicinesFragment extends Fragment implements MyAdapter.OnItemClickListener {

    private FirebaseServices fbs;
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

        rvMeds = view.findViewById(R.id.rvMedicinesMedFragment);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();



        fbs = FirebaseServices.getInstance();
        meds = new ArrayList<>();
        adapter = new MyAdapter(getActivity(), meds,this);
        rvMeds.setAdapter(adapter);
        rvMeds.setHasFixedSize(true);
        rvMeds.setLayoutManager(new LinearLayoutManager(getActivity()));





        progressBar.setVisibility(View.VISIBLE);

        fbs.getFire().collection("medicines").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot dataSnapshot : queryDocumentSnapshots.getDocuments()) {
                        Medicine med = dataSnapshot.toObject(Medicine.class);
                        if (med != null) {
                            meds.add(med);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Hide progress bar after data is loaded
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                    Log.e("AllMedicinesFragment", e.getMessage());

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