package com.example.medicineapp.Mains;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.medicineapp.function.LoginFragment;
import com.example.medicineapp.R;
import com.example.medicineapp.function.SignUpFragment;

public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Find buttons
        View btnLogin = view.findViewById(R.id.btnLoginMain);
        View btnSignUp = view.findViewById(R.id.btnSignUpMain);

        // Set click listeners
        btnLogin.setOnClickListener(v -> navigateToFragment(new LoginFragment()));
        btnSignUp.setOnClickListener(v -> navigateToFragment(new SignUpFragment()));

        return view;
    }

    // Helper method to navigate to a new fragment
    private void navigateToFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace the current fragment with the new one
        transaction.replace(R.id.fragment_container, fragment);

        // Add the transaction to the back stack so users can return
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}