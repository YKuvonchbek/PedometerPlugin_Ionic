package com.yakubov.app;

import android.Manifest;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yakubov.app.utils.SharedPrefManager;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;


@CapacitorPlugin(
  name = "PedometerPlugin",
  permissions = { @Permission(alias = "activity", strings = {Manifest.permission.ACTIVITY_RECOGNITION}) }
)
public class PedometerPlugin extends Plugin {

  private PedometerPluginImpl plugin;

  private ActivityResultLauncher<String> requestPermissionLauncher;

  @Override
  public void load() {
    super.load();

    requestPermissionLauncher =
      getActivity().registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
          plugin.start();
        } else {
          // Explain to the user that the feature is unavailable because the
          // features requires a permission that the user has denied. At the
          // same time, respect the user's decision. Don't link to system
          // settings in an effort to convince the user to change their
          // decision.
        }
      });

    plugin = PedometerPluginImpl.getInstance();
    plugin.initialize(getContext());
    plugin.listener = new PedometerPluginListener() {
      @Override
      public void onReceived(JSObject data) {
        bridge.triggerJSEvent("stepEvent", "window", String.valueOf(data));

        try {
          ForegroundService.startService(getContext(), String.valueOf(((Double) data.get("numberOfSteps")).intValue()));
        } catch (JSONException e) {
          e.printStackTrace();
        };
      }
    };
  }

  @Override
  protected void handleOnDestroy() {
    super.handleOnDestroy();
  }

  @PluginMethod
  public void getSavedData(PluginCall call) {
    SharedPrefManager manager = new SharedPrefManager(getContext());
    String savedData = manager.getData();
    if (savedData == null) {
      JSObject data = plugin.getStepsJSON(0);
      call.resolve(data);
      return;
    }
    try {
      call.resolve(new JSObject(savedData));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @PluginMethod
  public void setData(PluginCall call) {
    SharedPrefManager manager = new SharedPrefManager(getContext());
    int stepsFromIonic = call.getInt("numberOfSteps");
    manager.saveSteps(stepsFromIonic);
    plugin.lastNumberOfSteps = stepsFromIonic;
  }

  @PluginMethod
  public void start(PluginCall call) {
    call.resolve();
    SharedPrefManager sharedPrefManager = new SharedPrefManager(getContext());
    String lastSavedSteps = String.valueOf(sharedPrefManager.getLastNumberOfSteps());
    ForegroundService.startService(getContext(), lastSavedSteps);

    if(ContextCompat.checkSelfPermission(getActivity(),
      android.Manifest.permission.ACTIVITY_RECOGNITION) == android.content.pm.PackageManager.PERMISSION_DENIED){
      //ask for permission
      requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION);
    } else {
      plugin.start();
    }
  }

  @PluginMethod
  public void stop() {
    plugin.stop();
    ForegroundService.stopService(getContext());
  }

  @PluginMethod
  public void reset() {
    plugin.reset();
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
        notifyListeners("timerEvent", object);
        bridge.triggerJSEvent("timerEvent", "window", String.valueOf(object));
        Log.e("testtest", counts + "");
      }
    };
    timer.scheduleAtFixedRate(t,1000,1000);
    call.setKeepAlive(true);
  }

}
