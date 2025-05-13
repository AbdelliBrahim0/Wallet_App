package com.example.be9ik_wallet;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QRScannerActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Forcer l'orientation portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
} 