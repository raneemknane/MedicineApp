package com.example.medicineapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
        /*galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        img.setImageURI(selectedImageUri);
                        utils.uploadImage(getActivity(), selectedImageUri);
                    }
                }
        );*/

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
                            }

                            @Override
                            public void onUploadFailure() {
                                Toast.makeText(requireActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("AddMedicineFragment", "onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_medicine, container, false);
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
    private void addMedicine() {
        String nameString = etName.getText().toString().trim();
        String expirationString = etExpiration.getText().toString().trim();
        String mealtimeString = etMealTime.getText().toString().trim();
        String quantityString = etQuantity.getText().toString().trim();

        // Validate input
        if (nameString.isEmpty() || expirationString.isEmpty() || mealtimeString.isEmpty() || quantityString.isEmpty() || selectedImageUri == null) {
            utils.showMessageDialog(requireActivity(), "Some fields are empty or invalid");
            return;
        }

        Medicine medicine;
        if (fbs.getSelectedImageURL() == null) {
            medicine = new Medicine(nameString, expirationString, mealtimeString, quantityString, "");
        } else {
            medicine = new Medicine(nameString, expirationString, mealtimeString, quantityString, fbs.getSelectedImageURL());
        }

        fbs.getFire().collection("medicines").add(medicine)
                .addOnSuccessListener(documentReference -> Toast.makeText(requireActivity(), "Success", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireActivity(), "Failure", Toast.LENGTH_SHORT).show());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }


    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            img.setImageURI(selectedImageUri);
            utils.uploadImage(getActivity(), selectedImageUri);
        }
    } */
}