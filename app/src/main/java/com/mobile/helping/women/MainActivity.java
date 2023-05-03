package com.mobile.helping.women;

import static com.mobile.helping.women.InitialActivity.REQUEST_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
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

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private File videoFile;

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

        adRequest = new AdRequest.Builder().build();
        binding.bannerAd.loadAd(adRequest);

        provideLocation();
        mediaRecorder = new MediaRecorder();

        binding.btnRecord.setOnClickListener(view -> {
            if (!isRecording) {
                try {
                    startRecording();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                stopRecording();
            }
        });

        binding.btnMessage.setOnClickListener(view -> {
            if (isReadyToSend) {
                sendHelpMessage();
            } else {
                Toast.makeText(this, "Wait to update location and try again", Toast.LENGTH_SHORT).show();
                if (!PermissionManager.isAccessFineLocationPermissionApproved(this)){
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
    protected void onResume() {
        super.onResume();
        startLocationUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdate();
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
        if (PermissionManager.isAccessFineLocationPermissionApproved(this)) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
        }
    }

    private void stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        binding.textStatus.setText("inactive");
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
        sendIntent.putExtra(Intent.EXTRA_TEXT, " Help!\nMy Last Location!\n" + uri.toString());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void startRecording() throws IOException {
        if (PermissionManager.isCameraPermissionApproved(this)
                && PermissionManager.isRecordAudioPermissionApproved(this)
                && PermissionManager.isWriteExternalStoragePermissionApproved(this)
                && PermissionManager.isReadExternalStoragePermissionApproved(this)
                && PermissionManager.isRead13(this)) {
            videoFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "WomenSafety_" + System.currentTimeMillis() + ".mp4");
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
            mediaRecorder.setOrientationHint(90);
            mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
            mediaRecorder.setPreviewDisplay(binding.prew.getHolder().getSurface());
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            binding.btnRecord.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.custom)));
            binding.btnRecord.setText("stop recording");
        } else PermissionManager.requestPermissions(this, REQUEST_CODE_SUCCESS);
    }

    public void stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            isRecording = false;
            binding.btnRecord.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.custom2)));
            binding.btnRecord.setText("start recording");
        }
    }
}