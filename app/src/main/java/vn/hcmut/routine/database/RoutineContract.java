package vn.hcmut.routine.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

public class RoutineContract {

    public static final String CONTENT_AUTHORITY = "vn.hcmut.routine";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TASK = "task";
    public static final String PATH_DAILY = "daily";
    public static final String PATH_REPEAT = "repeat";
    public static final String PATH_TODO = "todo";

    public static final class TaskEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();

        public static final String TABLE_NAME = "task";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_NOTIFY = "notify";
        public static final String COLUMN_ENABLE = "enable";

        public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_NOTE + " TEXT, " +
                COLUMN_NOTIFY + " NUMBER NOT NULL, " +
                COLUMN_ENABLE + " NUMBER NOT NULL);";

        public static Uri getTaskUri(long taskId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(taskId)).build();
        }

        public static Uri getDailyUri(long taskId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(taskId)).appendPath(RoutineContract.PATH_DAILY).build();
        }

        public static Uri getRepeatUri(long taskId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(taskId)).appendPath(RoutineContract.PATH_REPEAT).build();
        }

        public static Uri getTodoUri(long taskId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(taskId)).appendPath(RoutineContract.PATH_TODO).build();
        }

        public static long parseTaskId(Uri uri) {
            List<String> pathSegments = uri.getPathSegments();
            return Long.parseLong(pathSegments.get(1));
        }
    }

    public static final class DailyEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DAILY).build();

        public static final String TABLE_NAME = "daily";

        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_MON = "mo";
        public static final String COLUMN_TUE = "tu";
        public static final String COLUMN_WED = "we";
        public static final String COLUMN_THU = "th";
        public static final String COLUMN_FRI = "fr";
        public static final String COLUMN_SAT = "sa";
        public static final String COLUMN_SUN = "su";

        public static final String[] DAYS_OF_WEEK = {
                COLUMN_MON, COLUMN_TUE, COLUMN_WED, COLUMN_THU, COLUMN_FRI, COLUMN_SAT, COLUMN_SUN
        };

        public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_ID + " INTEGER, " +
                COLUMN_MON + " INTEGER, " +
                COLUMN_TUE + " INTEGER, " +
                COLUMN_WED + " INTEGER, " +
                COLUMN_THU + " INTEGER, " +
                COLUMN_FRI + " INTEGER, " +
                COLUMN_SAT + " INTEGER, " +
                COLUMN_SUN + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_TASK_ID + ") REFERENCES " + TaskEntry.TABLE_NAME + "(" + TaskEntry._ID + ") ON DELETE CASCADE);";

        public static Uri getDailyUri(long dailyId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(dailyId)).build();
        }
    }

    public static final class RepeatEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPEAT).build();

        public static final String TABLE_NAME = "repeat";

        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_START = "start";
        public static final String COLUMN_FINISH = "finish";
        public static final String COLUMN_INTERVAL = "interval";
        public static final String COLUMN_ALARM_ID = "alarm_id";

        public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_ID + " INTEGER NOT NULL, " +
                COLUMN_START + " INTEGER, " +
                COLUMN_FINISH + " INTEGER, " +
                COLUMN_INTERVAL + " INTEGER, " +
                COLUMN_ALARM_ID + " NUMBER, " +
                "FOREIGN KEY(" + COLUMN_TASK_ID + ") REFERENCES " + TaskEntry.TABLE_NAME + "(" + TaskEntry._ID + ") ON DELETE CASCADE);";
        public static Uri getRepeatUri(long repeatId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(repeatId)).build();
        }
    }

    public static final class TodoEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TODO).build();

        public static final String TABLE_NAME = "todo";

        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_TODO = "content";

        public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_ID + " INTEGER NOT NULL, " +
                COLUMN_TODO + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_TASK_ID + ") REFERENCES " + TaskEntry.TABLE_NAME + "(" + TaskEntry._ID + ") ON DELETE CASCADE);";

        public static Uri getTodoId(long todoId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(todoId)).build();
        }
    }

}
