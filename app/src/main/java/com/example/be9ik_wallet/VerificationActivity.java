package com.example.be9ik_wallet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VerificationActivity extends AppCompatActivity {
    private ActivityResultLauncher<Uri> frontIdCameraLauncher;
    private ActivityResultLauncher<Uri> backIdCameraLauncher;
    private ActivityResultLauncher<Uri> selfieFrontCameraLauncher;
    private ActivityResultLauncher<Uri> selfieBackCameraLauncher;

    private Uri currentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        // Initialize camera launchers
        initializeCameraLaunchers();

        // Setup button click listeners
        setupCameraButtons();
    }

    private void initializeCameraLaunchers() {
        frontIdCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    ImageView imgFrontId = findViewById(R.id.img_front_id);
                    imgFrontId.setImageURI(currentPhotoUri);
                }
            }
        );

        backIdCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    ImageView imgBackId = findViewById(R.id.img_back_id);
                    imgBackId.setImageURI(currentPhotoUri);
                }
            }
        );

        selfieFrontCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    ImageView imgSelfieFront = findViewById(R.id.img_selfie_front);
                    imgSelfieFront.setImageURI(currentPhotoUri);
                }
            }
        );

        selfieBackCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    ImageView imgSelfieBack = findViewById(R.id.img_selfie_back);
                    imgSelfieBack.setImageURI(currentPhotoUri);
                }
            }
        );
    }

    private void setupCameraButtons() {
        MaterialButton btnFrontId = findViewById(R.id.btn_upload_front_id);
        MaterialButton btnBackId = findViewById(R.id.btn_upload_back_id);
        MaterialButton btnSelfieFront = findViewById(R.id.btn_upload_selfie_front);
        MaterialButton btnSelfieBack = findViewById(R.id.btn_upload_selfie_back);

        btnFrontId.setOnClickListener(v -> openCamera(CameraType.FRONT_ID));
        btnBackId.setOnClickListener(v -> openCamera(CameraType.BACK_ID));
        btnSelfieFront.setOnClickListener(v -> openCamera(CameraType.SELFIE_FRONT));
        btnSelfieBack.setOnClickListener(v -> openCamera(CameraType.SELFIE_BACK));
    }

    private void openCamera(CameraType type) {
        if (checkCameraPermission()) {
            try {
                File photoFile = createImageFile();
                currentPhotoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);

                switch (type) {
                    case FRONT_ID:
                        frontIdCameraLauncher.launch(currentPhotoUri);
                        break;
                    case BACK_ID:
                        backIdCameraLauncher.launch(currentPhotoUri);
                        break;
                    case SELFIE_FRONT:
                        selfieFrontCameraLauncher.launch(currentPhotoUri);
                        break;
                    case SELFIE_BACK:
                        selfieBackCameraLauncher.launch(currentPhotoUri);
                        break;
                }
            } catch (IOException ex) {
                Toast.makeText(this, "Erreur lors de la création du fichier photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can open the camera
                Toast.makeText(this, "Autorisation caméra accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Autorisation caméra refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
    }

    private enum CameraType {
        FRONT_ID,
        BACK_ID,
        SELFIE_FRONT,
        SELFIE_BACK
    }

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
}