package com.example.medicineapp;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;



    public class FirebaseServices {

        private static FirebaseServices instance;
        private FirebaseAuth auth;
        private FirebaseFirestore fire;
        private FirebaseStorage storage;
        private String selectedImageURL; // Change from Uri to String

        public FirebaseServices() {
            auth = FirebaseAuth.getInstance();
            fire = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
        }

        public String getSelectedImageURL() { // Change return type to String
            return selectedImageURL;
        }

        public void setSelectedImageURL(String selectedImageURL) { // Change parameter type to String
            this.selectedImageURL = selectedImageURL;
        }

        public FirebaseStorage getStorage() {
            return storage;
        }

        public FirebaseFirestore getFire() {
            return fire;
        }

        public FirebaseAuth getAuth() {
            return auth;
        }

        public static FirebaseServices getInstance() {
            if (instance == null) {
                instance = new FirebaseServices();
            }
            return instance;
        }
    }
