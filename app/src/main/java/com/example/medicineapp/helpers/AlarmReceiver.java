package com.example.medicineapp.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.medicineapp.Java.Medicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the medicine ID from the Intent
        String medicineId = intent.getStringExtra("medicineId");

        if (medicineId == null) {
            Log.e("AlarmReceiver", "No medicine ID found in intent");
            return;
        }

        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference the specific medicine document in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("medicines")
                .document(medicineId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Medicine medicine = documentSnapshot.toObject(Medicine.class);
                        if (medicine != null) {
                            // Display personalized notification
                            displayNotification(context, medicine.getName());
                        }
                    } else {
                        Log.e("AlarmReceiver", "Medicine document does not exist: " + medicineId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AlarmReceiver", "Error fetching medicine: " + e.getMessage());
                });
    }

    private void displayNotification(Context context, String medicineName) {
        // Get the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "medicine_reminder", // Channel ID
                    "Medicine Reminder", // Channel Name
                    NotificationManager.IMPORTANCE_HIGH // Importance level
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        Notification notification = new Notification.Builder(context, "medicine_reminder")
                .setContentTitle("Medicine Reminder") // Title of the notification
                .setContentText("It's time to take " + medicineName) // Personalized content
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Icon for the notification
                .setAutoCancel(true) // Dismiss the notification when clicked
                .build();

        // Display the notification
        notificationManager.notify(medicineName.hashCode(), notification); // Use a unique ID for each medicine
    }
}