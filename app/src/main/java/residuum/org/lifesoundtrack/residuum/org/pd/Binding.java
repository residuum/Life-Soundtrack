package residuum.org.lifesoundtrack.residuum.org.pd;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import residuum.org.lifesoundtrack.MainActivity;
import residuum.org.lifesoundtrack.R;
import residuum.org.lifesoundtrack.residuum.org.pd.database.Contract;
import residuum.org.lifesoundtrack.residuum.org.utils.FileUtils;

/**
 * Created by thomas on 21.01.17.
 */

public final class Binding {
    private MainActivity activity;
    private final SQLiteDatabase database;
    private final Object lock = new Object();
    private int patchHandle;
    private boolean isExternallyPaused;
    private float bpm;

    public Binding(MainActivity activity, SQLiteDatabase database) {
        this.activity = activity;
        this.database = database;
        Intent intent = new Intent(activity, PdService.class);
        activity.bindService(intent, pdConnection, Context.BIND_AUTO_CREATE);
        initSystemServices();
    }

    public void play() {
        activity.startOtherServices();
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pdService.startAudio(intent, R.drawable.ls, "Life Soundtrack", "Return to Life Soundtrack");
    }




    private PdService pdService;

    @NonNull
    private ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (lock) {
                pdService = ((PdService.PdBinder) service).getService();
                if (!pdService.isRunning()) {
                    try {
                        initPd();
                        int sampleRate = AudioParameters.suggestSampleRate();
                        pdService.initAudio(sampleRate, 0, 2, 10.0f);
                        HashMap<String, Object> state = loadState();
                        for (Map.Entry<String, Object> entry : state.entrySet()) {
                            String key = entry.getKey();
                            activity.addDebugState(new Object[]{key, entry.getValue()});
                            sendToPd(key, entry.getValue());
                            if (key.equals("bpm")) {
                                activity.initBpm((float) entry.getValue());
                                bpm = (float)entry.getValue();
                            }
                        }
                        sendBangToPd("load-samples");
                        if (bpm == 0) {
                            setBpm(120);
                        }
                    } catch (IOException e) {
                        Log.e("Pd", e.toString());
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // this method will never be called?!
        }
    };
    private PdReceiver receiver = new PdReceiver() {

        @Override
        public void print(String s) {
            activity.addDebugPrint(s);
        }

        @Override
        public void receiveBang(String source) {

        }

        @Override
        public void receiveFloat(String source, float x) {

        }

        @Override
        public void receiveList(String source, Object... args) {
            if (source.equals("state")) {
                activity.addDebugState(args);
                writeState(args);
            }
            if (source.equals("analyzed-r") || source.equals("analyzed-l")){
                activity.addSonification(source, args);
            }
        }

        @Override
        public void receiveMessage(String source, String symbol, Object... args) {

        }

        @Override
        public void receiveSymbol(String source, String symbol) {

        }
    };


    private void writeState(Object[] args) {
        String key = (String) args[0];
        String value = "";
        if (args[1] instanceof Float) {
            value = args[1].toString();
        } else if (args[1] instanceof String) {
            value = (String) args[1];
        }
        ContentValues row = new ContentValues();
        row.put(Contract.Entry.COLUMN_NAME_KEY, key);
        row.put(Contract.Entry.COLUMN_NAME_VALUE, value);
        database.replace(Contract.Entry.TABLE_NAME, null, row);
    }

    private HashMap<String, Object> loadState() {
        HashMap<String, Object> state = new HashMap<>();
        Cursor cursor = database.query(Contract.Entry.TABLE_NAME, Contract.Entry.getColumns(), null, null, null, null, null);
        while (cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex(Contract.Entry.COLUMN_NAME_KEY));
            String value = cursor.getString(cursor.getColumnIndex(Contract.Entry.COLUMN_NAME_VALUE));
            if (tryParseFloat(value)) {
                state.put(key, Float.parseFloat(value));
            } else {
                state.put(key, value);
            }
        }
        return state;
    }

    private boolean tryParseFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void loadPatch() throws IOException {
        File patch = FileUtils.getFileFromZip(activity, R.raw.pdlogic, "patch.pd");
        patchHandle = PdBase.openPatch(patch.getAbsolutePath());
    }

    private void initPd() throws IOException {
        // Configure the audio glue
        PdBase.setReceiver(receiver);
        PdBase.subscribe("state");
        PdBase.subscribe("analyzed-r");
        PdBase.subscribe("analyzed-l");
        loadPatch();
        PdBase.sendFloat("rand-seed", new Date().getTime());
        AudioParameters.init(this.activity);
    }

    public void clearPd() {
        isExternallyPaused = false;
        writeState(new Object[]{"bpm", bpm});
        PdBase.closeAudio();
        PdBase.closePatch(patchHandle);
        activity.unbindService(pdConnection);
        activity.stopOtherListeners();
    }

    public void pause() {
        isExternallyPaused = false;
        writeState(new Object[]{"bpm", bpm});
        pdService.stopAudio();
        activity.stopOtherListeners();
    }

    private void initSystemServices() {
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (pdService == null) {
                    return;
                }
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    if (isExternallyPaused) {
                        play();
                    }
                    isExternallyPaused = false;
                } else {
                    pause();
                    isExternallyPaused = true;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void setBpm(float bpm) {
        sendToPd("bpm", bpm);
        activity.addDebugPrint("bpm " + bpm);
        this.bpm = bpm;
    }

    private void sendToPd(String receiver, Object value) {
        if (value instanceof Float) {
            PdBase.sendFloat(receiver, (float) value);
        } else if (value instanceof String) {
            PdBase.sendSymbol(receiver, (String) value);
        }
    }

    private void sendBangToPd(String receiver){
        PdBase.sendBang(receiver);
    }
}
