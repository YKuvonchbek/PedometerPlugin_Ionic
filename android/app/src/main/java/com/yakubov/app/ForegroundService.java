package com.yakubov.app;

import android.app.AlarmManager;
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
import android.widget.Toast;

import com.yakubov.app.utils.AlarmReceiver;
import com.yakubov.app.utils.SharedPrefManager;
import com.getcapacitor.JSObject;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {

    private static final String CHANNEL_ID = "com.pedometer.weedoweb";
    private static final int FOREGROUND_ID = 945;

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);

        PedometerPluginImpl plugin = PedometerPluginImpl.getInstance();
        plugin.initialize(this);
        plugin.start();

        plugin.listenerForService = new PedometerPluginListener() {
          @Override
          public void onReceived(JSObject data) {
            try {
              int steps = ((Double) data.get("numberOfSteps")).intValue();
              sharedPrefManager.saveSteps(steps);
              sharedPrefManager.save(String.valueOf(data));
              updateContent(String.valueOf(steps));
            } catch (JSONException e) {
              e.printStackTrace();
            }

          }
        };


        createAlarm();
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

  public void createAlarm() {
    //System request code
    int DATA_FETCHER_RC = 123;
    //Create an alarm manager
    AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

    //Create the time of day you would like it to go off. Use a calendar
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);

    //Create an intent that points to the receiver. The system will notify the app about the current time, and send a broadcast to the app
    Intent intent = new Intent(this, AlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, DATA_FETCHER_RC,intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

    //initialize the alarm by using inexactrepeating. This allows the system to scheduler your alarm at the most efficient time around your
    //set time, it is usually a few seconds off your requested time.
    // you can also use setExact however this is not recommended. Use this only if it must be done then.

    //Also set the interval using the AlarmManager constants
    mAlarmManager.setInexactRepeating(AlarmManager.RTC,calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

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
