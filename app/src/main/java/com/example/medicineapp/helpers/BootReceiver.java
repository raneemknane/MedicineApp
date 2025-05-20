package com.example.medicineapp.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.medicineapp.Java.FirebaseServices;
import com.example.medicineapp.Java.Medicine;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling alarms...");

            // Initialize Firebase
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = mAuth.getCurrentUser().getUid();

            // Reference to the user's medicines collection
            CollectionReference medicinesRef = db.collection("users")
                    .document(userId)
                    .collection("medicines");

            // Fetch all medicines and re-schedule alarms
            medicinesRef.get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Medicine medicine = document.toObject(Medicine.class);
                            String medicineId = document.getId();
                            String mealTime = medicine.getMealtime();

                            // Schedule the alarm for the medicine
                            scheduleAlarm(context, medicineId, mealTime);
                        }
                        Log.d("BootReceiver", "All alarms rescheduled successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BootReceiver", "Failed to fetch medicines: " + e.getMessage());
                    });
        }
    }

    private void scheduleAlarm(Context context, String medicineId, String mealTime) {
        try {
            // Parse the meal time into a Calendar instance
            String[] timeParts = mealTime.split(":");
            if (timeParts.length != 2) {
                Log.e("BootReceiver", "Invalid time format: " + mealTime);
                return;
            }
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Validate hour and minute ranges
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                Log.e("BootReceiver", "Invalid hour or minute: " + hour + ":" + minute);
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // If the time has already passed today, schedule it for the next day
            if (Calendar.getInstance().after(calendar)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Set the alarm using AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.putExtra("medicineId", medicineId); // Pass the medicine ID

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medicineId.hashCode(), // Unique request code based on medicine ID
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.d("BootReceiver", "Alarm rescheduled for medicine ID: " + medicineId);
            }
        } catch (Exception e) {
            Log.e("BootReceiver", "Error scheduling alarm: " + e.getMessage(), e);
        }
    }
}