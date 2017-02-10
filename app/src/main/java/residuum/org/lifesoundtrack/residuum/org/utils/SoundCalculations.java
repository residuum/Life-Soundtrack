package residuum.org.lifesoundtrack.residuum.org.utils;

/**
 * Created by thomas on 24.01.17.
 */

public class SoundCalculations {

    private SoundCalculations(){

    }

    public static float BpmFromMs(double ms){
        float value =(float)(60000 / ms);
        if (value < 60){
            return 60;
        }
        if (value > 200){
            return 200;
        }
        return value;
    }

    public static long MsFromBpm(float bpm){
        return (long)(60000 / bpm);
    }
}
