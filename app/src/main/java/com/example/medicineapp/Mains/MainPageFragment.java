package com.example.medicineapp.Mains;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medicineapp.BlueTooths.ChooseService;
import com.example.medicineapp.R;
import com.example.medicineapp.function.AddMedicineFragment;
import com.example.medicineapp.function.AllMedicinesFragment;
import com.example.medicineapp.Mains.MainFragment;
import com.example.medicineapp.function.LoginFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class MainPageFragment extends Fragment {

    private static final String TAG = "MainPageFragment";
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Firestore (if needed)
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setup button listeners
        view.findViewById(R.id.btnAddMed).setOnClickListener(v -> gotoAddMedicineFragment());
        view.findViewById(R.id.btnViewAllMeds).setOnClickListener(v -> gotoAllMedicinesFragment());
        view.findViewById(R.id.btnBT).setOnClickListener(v -> gotoChooseService());
        view.findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> showDeleteAccountDialog());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void gotoAddMedicineFragment() {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new AddMedicineFragment());
        ft.addToBackStack(null);
        ft.commit();
    }

    private void gotoAllMedicinesFragment() {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new AllMedicinesFragment());
        ft.addToBackStack(null);
        ft.commit();
    }

    private void gotoChooseService() {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new ChooseService());
        ft.addToBackStack(null);
        ft.commit();
    }

    private void logout() {
        mAuth.signOut();
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MainFragment());
        ft.commit();
    }

    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This will remove all your data.")
                .setPositiveButton("Delete Account", (dialog, which) -> deleteUserAndData())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserAndData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: Delete user document
        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User doc deleted"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting user doc", e));

        // Step 2: Delete subcollections (e.g., medicines)
        db.collection("users").document(user.getUid()).collection("medicines")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : task.getResult()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit().addOnSuccessListener(aVoid ->
                                Log.d(TAG, "All medicines deleted"));
                    }
                });

        // Step 3: Delete Auth user
        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(requireContext(), MainActivity.class));
                        requireActivity().finish();
                        Toast.makeText(requireContext(), "Account Deleted Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}