package com.mobile.helping.women;

import static com.mobile.helping.women.PermissionManager.REQUEST_CODE_SUCCESS;
import static com.mobile.helping.women.PermissionManager.isCameraPermissionApproved;
import static com.mobile.helping.women.PermissionManager.isFreeSpace;
import static com.mobile.helping.women.PermissionManager.isRead13;
import static com.mobile.helping.women.PermissionManager.isRecordAudioPermissionApproved;
import static com.mobile.helping.women.PermissionManager.isWriteExternalStoragePermissionApproved;
import static com.mobile.helping.women.PermissionManager.showSnackBarForRationale;
import static com.mobile.helping.women.VideoRecorderService.mRecordingStatus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
    private static final int MSG_ACTIVE_LOCATION = 1;
    private static final int MSG_ACTIVE_INACTIVE = 0;
    private static final int MSG_RECORDING_TRUE = 3;
    private static final int MSG_RECORDING_FALSE = 2;
    public static ActivityMainBinding binding;
    public static SurfaceHolder mSurfaceHolder;
    public static SurfaceView mSurfaceView;
    private final Handler updateUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            switch (inputMessage.what) {
                case MSG_ACTIVE_LOCATION:
                    updateUI(getString(R.string.active), Color.GREEN);
                    break;

                case MSG_ACTIVE_INACTIVE:
                    updateUI(getString(R.string.inactive), getColor(R.color.red_custom));
                    break;
            }
        }
    };
    private AdRequest adRequest;
    private Handler checkingHandler;
    private Runnable mRunnable;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isReadyToSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        adRequest = new AdRequest.Builder().build();
        binding.bannerAd.loadAd(adRequest);

        mSurfaceView = binding.prew;
        mSurfaceHolder = mSurfaceView.getHolder();

        initChecking();
        provideLocation();
        checkingHandler.post(mRunnable);

        binding.btnRecord.setOnClickListener(view -> {
            if (!mRecordingStatus) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        binding.btnMessage.setOnClickListener(view -> {
            sendHelpMessage();
        });

        binding.btnSos.setOnClickListener(view -> {
            callingAlert();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkingHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            stopLocationUpdate();
        } else {
            startLocationUpdate();
        }
    }

    private void updateUI(String text, int color) {
        binding.textStatus.setTextColor(color);
        binding.textStatus.setText(text);

    }

    private void initChecking() {
        checkingHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Message msg;
                if (mRecordingStatus) {
                    msg = updateUiHandler.obtainMessage(MSG_RECORDING_TRUE);
                } else {
                    msg = updateUiHandler.obtainMessage(MSG_RECORDING_FALSE);
                }
                updateUiHandler.sendMessage(msg);
                if (!PermissionManager.isGPSEnabled(MainActivity.this)) {
                    PermissionManager.showSnackBarForGPS(binding.getRoot(), MainActivity.this);
                    isReadyToSend = false;
                    Message message = updateUiHandler.obtainMessage(MSG_ACTIVE_INACTIVE);
                    updateUiHandler.sendMessage(message);
                }
                checkingHandler.postDelayed(this, 5000);
            }
        };
    }

    private void provideLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                60000).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = location;
                    isReadyToSend = true;
                    Message msg = updateUiHandler.obtainMessage(MSG_ACTIVE_LOCATION, location);
                    updateUiHandler.sendMessage(msg);
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        if (PermissionManager.isAccessFineLocationPermissionApproved(this)
                && PermissionManager.isGPSEnabled(this)) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else if (!PermissionManager.isGPSEnabled(this)) {
            PermissionManager.showSnackBarForGPS(binding.getRoot(), this);
        } else {
            PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
        }
    }

    private void stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Message msg = updateUiHandler.obtainMessage(MSG_ACTIVE_INACTIVE);
        updateUiHandler.sendMessage(msg);
    }

    private void callingAlert() {
        String phone = "tel:" + getString(R.string.sos_102);
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(phone));
        startActivity(intent);
    }

    private void sendHelpMessage() {
        if (isReadyToSend) {
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
        } else if (!PermissionManager.isGPSEnabled(this)) {
            Toast.makeText(this, getString(R.string.gps_is_off_it_must_be_turned_on), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.wait_to_update_location_and_try_again), Toast.LENGTH_SHORT).show();
            if (!PermissionManager.isAccessFineLocationPermissionApproved(this)) {
                showSnackBarForRationale(binding.getRoot(), this);
                PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
            }
        }
    }

    private void startRecording() {
        if (isFreeSpace()) {
            if (PermissionManager.isPostNotificationsPermissionApproved(this)
                    && isWriteExternalStoragePermissionApproved(this)
                    && isCameraPermissionApproved(this)
                    && isRecordAudioPermissionApproved(this)
                    && isRead13(this)) {
                Intent intent = new Intent(this, VideoRecorderService.class);
                startService(intent);
            } else {
                PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
                Toast.makeText(this, getString(R.string.needed_access_for_using_this_feature), Toast.LENGTH_SHORT).show();
            }
        } else {
            PermissionManager.showSnackBarForStorage(binding.getRoot(), this);
        }
    }

    private void stopRecording() {
        stopService(new Intent(MainActivity.this, VideoRecorderService.class));
    }
}