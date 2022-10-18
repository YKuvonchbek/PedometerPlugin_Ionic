package com.yakubov.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yakubov.app.PedometerPluginImpl;

public class AlarmReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    //Your code once the alarm is set off goes here
    //You can use an intent filter to filter the specified intent
    PedometerPluginImpl pedometerPlugin = PedometerPluginImpl.getInstance();
    pedometerPlugin.reset();
    pedometerPlugin.start();
  }
}
