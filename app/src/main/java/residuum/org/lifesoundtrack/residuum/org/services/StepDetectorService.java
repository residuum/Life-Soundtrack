package residuum.org.lifesoundtrack.residuum.org.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by thomas on 25.01.17.
 */

public class StepDetectorService extends Service implements SensorEventListener {

    public static final String BROADCAST_ACTION= "residuum.org.lifesoundtrack.services.StepDetectorService.STEP_DETECTED";
    public static final String DETECTED= "residuum.org.lifesoundtrack.services.StepDetectorService.DETECTED";
    public static final String ACCEL_DIFF = "residuum.org.lifesoundtrack.services.StepDetectorService.ACCELERATION_DIFF";
    public static final String ACCEL = "residuum.org.lifesoundtrack.services.StepDetectorService.ACCELERATION";

    @Override
    public void onCreate(){
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //don't care
    }

    private final IBinder mBinder = new StepBinder();


    public class StepBinder extends Binder {
        public StepDetectorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StepDetectorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private float last_sign;
    private float[] last_extrema = new float[2];
    private float last_acceleration_diff;
    private float[] mLastStepAccelerationDeltas = {-1, -1, -1, -1, -1, -1};
    private int mLastStepAccelerationDeltasIndex = 0;
    private float accelerometerThreshold = 0.75f;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];
        float acceleration = linear_acceleration[0] + linear_acceleration[1] + linear_acceleration[2];
        float current_sign = Math.signum(acceleration);

        if (current_sign == last_sign) {
            // the maximum is not reached yet, keep on waiting
            return;
        }

        if (!isSignificantValue(acceleration)) {
            // not significant (acceleration delta is too small)
            return;
        }

        float acceleration_diff = Math.abs(last_extrema[current_sign < 0 ? 1 : 0] /* the opposite */ - acceleration);
        if (!isAlmostAsLargeAsPreviousOne(acceleration_diff)) {
            return;
        }

        if (!wasPreviousLargeEnough(acceleration_diff)) {
            last_acceleration_diff = acceleration_diff;
            return;
        }

        // check if this occurrence is regular with regard to the acceleration data
        if (!isRegularlyOverAcceleration(acceleration_diff)) {
            last_acceleration_diff = acceleration_diff;
            return;
        }
        last_acceleration_diff = acceleration_diff;

        last_sign = current_sign;
        last_extrema[current_sign < 0 ? 0 : 1] = acceleration;
        onStepDetected(acceleration_diff, acceleration);
    }

    private void onStepDetected(float acceleration_diff, float acceleration) {
        Intent localIntent =
                new Intent(BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(DETECTED, true).putExtra(ACCEL_DIFF, acceleration_diff).putExtra(ACCEL, acceleration);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    private boolean isSignificantValue(float val) {
        return Math.abs(val) > accelerometerThreshold;
    }

    private boolean isAlmostAsLargeAsPreviousOne(float diff) {
        return diff > last_acceleration_diff * 0.5;
    }
    private boolean wasPreviousLargeEnough(float diff) {
        return last_acceleration_diff > diff / 3;
    }

    private boolean isRegularlyOverAcceleration(float diff) {
        mLastStepAccelerationDeltas[mLastStepAccelerationDeltasIndex] = diff;
        mLastStepAccelerationDeltasIndex = (mLastStepAccelerationDeltasIndex + 1) % mLastStepAccelerationDeltas.length;
        int numIrregularAccelerationValues = 0;
        for (float mLastStepAccelerationDelta : mLastStepAccelerationDeltas) {
            if (Math.abs(mLastStepAccelerationDelta - last_acceleration_diff) > 0.5) {
                numIrregularAccelerationValues++;
                break;
            }
        }
        return numIrregularAccelerationValues < mLastStepAccelerationDeltas.length * 0.2;
    }
}
