package com.example.medicineapp.function;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.medicineapp.Java.Medicine;
import com.example.medicineapp.R;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicineDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicineDetailFragment extends Fragment {

    private Medicine medicine;



    public MedicineDetailFragment() {
        // Required empty public constructor
    }


    /**
     * Factory method to create a new instance of MedicineDetailFragment.
     *
     * @param medicine The Medicine object to display in the fragment.
     * @return A new instance of MedicineDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MedicineDetailFragment newInstance( Medicine medicine) {
        MedicineDetailFragment fragment = new MedicineDetailFragment();
        Bundle args = new Bundle();

        // Pass the Medicine object using Serializable
        args.putSerializable("medicine", medicine);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            medicine = (Medicine) getArguments().getSerializable("medicine");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_medicine_detail, container, false);
        View view = inflater.inflate(R.layout.fragment_medicine_detail, container, false);

        // Connect the back arrow ImageView
        ImageView ivBackArrow = view.findViewById(R.id.ivBackArrowMedDetail);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the previous fragment (e.g., AllMedicinesFragment)
                getParentFragmentManager().popBackStack();
            }
        });

        // Initialize views
        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvExpiration = view.findViewById(R.id.tvDetailExpiration);
        TextView tvMealTime = view.findViewById(R.id.tvDetailMealTime);
        TextView tvQuantity = view.findViewById(R.id.tvDetailQuantity);
        ImageView ivPhoto = view.findViewById(R.id.ivDetailPhoto);
        Button btnEdit = view.findViewById(R.id.btnEditMedicine);
        Button btnDelete = view.findViewById(R.id.btnDeleteMed);

        // Bind data to views
        if (medicine != null) {
            tvName.setText(medicine.getName());
            tvExpiration.setText(medicine.getExpiration());
            tvMealTime.setText(medicine.getMealtime());
            tvQuantity.setText(medicine.getQuantity());

            String photoUrl = medicine.getPhoto();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Picasso.get()
                        .load(photoUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivPhoto);
            } else {
                ivPhoto.setImageResource(R.drawable.default_image);
            }

        }




        // Handle Edit button click
        btnEdit.setOnClickListener(v -> {
            if (medicine != null) {
                navigateToEditMedicineFragment(medicine.getId());
            }
        });

        // Handle Delete button click
        btnDelete.setOnClickListener(v -> {
            if (medicine != null) {
                showDeleteConfirmationDialog(medicine);
            } else {
                Toast.makeText(requireActivity(), "No medicine data available to delete", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void navigateToEditMedicineFragment(String medicineId) {
        // Create a new instance of EditMedicineFragment
        EditMedicineFragment editFragment = EditMedicineFragment.newInstance(medicineId);

        // Navigate to the EditMedicineFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment) // Replace with your container ID
                .addToBackStack(null) // Add to back stack so users can return
                .commit();
    }

    private void showDeleteConfirmationDialog(Medicine medicine) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this medicine?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMedicine(medicine);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteMedicine(Medicine medicine) {
        if (medicine == null || medicine.getId() == null) {
            Toast.makeText(requireActivity(), "Invalid medicine data", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        DocumentReference medicineRef = db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(medicine.getId());

        medicineRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireActivity(), "Medicine deleted successfully!", Toast.LENGTH_SHORT).show();

                    String photoUrl = medicine.getPhoto();
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        deleteImageFromStorage(photoUrl);
                    }

                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Failed to delete medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteImageFromStorage(String photoUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl(photoUrl);

        imageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("MedicineDetailFragment", "Associated image deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("MedicineDetailFragment", "Failed to delete associated image: " + e.getMessage());
                });
    }



}