package com.mobile.helping.women;

import static com.mobile.helping.women.PermissionManager.REQUEST_CODE_SUCCESS;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mobile.helping.women.databinding.ActivityInitialBinding;


public class InitialActivity extends AppCompatActivity {
    private ActivityInitialBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_initial);

        if (PermissionManager.isAllPermissionsApproved(this)
                && PermissionManager.isGPSEnabled(this)) {
            startMainActivity();
        }
        binding.btnAllow.setOnClickListener(view -> {
            PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
        });

        if (!PermissionManager.isGPSEnabled(this)) {
            PermissionManager.showSnackBarForGPS(binding.getRoot(), this);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (PermissionManager.isAllPermissionsApproved(this)) {
            startMainActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_SUCCESS) {
            if (PermissionManager.isAllPermissionsApproved(this)
                    && PermissionManager.isGPSEnabled(this)) {
                startMainActivity();
            } else if (!PermissionManager.isGPSEnabled(this)) {
                PermissionManager.showSnackBarForGPS(binding.getRoot(), this);
            } else {
                PermissionManager.showSnackBarForRationale(binding.getRoot(), this);
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
