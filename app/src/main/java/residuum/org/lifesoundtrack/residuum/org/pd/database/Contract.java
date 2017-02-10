package residuum.org.lifesoundtrack.residuum.org.pd.database;

import android.provider.BaseColumns;

/**
 * Created by thomas on 30.01.17.
 */

public final class Contract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private Contract() {
    }

    /* Inner class that defines the table contents */
    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "audio_state";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VALUE = "value";

        public static String[] getColumns() {
            String[] projection = {
                    COLUMN_NAME_KEY,
                    COLUMN_NAME_VALUE
            };
            return projection;
        }
    }
}


