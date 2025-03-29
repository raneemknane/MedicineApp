package com.example.medicineapp;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class Utils {

    private static Utils instance;

    private FirebaseServices fbs;
    private String imageStr;

    public Utils()
    {
        fbs = FirebaseServices.getInstance();
    }

    public static Utils getInstance()
    {
        if (instance == null)
            instance = new Utils();

        return instance;
    }

    public void showMessageDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(message);
        //builder.setMessage(message);

        // Add a button to dismiss the dialog box
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You can perform additional actions here if needed
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void uploadImage(Context context, Uri selectedImageUri, ImageUploadCallback callback) {
        if (selectedImageUri == null) {
            Toast.makeText(context, "Please choose an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = fbs.getStorage().getReference().child("images/" + imageName);

        UploadTask uploadTask = imageRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                fbs.setSelectedImageURL(uri.toString());
                callback.onUploadSuccess(uri);
            }).addOnFailureListener(e -> {
                Log.e("Utils: uploadImage", "Failed to get download URL: " + e.getMessage());
                callback.onUploadFailure();
            });
        }).addOnFailureListener(e -> {
            Log.e("Utils: uploadImage", "Failed to upload image: " + e.getMessage());
            callback.onUploadFailure();
        });
    }

    public interface ImageUploadCallback {
        void onUploadSuccess(Uri downloadUrl);
        void onUploadFailure();
    }


}
