package com.xhamon.noflashscreenshot;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.xhamon.noflashscreenshot.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "update_channel";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        createNotificationChannel();
        requestNotificationPermission();
        schedulePeriodicCheck();

        binding.checkButton.setOnClickListener(v -> checkNow());
        checkNow();
    }

    private void checkNow() {
        binding.statusText.setText(R.string.checking);
        UpdateChecker.check(BuildConfig.VERSION_NAME, new UpdateChecker.Callback() {
            @Override
            public void onResult(boolean updateAvailable, String latestVersion, String releaseUrl) {
                runOnUiThread(() -> {
                    if (updateAvailable) {
                        binding.statusText.setText(getString(R.string.update_available, latestVersion));
                        binding.checkButton.setOnClickListener(v -> openUrl(releaseUrl));
                    } else {
                        binding.statusText.setText(R.string.up_to_date);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> binding.statusText.setText(R.string.check_failed));
            }
        });
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void schedulePeriodicCheck() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                UpdateWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "update_check", ExistingPeriodicWorkPolicy.KEEP, request);
    }
}
