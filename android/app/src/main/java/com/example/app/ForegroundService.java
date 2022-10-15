package com.example.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.getcapacitor.PluginCall;

import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {

    private static final String CHANNEL_ID = "com.pedometer.weedoweb";
    private static final int FOREGROUND_ID = 945;

    private NotificationManager notificationManager;
    private PedometerPluginImpl plugin;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


//      Timer timer = new Timer();
//
//      TimerTask t = new TimerTask() {
//        int counts = 0;
//        @Override
//        public void run() {
//
//          counts++;
//          updateContent(String.valueOf(counts));
//          System.out.println("1");
//        }
//      };
//      timer.scheduleAtFixedRate(t,1000,1000);

      plugin = new PedometerPluginImpl(this);
      plugin.start();
      plugin.listener = new PedometerPluginListener() {
        @Override
        public void onReceivedStep(String count) {
          updateContent(count);
        }
      };
    }

    public static void startService(Context context, String message) {
      Intent intent = new Intent(context, ForegroundService.class);
      intent.putExtra("numberOfSteps", message);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent);
      } else {
        context.startService(intent);
      }
    }

  public static void stopService(Context context) {
    Intent intent = new Intent(context, ForegroundService.class);
    context.stopService(intent);
  }

  @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      String input = intent.getStringExtra("numberOfSteps");
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
          0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

    Notification notification = null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      notification = new Notification.Builder(this, CHANNEL_ID)
        .setContentTitle("Counting steps")
        .setContentText(input)
        .setSmallIcon(R.drawable.ic_baseline_directions_walk_24)
        .setContentIntent(pendingIntent)
        .build();
    }

    startForeground(FOREGROUND_ID, notification);

        //do heavy work on a background thread
        //stopSelf();
        return START_STICKY;
    }


    private void createNotificationChannel() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Counting steps", NotificationManager.IMPORTANCE_DEFAULT);
          channel.enableVibration(false);
          channel.setSound(null, null);
          channel.setShowBadge(false);
          notificationManager.createNotificationChannel(channel);
      }
    }


    public void updateContent(String message) {
      PendingIntent pendingIntent = PendingIntent.getActivity(this,
        0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
          .setContentTitle("Counting steps")
          .setContentText(message)
          .setSmallIcon(R.drawable.ic_baseline_directions_walk_24)
          .setContentIntent(pendingIntent)
          .build();

        notificationManager.notify(FOREGROUND_ID, notification);
      }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}