package com.xhamon.noflashscreenshot;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateWorker extends Worker {

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean available = new AtomicBoolean(false);
        AtomicReference<String> version = new AtomicReference<>("");
        AtomicReference<String> url = new AtomicReference<>("");

        UpdateChecker.check(BuildConfig.VERSION_NAME, new UpdateChecker.Callback() {
            @Override
            public void onResult(boolean updateAvailable, String latestVersion, String releaseUrl) {
                available.set(updateAvailable);
                version.set(latestVersion);
                url.set(releaseUrl);
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            return Result.retry();
        }

        if (available.get()) {
            notifyUser(context, version.get(), url.get());
        }

        return Result.success();
    }

    private void notifyUser(Context context, String version, String releaseUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.update_available_title))
                .setContentText(context.getString(R.string.update_available, version))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(context).notify(1, builder.build());
    }
}
