package com.getcapacitor.plugin.compassaccuracy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CompassAccuracy")
public class CompassAccuracyPlugin extends Plugin implements SensorEventListener {
    public static final String TAG = "CompassAccuracy";

    protected static int NO_ACCURACY_RECEIVED = -1000;
    protected static int RELATIVE_LAYOUT_ID = 1000;
    protected static int DEFAULT_HORIZONTAL_MARGIN = 50;
    protected static int DEFAULT_VERTICAL_MARGIN = 100;
    protected static String CALIBRATION_IMAGE_NAME = "calibration";
    protected static String DIALOG_TITLE = "Compass calibration required";
    protected static String CALIBRATION_HINT = "Tilt and move your phone 3 times in a figure-of-eight motion like this.";

    protected String currentWatchId = null;
    protected int requiredAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
    protected int previousAccuracy = NO_ACCURACY_RECEIVED;

    protected AlertDialog dialog = null;
    protected TextView accuracyTextView = null;
    protected boolean hasShownDialog = false;
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
        hasShownDialog = false;
        hideDialog();
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
        hasShownDialog = false;
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
                    savedCall.resolve(result);

                    if (isInaccurate) {
                        if (!hasShownDialog) {
                            showDialog(currentAccuracy);
                            hasShownDialog = true;
                        }
                        if (dialog != null) {
                            setAccuracyText(currentAccuracy);
                        }
                    } else {
                        hideDialog();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error evaluating accuracy: " + e.getMessage());
        }
    }

    protected void hideDialog() {
        if (dialog != null) {
            Activity activity = getActivity();
            activity.runOnUiThread(() -> {
                dialog.dismiss();
                dialog = null;
                accuracyTextView = null;
            });
        }
    }

    protected void showDialog(int currentAccuracy) {
        if (dialog != null) {
            return;
        }

        Activity activity = getActivity();
        Context context = getContext();
        activity.runOnUiThread(() -> {
            if (!isInaccurate) {
                hideDialog();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            // Create a parent RelativeLayout for the dialog
            RelativeLayout dialogRelativeLayout = new RelativeLayout(context);
            dialogRelativeLayout.setId(RELATIVE_LAYOUT_ID);
            RelativeLayout.LayoutParams dialogRelativeLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            );
            dialogRelativeLayout.setLayoutParams(dialogRelativeLayoutParams);
            dialogRelativeLayout.setBackgroundColor(0xFFFFFFFF);

            // Create title TextView
            TextView titleTextView = new TextView(context);
            titleTextView.setId(RELATIVE_LAYOUT_ID + 1);
            RelativeLayout.LayoutParams titleTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            titleTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            titleTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            titleTextParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, 0);
            titleTextView.setLayoutParams(titleTextParams);
            titleTextView.setText(DIALOG_TITLE);
            titleTextView.setTypeface(titleTextView.getTypeface(), android.graphics.Typeface.BOLD);
            titleTextView.setTextSize(22);
            titleTextView.setTextColor(0xFF000000);
            titleTextView.setGravity(android.view.Gravity.CENTER);
            dialogRelativeLayout.addView(titleTextView);

            // Create hint TextView
            TextView hintTextView = new TextView(context);
            hintTextView.setId(RELATIVE_LAYOUT_ID + 2);
            RelativeLayout.LayoutParams hintTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            hintTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            hintTextParams.addRule(RelativeLayout.BELOW, titleTextView.getId());
            hintTextParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, 0);
            hintTextView.setLayoutParams(hintTextParams);
            hintTextView.setText(CALIBRATION_HINT);
            hintTextView.setTextSize(18);
            hintTextView.setTextColor(0xFF000000);
            hintTextView.setGravity(android.view.Gravity.CENTER);
            dialogRelativeLayout.addView(hintTextView);

            // Create ImageView
            ImageView imageView = new ImageView(context);
            imageView.setId(RELATIVE_LAYOUT_ID + 3);
            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            imageParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN);
            imageView.setLayoutParams(imageParams);

            int defaultSmallIconResID = context.getResources().getIdentifier(CALIBRATION_IMAGE_NAME, "drawable", context.getPackageName());
            imageView.setImageResource(defaultSmallIconResID);
            dialogRelativeLayout.addView(imageView);

            // Create Button
            Button button = new Button(context);
            button.setId(RELATIVE_LAYOUT_ID + 4);
            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            int buttonBottomOffset = 16;
            buttonParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN + buttonBottomOffset, DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN);
            button.setLayoutParams(buttonParams);
            button.setText("DONE");
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(0xFF007AFF);
            button.setTranslationY(-(buttonBottomOffset) * context.getResources().getDisplayMetrics().density);
            dialogRelativeLayout.addView(button);

            // Create accuracy TextView
            accuracyTextView = new TextView(context);
            accuracyTextView.setId(RELATIVE_LAYOUT_ID + 5);
            RelativeLayout.LayoutParams accuracyTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            accuracyTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            accuracyTextParams.addRule(RelativeLayout.ABOVE, button.getId());
            accuracyTextParams.setMargins(DEFAULT_HORIZONTAL_MARGIN, DEFAULT_VERTICAL_MARGIN, DEFAULT_HORIZONTAL_MARGIN, 0);
            accuracyTextView.setLayoutParams(accuracyTextParams);
            accuracyTextView.setTextSize(18);
            accuracyTextView.setTextColor(0xFF000000);
            accuracyTextView.setGravity(android.view.Gravity.CENTER);

            setAccuracyText(currentAccuracy);
            dialogRelativeLayout.addView(accuracyTextView);

            // Set the view
            builder.setView(dialogRelativeLayout);

            // Create and show dialog
            dialog = builder.create();
            dialog.show();

            // Set button click listener
            button.setOnClickListener(v -> {
                dialog.dismiss();
                dialog = null;
                accuracyTextView = null;
            });

            // Make dialog modal
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        });
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

    private int getAccuracyColor(int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                return Color.GREEN;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                return Color.rgb(255, 165, 0);
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                return Color.RED;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                return Color.GRAY;
            default:
                return Color.BLACK;
        }
    }

    private void setAccuracyText(int accuracy) {
        if (accuracyTextView != null) {
            String accuracyName = getAccuracyName(accuracy);
            int accuracyColor = getAccuracyColor(accuracy);

            Activity activity = getActivity();
            activity.runOnUiThread(() -> {
                accuracyTextView.setText("");

                SpannableString compassAccuracyPrefix = new SpannableString("Compass accuracy: ");
                compassAccuracyPrefix.setSpan(new ForegroundColorSpan(Color.BLACK), 0, compassAccuracyPrefix.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                accuracyTextView.setText(compassAccuracyPrefix, TextView.BufferType.SPANNABLE);

                SpannableString accuracyValue = new SpannableString(accuracyName);
                accuracyValue.setSpan(new StyleSpan(Typeface.BOLD), 0, accuracyValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                accuracyValue.setSpan(new ForegroundColorSpan(accuracyColor), 0, accuracyValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                accuracyTextView.append(accuracyValue);
            });
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
