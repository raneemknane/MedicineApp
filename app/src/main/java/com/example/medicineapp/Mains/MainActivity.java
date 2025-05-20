package com.example.medicineapp.Mains;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medicineapp.BlueTooths.ChooseService;
import com.example.medicineapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // Check if a specific fragment was requested
        Intent intent = getIntent();
        if (intent.hasExtra("fragment")) {
            String fragmentName = intent.getStringExtra("fragment");

            if ("ChooseService".equals(fragmentName)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ChooseService())
                        .commit();
            }
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Adjust padding for system bars (e.g., status bar and navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add an AuthStateListener to monitor authentication state changes
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove the AuthStateListener when the activity stops
        mAuth.removeAuthStateListener(authStateListener);
    }

    // Listener for authentication state changes
    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentUser != null) {
            // User is signed in
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null && !(currentFragment instanceof MainPageFragment) ) {
                gotoMainPageFragment();
            }
        } else {
            // User is signed out
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null && !(currentFragment instanceof MainFragment) ) {
                gotoMainFragment();
            }
        }
    };

    // Navigate to LoginFragment
    private void gotoMainFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MainFragment());
        ft.commit();
    }

    // Navigate to MainPageFragment
    private void gotoMainPageFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new MainPageFragment());
        ft.commit();
    }
}