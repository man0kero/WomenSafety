package com.mobile.helping.women;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;

public class VideoRecorderService extends Service {
    public static boolean mRecordingStatus;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;

    @Override
    public void onCreate() {
        super.onCreate();
        mRecordingStatus = false;
        mSurfaceView = MainActivity.mSurfaceView;
        mSurfaceHolder = MainActivity.mSurfaceHolder;
        if (!mRecordingStatus) {
            startRecording();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("stop_recording")) {
                stopForeground(true);
                stopRecording();
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        mRecordingStatus = false;
        super.onDestroy();
    }

    public void startRecording() {
        Toast.makeText(getBaseContext(), getString(R.string.recording_started), Toast.LENGTH_SHORT).show();

        File videoFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".mp4");
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
            mMediaRecorder.setOrientationHint(90);
            mMediaRecorder.setOutputFile(videoFile.getAbsolutePath());
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mMediaRecorder.start();
            mRecordingStatus = true;
            notificationForUser();
        }
    }

    private void notificationForUser() {
        String channelId = "NOTIFICATION_CHANNEL_ID";
        NotificationCompat.Builder notificationBuilder = new
                NotificationCompat.Builder(this, channelId);

        Intent stopIntent = new Intent(this, VideoRecorderService.class);
        stopIntent.setAction("stop_recording");
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.recording_in_progress))
                .setContentText(getString(R.string.tap_to_stop_recording))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setContentIntent(stopPendingIntent)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Check the Weather",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, notification);
    }

    public void stopRecording() {
        Toast.makeText(getBaseContext(), getString(R.string.recording_stopped), Toast.LENGTH_SHORT).show();
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (Exception e) {
                Log.d(TAG, "stopRecording: " + e.getMessage());
            }
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}