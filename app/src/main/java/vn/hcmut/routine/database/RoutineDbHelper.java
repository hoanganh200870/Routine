package vn.hcmut.routine.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RoutineDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "routine.db";
    public static final int DATABASE_VERSION = 3;

    public RoutineDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RoutineContract.TaskEntry.SQL_CREATE);
        db.execSQL(RoutineContract.DailyEntry.SQL_CREATE);
        db.execSQL(RoutineContract.RepeatEntry.SQL_CREATE);
        db.execSQL(RoutineContract.TodoEntry.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RoutineContract.DailyEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RoutineContract.RepeatEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RoutineContract.TodoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RoutineContract.TaskEntry.TABLE_NAME);

        onCreate(db);
    }

}
