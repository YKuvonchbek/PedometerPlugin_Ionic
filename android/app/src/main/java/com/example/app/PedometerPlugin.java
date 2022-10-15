package com.example.app;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import java.util.Timer;
import java.util.TimerTask;


@CapacitorPlugin(
  name = "PedometerPlugin",
  permissions = { @Permission(alias = "activity", strings = {Manifest.permission.ACTIVITY_RECOGNITION}) }
)
public class PedometerPlugin extends Plugin {

  @Override
  public void load() {
    super.load();
  }

  @Override
  protected void handleOnDestroy() {
    super.handleOnDestroy();
  }

  @PluginMethod
  public void start(PluginCall call) {
    call.resolve();
    if(ContextCompat.checkSelfPermission(getActivity(),
      android.Manifest.permission.ACTIVITY_RECOGNITION) == android.content.pm.PackageManager.PERMISSION_DENIED){
      //ask for permission
      ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 1);
    }

    ForegroundService.startService(getContext(), "0");
  }

  @PluginMethod
  public void stop() {
    ForegroundService.stopService(getContext());
  }

  @PluginMethod
  public void testTimer(PluginCall call) {
    Timer timer = new Timer();
    call.setKeepAlive(true);
    TimerTask t = new TimerTask() {
      int counts = 0;
      @Override
      public void run() {

        counts++;
        JSObject object = new JSObject();
        object.put("seconds", counts);
        call.resolve(object);
        System.out.println("1");
      }
    };
    timer.scheduleAtFixedRate(t,1000,1000);
    call.setKeepAlive(true);
  }

}
