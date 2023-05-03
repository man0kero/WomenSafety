package com.mobile.helping.women;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;


public class PermissionManager {
    private static final int REQUEST_CODE_CALL_PHONE = 111;
    private static final int REQUEST_CODE_CAMERA = 222;
    private static final int REQUEST_CODE_RECORD_AUDIO = 333;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 444;



    static String[] perm = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static boolean isCallPhonePermissionApproved(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isCameraPermissionApproved(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isRecordAudioPermissionApproved(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean isAccessFineLocationPermissionApproved(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isWriteExternalStoragePermissionApproved(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean isReadExternalStoragePermissionApproved(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean isRead13(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    public static boolean isAllPermissionsApproved(Context context) {
        return isCallPhonePermissionApproved(context)
                && isAccessFineLocationPermissionApproved(context)
                && isCameraPermissionApproved(context)
                && isRecordAudioPermissionApproved(context)
                && isWriteExternalStoragePermissionApproved(context)
                && isReadExternalStoragePermissionApproved(context)
                && isRead13(context);
    }

    public static void requestPermissions(Activity activity, int requestCode) {
        List<String> permissions = new ArrayList<>();
        if (!isCallPhonePermissionApproved(activity)) {
            permissions.add(perm[0]);
        }
        if (!isCameraPermissionApproved(activity)) {
            permissions.add(perm[1]);
        }
        if (!isRecordAudioPermissionApproved(activity)) {
            permissions.add(perm[2]);
        }
        if (!isAccessFineLocationPermissionApproved(activity)) {
            permissions.add(perm[3]);
        }

        if (!isWriteExternalStoragePermissionApproved(activity)) {
            permissions.add(perm[4]);
        }
        if (!isReadExternalStoragePermissionApproved(activity)) {
            permissions.add(perm[5]);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), requestCode);
        }

    }

    public static void showSnackBar(
            String mainText, View view, Activity activity) {
        Snackbar.make(view, mainText, Snackbar.LENGTH_INDEFINITE)
                .setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        intent.putExtra("android.provider.extra.APP_PACKAGE", activity.getPackageName());
                        activity.startActivity(intent);
                    }
                })
                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                .setBackgroundTint(activity.getColor(R.color.red))
                .show();
    }
}
