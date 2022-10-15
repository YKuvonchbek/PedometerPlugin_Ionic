package com.example.app;

import java.util.List;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import org.json.JSONException;

/**
 * This class listens to the pedometer sensor
 */

@CapacitorPlugin(
  name = "PedometerPlugin",
  permissions = { @Permission(alias = "activity", strings = {Manifest.permission.ACTIVITY_RECOGNITION}) }
)
public class PedometerPluginImpl implements SensorEventListener {

    public static int STOPPED = 0;
    public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    public static int ERROR_NO_SENSOR_FOUND = 4;

    private int status;     // status of listener
    private float startsteps; //first value, to be substracted
    private long starttimestamp; //time stamp of when the measurement starts

    private SensorManager sensorManager; // Sensor manager
    private Sensor mSensor;             // Pedometer sensor returned by sensor manager

    public PedometerPluginListener listener;

    private Context mContext;

    /**
     * Constructor
     */
    public PedometerPluginImpl(Context context) {
      this.mContext = context;

      this.starttimestamp = 0;
      this.startsteps = 0;
      this.setStatus(PedometerPluginImpl.STOPPED);
      this.sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Start listening for pedometers sensor.
     */
    public void start() {
        // If already starting or running, then return
        if ((this.status == PedometerPluginImpl.RUNNING) || (this.status == PedometerPluginImpl.STARTING)) {
            return;
        }

        starttimestamp = System.currentTimeMillis();
        this.startsteps = 0;
        this.setStatus(PedometerPluginImpl.STARTING);

        // Get pedometer from sensor manager
        List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_STEP_COUNTER);

        // If found, then register as listener
        if ((list != null) && (list.size() > 0)) {
            this.mSensor = list.get(0);
            if (this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_UI)) {
                this.setStatus(PedometerPluginImpl.STARTING);
            } else {
                this.setStatus(PedometerPluginImpl.ERROR_FAILED_TO_START);
                this.fail(PedometerPluginImpl.ERROR_FAILED_TO_START, "Device sensor returned an error.");
                return;
            };
        } else {
            this.setStatus(PedometerPluginImpl.ERROR_FAILED_TO_START);
            this.fail(PedometerPluginImpl.ERROR_FAILED_TO_START, "No sensors found to register step counter listening to.");
        }
    }

    /**
     * Stop listening to sensor.
     */
    public void stop() {
        if (this.status != PedometerPluginImpl.STOPPED) {
            this.sensorManager.unregisterListener(this);
        }
        this.setStatus(PedometerPluginImpl.STOPPED);
    }

    /**
     * Called when the accuracy of the sensor has changed.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //nothing to do here
        return;
    }

    /**
     * Sensor listener event.
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Only look at step counter events
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
            return;
        }

        // If not running, then just return
        if (this.status == PedometerPluginImpl.STOPPED) {
            return;
        }
        this.setStatus(PedometerPluginImpl.RUNNING);

        float steps = event.values[0];

        if(this.startsteps == 0)
            this.startsteps = steps;

        steps = steps - this.startsteps;

        this.win(this.getStepsJSON(steps));
    }

    @PluginMethod
    public void onReset() {
        if (this.status == PedometerPluginImpl.RUNNING) {
            this.stop();
        }
    }

    // Sends an error back to JS
    private void fail(int code, String message) {
        // Error object
//        callbackContext.reject(message, "" + code);
    }

    private void win(JSObject message) {
        // Success return object
      try {
//        ForegroundService.getInstance().updateContent();
        this.listener.onReceivedStep(String.valueOf(((Double) message.get("numberOfSteps")).intValue()));
      } catch (JSONException e) {
        e.printStackTrace();
      }


//      callbackContext.resolve(message);
    }

    private void win(boolean success) {
        // Success return object
//        callbackContext.resolve();
    }

    private void setStatus(int status) {
        this.status = status;
    }

    private JSObject getStepsJSON(float steps) {
        JSObject r = new JSObject();
      r.put("startDate", this.starttimestamp);
      r.put("endDate", System.currentTimeMillis());
      r.put("numberOfSteps", steps);
        return r;
    }
}
