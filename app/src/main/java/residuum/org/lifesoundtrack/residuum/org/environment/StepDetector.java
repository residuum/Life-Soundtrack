package residuum.org.lifesoundtrack.residuum.org.environment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;

import residuum.org.lifesoundtrack.MainActivity;
import residuum.org.lifesoundtrack.residuum.org.services.StepDetectorService;
import residuum.org.lifesoundtrack.residuum.org.utils.SoundCalculations;

/**
 * Created by thomas on 31.01.17.
 */

public class StepDetector {
    private final MainActivity activity;
    private final Object lock = new Object();
    private ArrayList<Long> beatLengthInMs = new ArrayList<>(10);
    private Date latestStep;

    public StepDetector(MainActivity activity) {
        this.activity = activity;

        loadBpm();
    }

    public void startStepDetection() {

        Intent intent = new Intent(this.activity, StepDetectorService.class);
        this.activity.bindService(intent, stepDetectorConnection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(StepDetectorService.BROADCAST_ACTION);

        StepDetectorReceiver detectorReceiver = new StepDetectorReceiver();
        LocalBroadcastManager.getInstance(this.activity).registerReceiver(
                detectorReceiver,
                intentFilter);
    }


    private StepDetectorService stepDetectorService;

    @NonNull
    private ServiceConnection stepDetectorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (lock) {
                stepDetectorService = ((StepDetectorService.StepBinder) service).getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // this method will never be called?!
        }
    };

    private void loadBpm() {
        float bpm = 120;
        initBpm(bpm);
    }

    public void initBpm(float bpm) {
        latestStep = new Date();
        long meanDifference = SoundCalculations.MsFromBpm(bpm);
        beatLengthInMs.clear();
        beatLengthInMs.add(0, meanDifference);
        beatLengthInMs.add(1, meanDifference);
        beatLengthInMs.add(2, meanDifference);
        beatLengthInMs.add(3, meanDifference);
        beatLengthInMs.add(4, meanDifference);
        beatLengthInMs.add(5, meanDifference);
        beatLengthInMs.add(6, meanDifference);
        beatLengthInMs.add(7, meanDifference);
        beatLengthInMs.add(8, meanDifference);
        beatLengthInMs.add(9, meanDifference);
        activity.setDebugAcceleration(bpm, beatLengthInMs, 0, 0);
    }

    private void addNewStep(Bundle extras) {
        Date now = new Date();
        long elapsed = now.getTime() - latestStep.getTime();
        latestStep = now;
        if (elapsed < 200 || elapsed > 4000){
            //too short or too long
            return;
        }
        double weightedLength = (elapsed + (beatLengthInMs.get(0) * .91) + (beatLengthInMs.get(1) * .82) + (beatLengthInMs.get(2) * .73) + (beatLengthInMs.get(3) * .64)
                + (beatLengthInMs.get(4) * .55) + (beatLengthInMs.get(5) * .46) + (beatLengthInMs.get(6) * .37) + (beatLengthInMs.get(7) * .28)
                + (beatLengthInMs.get(8) * .19) + (beatLengthInMs.get(9) * .1))
                / (1 + .91 + .82 + .73 + .64 + .55 + .46 + .37 + .28 + .19 + 1);
        float bpm = SoundCalculations.BpmFromMs(weightedLength);
        beatLengthInMs.remove(9);
        beatLengthInMs.add(0, elapsed);
        activity.updateBpm(bpm);
        float acceleration = extras.getFloat(StepDetectorService.ACCEL);
        float accelerationDiff = extras.getFloat(StepDetectorService.ACCEL_DIFF);
        activity.setDebugAcceleration(bpm, beatLengthInMs, acceleration, accelerationDiff);
    }

    private class StepDetectorReceiver extends BroadcastReceiver
    {
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            addNewStep(intent.getExtras());
        }
    }

    public void stopDetection() {
        activity.unbindService(stepDetectorConnection);
        stepDetectorService.stopSelf();
    }
}
