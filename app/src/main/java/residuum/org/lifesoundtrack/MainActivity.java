package residuum.org.lifesoundtrack;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Date;
import java.util.ArrayList;

import residuum.org.lifesoundtrack.residuum.org.pd.Binding;
import residuum.org.lifesoundtrack.residuum.org.pd.database.DbHelper;
import residuum.org.lifesoundtrack.residuum.org.environment.StepDetector;

public class MainActivity extends AppCompatActivity {

    private Binding pdBinding;
    private StepDetector stepDetector;
    private ToggleButton startStopButton;
    private DbHelper dbHelper;
    //private TextView debugAcceleration;
    private TextView debugState;
    private TextView debugPrint;
    private ArrayList<String> stateList = new ArrayList<>();
    private ArrayList<String> printList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        setContentView(R.layout.activity_main);
        //debugAcceleration = (TextView)findViewById(R.id.debugAcceleration);
        debugState = (TextView)findViewById(R.id.debugState);
        debugPrint = (TextView)findViewById(R.id.debugPrint);
        startStopButton = (ToggleButton)findViewById(R.id.startStopButton);
        startStopButton.setOnCheckedChangeListener(startStopClicked);

        pdBinding = new Binding(this, db);
        stepDetector = new StepDetector(this);

    }

    public void setDebugAcceleration(float bpm, ArrayList<Long> beatLength, float acceleration, float accelerationDiff){
//        StringBuilder text = new StringBuilder("BPM: " + bpm
//                + "\nacceleration: " + acceleration
//                + "\naccel diff: " + accelerationDiff);
//        for (Long beat : beatLength) {
//            text.append("\nlength: " + beat);
//        }
//        if (debugAcceleration != null) {
//            debugAcceleration.setText(text);
//        }
    }

    public void addDebugState(Object[] pdList) {
        StringBuilder newState = new StringBuilder(new Date().getTime() + ": ");
        for (Object atom : pdList) {
            newState.append(atom + " ");
        }
        stateList.add(0, newState.toString());
        while (stateList.size() > 10){
            stateList.remove(10);
        }
        StringBuilder text = new StringBuilder();
        for(String s : stateList){
            text.append("\n" + s);
        }
        if (debugState != null){
            debugState.setText(text);
        }
    }

    public void addDebugPrint(String print){
        printList.add(0, new Date().getTime() + ": " + print);
        while (printList.size() > 5){
            printList.remove(5);
        }
        StringBuilder text = new StringBuilder();
        for(String s : printList){
            text.append("\n" + s);
        }
        if (debugPrint != null){
            debugPrint.setText(text);
        }
    }

    CompoundButton.OnCheckedChangeListener startStopClicked = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked){
                pdBinding.play();
            } else {
                pdBinding.pause();
            }
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        pdBinding.clearPd();
        dbHelper.close();
    }

    public void initBpm(float bpm) {
        stepDetector.initBpm(bpm);
    }

    public void updateBpm(float bpm) {
        pdBinding.setBpm(bpm);
    }

    public void stopOtherListeners() {
        stepDetector.stopDetection();
    }

    public void startOtherServices() {
        stepDetector.startStepDetection();
    }

    public void addSonification(String source, Object[] args) {
    }
}
