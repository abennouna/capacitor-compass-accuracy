package com.tellibus.capacitor.compass.accuracy;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CompassAccuracy")
public class CompassAccuracyPlugin extends Plugin implements SensorEventListener {
    public static final String TAG = "CompassAccuracy";

    protected static int NO_ACCURACY_RECEIVED = -1000;

    protected String currentWatchId = null;
    protected int requiredAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
    protected int previousAccuracy = NO_ACCURACY_RECEIVED;

    protected boolean isInaccurate = false;

    private SensorManager mSensorManager;
    private Sensor mSensorMagneticField;

    @Override
    public void load() {
        super.load();
        Log.d(TAG, "Initializing CompassAccuracy plugin");

        Activity activity = getActivity();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mSensorManager = activity.getSystemService(SensorManager.class);
        } else {
            mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        }
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mSensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void startMonitoring(PluginCall call) {
        // Store the call as a watch callback
        call.setKeepAlive(true);
        currentWatchId = call.getCallbackId();

        // Get required accuracy from options
        String requiredAccuracyStr = call.getString("requiredAccuracy", "high");
        requiredAccuracy = jsAccuracyToNativeAccuracy(requiredAccuracyStr);

        // Send initial result
        JSObject result = new JSObject();
        result.put("type", "started");
        result.put("requiredAccuracy", nativeAccuracyToJsAccuracy(requiredAccuracy));
        result.put("currentAccuracy", nativeAccuracyToJsAccuracy(previousAccuracy));
        call.resolve(result);

        // If current accuracy has been received, evaluate it
        if (previousAccuracy != NO_ACCURACY_RECEIVED) {
            Log.d(TAG, "Current accuracy has been received prior to starting monitoring: evaluating it");
            evaluateChangedAccuracy(previousAccuracy);
        }
    }

    @PluginMethod
    public void stopMonitoring(PluginCall call) {
        if (currentWatchId != null) {
            PluginCall savedCall = getBridge().getSavedCall(currentWatchId);
            if (savedCall != null) {
                getBridge().releaseCall(savedCall);
            }
        }
        currentWatchId = null;
        call.resolve();
    }

    @PluginMethod
    public void getCurrentAccuracy(PluginCall call) {
        JSObject result = new JSObject();
        result.put("currentAccuracy", nativeAccuracyToJsAccuracy(previousAccuracy));
        call.resolve(result);
    }

    @PluginMethod
    public void simulateAccuracyChange(PluginCall call) {
        String accuracyStr = call.getString("accuracy");
        if (accuracyStr == null) {
            call.reject("accuracy parameter is required");
            return;
        }
        int accuracy = jsAccuracyToNativeAccuracy(accuracyStr);
        evaluateChangedAccuracy(accuracy);
        call.resolve();
    }

    protected int jsAccuracyToNativeAccuracy(String jsAccuracy) {
        if (jsAccuracy == null) {
            return SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
        }
        switch (jsAccuracy.toLowerCase()) {
            case "unreliable":
                return SensorManager.SENSOR_STATUS_UNRELIABLE;
            case "low":
                return SensorManager.SENSOR_STATUS_ACCURACY_LOW;
            case "medium":
                return SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
            case "high":
                return SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
            default:
                return SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
        }
    }

    protected String nativeAccuracyToJsAccuracy(int nativeAccuracy) {
        switch (nativeAccuracy) {
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                return "unreliable";
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                return "low";
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                return "medium";
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                return "high";
            default:
                return "unknown";
        }
    }

    protected void evaluateChangedAccuracy(int currentAccuracy) {
        isInaccurate = false;
        try {
            switch (requiredAccuracy) {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    if (currentAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
                        isInaccurate = true;
                    }
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    if (currentAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                        isInaccurate = true;
                    }
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    if (currentAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                        isInaccurate = true;
                    }
                    break;
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                    isInaccurate = true;
                    break;
            }

            if (currentWatchId != null) {
                PluginCall savedCall = getBridge().getSavedCall(currentWatchId);
                if (savedCall != null) {
                    JSObject result = new JSObject();
                    result.put("type", "accuracy_changed");
                    result.put("requiredAccuracy", nativeAccuracyToJsAccuracy(requiredAccuracy));
                    result.put("currentAccuracy", nativeAccuracyToJsAccuracy(currentAccuracy));
                    result.put("previousAccuracy", nativeAccuracyToJsAccuracy(previousAccuracy));
                    result.put("isInaccurate", isInaccurate);
                    savedCall.resolve(result);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error evaluating accuracy: " + e.getMessage());
        }
    }

    private String getAccuracyName(int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                return "HIGH";
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                return "MEDIUM";
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                return "LOW";
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                return "UNRELIABLE";
            default:
                return "UNKNOWN";
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (previousAccuracy == NO_ACCURACY_RECEIVED || previousAccuracy != sensorEvent.accuracy) {
            onAccuracyChanged(sensorEvent.sensor, sensorEvent.accuracy);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int currentAccuracy) {
        if (previousAccuracy == currentAccuracy) return;

        Log.i(TAG, "Magnetometer accuracy changed from " + getAccuracyName(previousAccuracy) + " to " + getAccuracyName(currentAccuracy));
        previousAccuracy = currentAccuracy;

        evaluateChangedAccuracy(currentAccuracy);
    }
}
