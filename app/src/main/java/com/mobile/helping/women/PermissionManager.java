package com.mobile.helping.women;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;


public class PermissionManager {
    public static final int REQUEST_CODE_SUCCESS = 111;
    static String[] perm = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
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

    public static boolean isPostNotificationsPermissionApproved(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isAllPermissionsApproved(Context context) {
        return isCallPhonePermissionApproved(context)
                && isAccessFineLocationPermissionApproved(context)
                && isCameraPermissionApproved(context)
                && isRecordAudioPermissionApproved(context)
                && isWriteExternalStoragePermissionApproved(context)
                && isReadExternalStoragePermissionApproved(context)
                && isRead13(context)
                && isPostNotificationsPermissionApproved(context);
    }

    public static boolean isFreeSpace() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long availableSpace = statFs.getAvailableBytes();
        long minFreeSpace = 200L * 1000 * 1000;

        Log.d(TAG, "" + availableSpace / 1000 / 1000);
        Log.d(TAG, "" + minFreeSpace / 1000 / 1000);
        return availableSpace > minFreeSpace;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), requestCode);
        }

    }

    public static void showSnackBarForRationale(View view, Activity activity) {
        Snackbar.make(view, R.string.camera_and_location_rationale, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, new View.OnClickListener() {
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
                .setBackgroundTint(Color.WHITE)
                .setTextColor(Color.BLACK)
                .setActionTextColor(Color.BLACK)
                .show();
    }

    public static void showSnackBarForGPS(View view, Activity activity) {
        Snackbar.make(view, R.string.please_turn_on_gps, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivity(intent);
                    }
                })
                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                .setBackgroundTint(Color.WHITE)
                .setTextColor(Color.BLACK)
                .setActionTextColor(Color.BLACK)
                .show();
    }

    public static void showSnackBarForStorage(View view, Activity activity) {
        Snackbar.make(view, R.string.check_free_storage, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                        activity.startActivity(intent);
                    }
                })
                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                .setBackgroundTint(Color.WHITE)
                .setTextColor(Color.BLACK)
                .setActionTextColor(Color.BLACK)
                .show();
    }
}
