package residuum.org.lifesoundtrack.residuum.org.pd.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "PdState.db";
    private static final String SQL_CREATE_PdState = "CREATE TABLE " + Contract.Entry.TABLE_NAME
            + " (" + Contract.Entry._ID + " INTEGER PRIMARY KEY,"
            + Contract.Entry.COLUMN_NAME_KEY + " TEXT UNIQUE,"
            + Contract.Entry.COLUMN_NAME_VALUE + " TEXT)";
    private static final String SQL_CREATE_PdState_INDEX = "CREATE INDEX " + Contract.Entry.COLUMN_NAME_KEY + "_idx ON "
            + Contract.Entry.TABLE_NAME + " (" + Contract.Entry.COLUMN_NAME_KEY + ")";
    private static final String SQL_DELETE_PdState = "DROP TABLE IF EXISTS " + Contract.Entry.TABLE_NAME;

    private static final String SQL_LOAD_PdState = "SELECT * FROM " + Contract.Entry.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PdState);
        db.execSQL(SQL_CREATE_PdState_INDEX);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PdState);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
