package com.example.medicineapp.function;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.medicineapp.Java.FirebaseServices;
import com.example.medicineapp.Java.Medicine;
import com.example.medicineapp.Java.Utils;
import com.example.medicineapp.R;
import com.example.medicineapp.helpers.AlarmReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class EditMedicineFragment extends Fragment {

    private EditText etName, etExpiration, etMealTime, etQuantity;
    private Button btnSave;
    private ImageView img;
    private FirebaseServices fbs;
    private Utils utils;
    private String medicineId;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public EditMedicineFragment() {
        // Required empty public constructor
    }

    public static EditMedicineFragment newInstance(String medicineId) {
        EditMedicineFragment fragment = new EditMedicineFragment();
        Bundle args = new Bundle();
        args.putString("medicineId", medicineId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_medicine, container, false);

        // Connect the back arrow ImageView
        ImageView ivBackArrowEdit = view.findViewById(R.id.ivBackArrowEditMed);
        ivBackArrowEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the previous fragment (e.g., AllMedicinesFragment)
                getParentFragmentManager().popBackStack();
            }
        });



        // Initialize views
        etName = view.findViewById(R.id.etNameEditMed);
        etExpiration = view.findViewById(R.id.etExpirationEditMed);
        etMealTime = view.findViewById(R.id.etMealTimeEditMed);
        etQuantity = view.findViewById(R.id.etQuantityEditMed);
        btnSave = view.findViewById(R.id.btnSaveMed);
        img = view.findViewById(R.id.imageViewEdit);

        // Initialize services
        fbs = FirebaseServices.getInstance();
        utils = Utils.getInstance();

        // Retrieve medicineId from arguments
        if (getArguments() != null) {
            medicineId = getArguments().getString("medicineId");
        }

        // Validate medicineId
        if (medicineId == null || medicineId.isEmpty()) {
            Toast.makeText(requireActivity(), "Invalid medicine ID", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack(); // Exit the fragment
            return view;
        }

        // Load medicine details
        loadMedicineDetails(medicineId);

        // Set up gallery launcher for image selection
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        img.setImageURI(selectedImageUri);

                        // Upload the image and handle the result
                        utils.uploadImage(requireActivity(), selectedImageUri, new Utils.ImageUploadCallback() {
                            @Override
                            public void onUploadSuccess(Uri downloadUrl) {
                                fbs.setSelectedImageURL(downloadUrl.toString());
                                Toast.makeText(requireActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onUploadFailure() {
                                Toast.makeText(requireActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );

        // Handle button clicks
        btnSave.setOnClickListener(v -> updateMedicine(medicineId));
        img.setOnClickListener(v -> openGallery());

        return view;
    }

    private void loadMedicineDetails(String medicineId) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        DocumentReference medicineRef = db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(medicineId);

        medicineRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Medicine medicine = documentSnapshot.toObject(Medicine.class);
                        if (medicine != null) {
                            etName.setText(medicine.getName());
                            etExpiration.setText(medicine.getExpiration());
                            etMealTime.setText(medicine.getMealtime());
                            etQuantity.setText(medicine.getQuantity());

                            String photoUrl = medicine.getPhoto();
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                utils.loadImageIntoImageView(img, photoUrl, requireActivity());
                                fbs.setSelectedImageURL(photoUrl); // Preserve the existing image URL
                            } else {
                                img.setImageResource(R.drawable.default_image);
                            }
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Medicine not found", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack(); // Exit the fragment
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Failed to load medicine details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Exit the fragment
                });
    }

    private void updateMedicine(String medicineId) {
        String nameString = etName.getText().toString().trim();
        String expirationString = etExpiration.getText().toString().trim();
        String mealtimeString = etMealTime.getText().toString().trim();
        String quantityString = etQuantity.getText().toString().trim();

        // Validate input
        if (nameString.isEmpty() || expirationString.isEmpty() || mealtimeString.isEmpty() || quantityString.isEmpty()) {
            utils.showMessageDialog(requireActivity(), "Some fields are empty or invalid");
            return;
        }

        // Validate mealtime format
        if (!isValidMealTime(mealtimeString)) {
            utils.showMessageDialog(requireActivity(), "Invalid meal time format. Please use HH:mm (e.g., 08:30 or 14:45).");
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        DocumentReference medicineRef = db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(medicineId);

        // Preserve the existing image URL if no new image is selected
        AtomicReference<String> photoUrl = new AtomicReference<>(fbs.getSelectedImageURL());
        if (photoUrl.get() == null || photoUrl.get().isEmpty()) {
            medicineRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Medicine existingMedicine = documentSnapshot.toObject(Medicine.class);
                            if (existingMedicine != null) {
                                photoUrl.set(existingMedicine.getPhoto()); // Update asynchronously

                                // Perform the update only after retrieving the photo URL
                                medicineRef.update(
                                                "name", nameString,
                                                "expiration", expirationString,
                                                "mealtime", mealtimeString,
                                                "quantity", quantityString,
                                                "photo", photoUrl.get() // Correct usage
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(requireActivity(), "Medicine updated successfully!", Toast.LENGTH_SHORT).show();

                                            // Set the updated alarm
                                            setAlarm(medicineId, mealtimeString);

                                            getParentFragmentManager().popBackStack(); // Go back to the previous fragment

                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireActivity(), "Failed to update medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), "Failed to retrieve existing image URL", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Update only the specified fields
            medicineRef.update(
                            "name", nameString,
                            "expiration", expirationString,
                            "mealtime", mealtimeString,
                            "quantity", quantityString,
                            "photo", photoUrl.get()
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireActivity(), "Medicine updated successfully!", Toast.LENGTH_SHORT).show();
                        // Set the updated alarm
                        setAlarm(medicineId, mealtimeString);
                        getParentFragmentManager().popBackStack(); // Go back to the previous fragment
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), "Failed to update medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }
    private boolean isValidMealTime(String mealTime) {
        // Trim the input to remove leading/trailing whitespace
        mealTime = mealTime.trim();
        Log.d("EditMedicineFragment", "Trimmed meal time input: " + mealTime);

        // Define the regex pattern for HH:mm format
        String timePattern = "^(\\d{2}):([0-5]\\d)$"; // Matches two digits for hours and valid minutes

        // Check if the input matches the pattern
        boolean isValid = Pattern.matches(timePattern, mealTime);
        Log.d("EditMedicineFragment", "Regex validation result: " + isValid);

        return isValid;
    }


    /**
     * Sets an alarm for the given medicine ID and meal time.
     */
    private void setAlarm(String medicineId, String mealTime) {
        try {
            Log.d("EditMedicineFragment", "Setting alarm for meal time: " + mealTime);

            // Trim the input to remove leading/trailing whitespace
            mealTime = mealTime.trim();

            // Validate the input format
            if (!isValidMealTime(mealTime)) {
                Log.e("EditMedicineFragment", "Invalid meal time format: " + mealTime);
                Toast.makeText(requireActivity(), "Invalid meal time format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the meal time into a Calendar instance
            String[] timeParts = mealTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Validate hour and minute ranges
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                Log.e("EditMedicineFragment", "Invalid hour or minute: " + hour + ":" + minute);
                Toast.makeText(requireActivity(), "Invalid meal time format", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("EditMedicineFragment", "Parsed hour: " + hour + ", minute: " + minute);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // If the time has already passed today, schedule it for the next day
            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Set the alarm using AlarmManager
            Intent intent = new Intent(requireActivity(), AlarmReceiver.class);
            intent.putExtra("medicineId", medicineId); // Pass the medicine ID

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireActivity(),
                    medicineId.hashCode(), // Unique request code based on medicine ID
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(requireActivity(), "Reminder updated for " + mealTime, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("EditMedicineFragment", "AlarmManager is null");
                Toast.makeText(requireActivity(), "Failed to update reminder", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("EditMedicineFragment", "Error setting alarm: " + e.getMessage(), e);
            Toast.makeText(requireActivity(), "Failed to update reminder", Toast.LENGTH_SHORT).show();
        }
    }
}