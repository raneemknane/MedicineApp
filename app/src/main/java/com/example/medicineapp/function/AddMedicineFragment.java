package com.example.medicineapp.function;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.medicineapp.Mains.MainPageFragment;
import com.example.medicineapp.helpers.AlarmReceiver;
import com.example.medicineapp.Java.FirebaseServices;
import com.example.medicineapp.Java.Medicine;
import com.example.medicineapp.R;
import com.example.medicineapp.Java.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddMedicineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddMedicineFragment extends Fragment {

    private EditText etName, etExpiration, etMealTime, etQuantity;
    private Button btnAdd;
    private FirebaseServices fbs;
    private Uri selectedImageUri;

    private static final int GALLERY_REQUEST_CODE = 123;

    ImageView img;

    private Utils utils;
    private ActivityResultLauncher<Intent> galleryLauncher;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddMedicineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddMedicineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddMedicineFragment newInstance(String param1, String param2) {
        AddMedicineFragment fragment = new AddMedicineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AddMedicineFragment", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        img.setImageURI(selectedImageUri);

                        // Upload the image and handle the result
                        utils.uploadImage(requireActivity(), selectedImageUri, new Utils.ImageUploadCallback() {
                            @Override
                            public void onUploadSuccess(Uri downloadUrl) {
                                fbs.setSelectedImageURL(downloadUrl.toString());
                                Toast.makeText(requireActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                selectedImageUri = null;

                            }

                            @Override
                            public void onUploadFailure() {
                                Toast.makeText(requireActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();

                                // Reset the selectedImageUri in case of failure
                                selectedImageUri = null;
                            }
                        });
                    } else {
                        // Reset the selectedImageUri if the user cancels the gallery picker
                        selectedImageUri = null;
                    }
                }
        );




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("AddMedicineFragment", "onCreateView");
        // Inflate the layout for this fragment
        /*return inflater.inflate(R.layout.fragment_add_medicine, container, false);*/
        View view = inflater.inflate(R.layout.fragment_add_medicine, container, false);

        // Connect the back arrow ImageView
        ImageView ivBackArrow = view.findViewById(R.id.ivBackArrowAddMed);
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

        // Rest of your existing code...
        return view;
    }




    @Override
    public void onStart() {
        Log.d("AddMedicineFragment", "onStart");
        super.onStart();
        utils = Utils.getInstance();
        fbs = FirebaseServices.getInstance();

        // Initialize all view components here
        etName = getView().findViewById(R.id.etNameAddMed);
        etExpiration = getView().findViewById(R.id.etExpirationAddMed);
        etMealTime = getView().findViewById(R.id.etMealTimeAddMed);
        etQuantity = getView().findViewById(R.id.etQuantityAddMed);
        btnAdd = getView().findViewById(R.id.btnAddMed);
        img = getView().findViewById(R.id.imageView);

        btnAdd.setOnClickListener(v -> addMedicine());
        img.setOnClickListener(v -> openGallery());



    }
    @SuppressLint("ScheduleExactAlarm")
    private void addMedicine() {
        String nameString = etName.getText().toString().trim();
        String expirationString = etExpiration.getText().toString().trim();
        String mealtimeString = etMealTime.getText().toString().trim();
        String quantityString = etQuantity.getText().toString().trim();

        // Validate input
        if (nameString.isEmpty() || expirationString.isEmpty() || mealtimeString.isEmpty() || quantityString.isEmpty() ) {
            utils.showMessageDialog(requireActivity(), "Some fields are empty or invalid");
            return;
        }

        // Validate meal time format
        if (!isValidMealTime(mealtimeString)) {
            utils.showMessageDialog(requireActivity(), "Invalid meal time format. Please use HH:mm (e.g., 08:30 or 14:45).");
            return;
        }


        // Validate image selection
        if (selectedImageUri == null && (fbs.getSelectedImageURL() == null || fbs.getSelectedImageURL().isEmpty())) {
            utils.showMessageDialog(requireActivity(), "Please select an image");
            return;
        }




        // Create a Medicine object

        // Create a Medicine object
        Medicine medicine = new Medicine(
                nameString,
                expirationString,
                mealtimeString,
                quantityString,
                fbs.getSelectedImageURL()
        );

        // Save medicine details to Firestore under the current user's collection
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference medicinesRef = db.collection("users")
                .document(userId)
                .collection("medicines");

        medicinesRef.add(medicine)
                .addOnSuccessListener(documentReference -> {
                    String medicineId = documentReference.getId();
                    medicine.setId(medicineId);

                    // Update the Firestore document with the medicine ID
                    documentReference.update("id", medicineId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AddMedicineFragment", "Medicine added successfully");

                                // Reset the ImageView, selectedImageUri, and fbs.setSelectedImageURL()
                                img.setImageDrawable(null);
                                selectedImageUri = null;
                                fbs.setSelectedImageURL(null);

                                Toast.makeText(requireActivity(), "Medicine added successfully!", Toast.LENGTH_SHORT).show();
                                setAlarm(medicineId, mealtimeString); // Set an alarm for the meal time
                            })
                            .addOnFailureListener(e -> {
                                Log.e("AddMedicineFragment", "Failed to update medicine ID: " + e.getMessage());
                                Toast.makeText(requireActivity(), "Failed to update medicine ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AddMedicineFragment", "Failed to add medicine: " + e.getMessage());
                    Toast.makeText(requireActivity(), "Failed to add medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private boolean isValidMealTime(String mealTime) {
        // Trim the input to remove leading/trailing whitespace
        mealTime = mealTime.trim();
        Log.d("AddMedicineFragment", "Trimmed meal time input: " + mealTime);

        // Define the regex pattern for HH:mm format
        String timePattern = "^(\\d{2}):([0-5]\\d)$"; // Matches two digits for hours and valid minutes

        // Check if the input matches the pattern
        boolean isValid = Pattern.matches(timePattern, mealTime);
        Log.d("AddMedicineFragment", "Regex validation result: " + isValid);

        return isValid;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = requireActivity().getPackageName();
            PowerManager powerManager = (PowerManager) requireActivity().getSystemService(Context.POWER_SERVICE);

            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
                Toast.makeText(requireActivity(), "Please disable battery optimization for this app.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void setAlarm(String medicineId, String mealTime) {
        try {
            Log.d("AddMedicineFragment", "Setting alarm for meal time: " + mealTime);

            // Check battery optimization settings
            checkBatteryOptimization();

            // Trim the input to remove leading/trailing whitespace
            mealTime = mealTime.trim();

            // Validate the input format
            if (!isValidMealTime(mealTime)) {
                Log.e("AddMedicineFragment", "Invalid meal time format: " + mealTime);
                Toast.makeText(requireActivity(), "Invalid meal time format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the meal time into a Calendar instance
            String[] timeParts = mealTime.split(":");
            if (timeParts.length != 2) {
                Log.e("AddMedicineFragment", "Invalid time format: " + mealTime);
                Toast.makeText(requireActivity(), "Invalid meal time format", Toast.LENGTH_SHORT).show();
                return;
            }

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Validate hour and minute ranges
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                Log.e("AddMedicineFragment", "Invalid hour or minute: " + hour + ":" + minute);
                Toast.makeText(requireActivity(), "Invalid meal time format", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("AddMedicineFragment", "Parsed hour: " + hour + ", minute: " + minute);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // If the time has already passed today, schedule it for the next day
            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Check and request notification permission for Android 13+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            requireActivity(),
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            100 // Request code (can be any unique integer)
                    );
                    Toast.makeText(requireActivity(), "Please grant notification permission to receive reminders.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method until permission is granted
                }
            }

            // Check if the app can schedule exact alarms (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                    Toast.makeText(requireActivity(), "Please enable exact alarm permissions in settings.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method until permission is granted
                }
            }

            // Set the alarm using AlarmManager

            AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e("AddMedicineFragment", "AlarmManager is null");
                return;
            }
            // Create an Intent to trigger the AlarmReceiver
            Intent intent = new Intent(requireActivity(), AlarmReceiver.class);
            intent.putExtra("medicineId", medicineId); // Pass the medicine ID

            // Create a PendingIntent to wrap the Intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireActivity(),
                    medicineId.hashCode(), // Use a unique request code based on the medicine ID
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Flags for compatibility
            );

            // Schedule the alarm
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(requireActivity(), "Reminder set for " + mealTime, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AddMedicineFragment", "Error setting alarm: " + e.getMessage(), e);
            Toast.makeText(requireActivity(), "Invalid meal time format", Toast.LENGTH_SHORT).show();
        }
    }




}