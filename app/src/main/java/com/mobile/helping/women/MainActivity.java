package com.mobile.helping.women;

import static com.mobile.helping.women.InitialActivity.REQUEST_CODE_SUCCESS;
import static com.mobile.helping.women.VideoRecorderService.mRecordingStatus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.mobile.helping.women.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    private final Handler mHandler = new Handler();
    private ActivityMainBinding binding;
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (!PermissionManager.isGPSEnabled(MainActivity.this)) {
                PermissionManager.showSnackBarForGPS(getString(R.string.please_turn_on_gps), binding.getRoot(), MainActivity.this);
                binding.textStatus.setText(R.string.inactive);
                binding.textStatus.setTextColor(Color.RED);
            }
            mHandler.postDelayed(this, 5000);
        }
    };
    private AdRequest adRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isReadyToSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mHandler.post(mRunnable);
        adRequest = new AdRequest.Builder().build();
        binding.bannerAd.loadAd(adRequest);

        mSurfaceView = (SurfaceView) findViewById(R.id.prew);
        mSurfaceHolder = mSurfaceView.getHolder();
        binding.btnRecord.setOnClickListener(view -> {
            if (!mRecordingStatus) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        provideLocation();
        binding.btnMessage.setOnClickListener(view -> {
            if (isReadyToSend) {
                sendHelpMessage();
            } else {
                Toast.makeText(this, getString(R.string.wait_to_update_location_and_try_again), Toast.LENGTH_SHORT).show();
                if (!PermissionManager.isAccessFineLocationPermissionApproved(this)) {
                    PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
                }
            }
        });

        binding.btnSos.setOnClickListener(view -> {
            String phone = "tel:" + "123";
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(phone));
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdate();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdate();
        if (mRecordingStatus) {
            binding.btnRecord.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.custom)));
            binding.btnRecord.setText(R.string.stop_recording);
        } else {
            binding.btnRecord.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.custom2)));
            binding.btnRecord.setText(R.string.start_recording);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }


    private void provideLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest =
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 60000).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = location;
                    isReadyToSend = true;
                    binding.textStatus.setText("active");
                    binding.textStatus.setTextColor(Color.GREEN);
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        if (PermissionManager.isAccessFineLocationPermissionApproved(this)
                && PermissionManager.isGPSEnabled(this)) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
        }
    }

    private void stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        binding.textStatus.setText(R.string.inactive);
        binding.textStatus.setTextColor(Color.RED);
    }

    private void sendHelpMessage() {
        String uriBegin = "https://www.google.com/maps/search/?api=1";
        String query = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "&query=" + encodedQuery + "&zoom=16";
        Uri uri = Uri.parse(uriString);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.help_my_last_location) + uri.toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void startRecording() {
        if (PermissionManager.isCameraPermissionApproved(this)
                && PermissionManager.isRecordAudioPermissionApproved(this)
                && PermissionManager.isWriteExternalStoragePermissionApproved(this)
                && PermissionManager.isReadExternalStoragePermissionApproved(this)
                && PermissionManager.isRead13(this)) {
            Intent intent = new Intent(this, VideoRecorderService.class);
            startService(intent);
            binding.btnRecord.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.custom)));
            binding.btnRecord.setText(R.string.stop_recording);
        } else PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
    }

    private void stopRecording() {
        stopService(new Intent(MainActivity.this, VideoRecorderService.class));
        binding.btnRecord.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.custom2)));
        binding.btnRecord.setText(R.string.start_recording);
    }
}