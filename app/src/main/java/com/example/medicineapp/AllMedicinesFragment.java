package com.example.medicineapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllMedicinesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllMedicinesFragment extends Fragment {

    private FirebaseServices fbs;
    private ArrayList<Medicine> meds;
    private RecyclerView rvMeds;
    private MyAdapter adapter;
    private ProgressBar progressBar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllMedicinesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllMedicinesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllMedicinesFragment newInstance(String param1, String param2) {
        AllMedicinesFragment fragment = new AllMedicinesFragment();
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

       /* fbs = FirebaseServices.getInstance();
        meds = new ArrayList<>();
        rvMeds = getView().findViewById(R.id.rvMedicinesMedFragment);
        adapter = new MyAdapter(getActivity(), meds);
        rvMeds.setAdapter(adapter);
        rvMeds.setHasFixedSize(true);
        rvMeds.setLayoutManager(new LinearLayoutManager(getActivity()));*/

        fbs = FirebaseServices.getInstance();
        meds = new ArrayList<>();
        adapter = new MyAdapter(getActivity(), meds);
        rvMeds.setAdapter(adapter);
        rvMeds.setHasFixedSize(true);
        rvMeds.setLayoutManager(new LinearLayoutManager(getActivity()));

        /*fbs.getFire().collection("medicines").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot dataSnapshot: queryDocumentSnapshots.getDocuments()){
                    Medicine med = dataSnapshot.toObject(Medicine.class);

                    meds.add(med);
                }

                adapter.notifyDataSetChanged();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                Log.e("AllMedicinesFragment", e.getMessage());
            }
        });*/

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
}