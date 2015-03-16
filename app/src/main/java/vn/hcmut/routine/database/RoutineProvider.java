package vn.hcmut.routine.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.List;

public class RoutineProvider extends ContentProvider {

    private static final int TASK = 100;
    private static final int TASK_ID = 101;
    private static final int TASK_REPEAT = 102;
    private static final int TASK_TODO = 103;
    private static final int TASK_DAILY = 104;

    private static final int TODO = 200;
    private static final int TODO_ID = 201;

    private static final int DAILY = 300;
    private static final int DAILY_ID = 301;

    private static final int REPEAT = 400;
    private static final int REPEAT_ID = 401;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = RoutineContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, RoutineContract.PATH_TASK, TASK);
        uriMatcher.addURI(authority, RoutineContract.PATH_TASK + "/#", TASK_ID);
        uriMatcher.addURI(authority, RoutineContract.PATH_TASK + "/#/" + RoutineContract.PATH_REPEAT, TASK_REPEAT);
        uriMatcher.addURI(authority, RoutineContract.PATH_TASK + "/#/" + RoutineContract.PATH_TODO, TASK_TODO);
        uriMatcher.addURI(authority, RoutineContract.PATH_TASK + "/#/" + RoutineContract.PATH_DAILY, TASK_DAILY);

        uriMatcher.addURI(authority, RoutineContract.PATH_TODO, TODO);
        uriMatcher.addURI(authority, RoutineContract.PATH_TODO + "/#", TODO_ID);

        uriMatcher.addURI(authority, RoutineContract.PATH_DAILY, DAILY);
        uriMatcher.addURI(authority, RoutineContract.PATH_DAILY + "/#", DAILY_ID);

        uriMatcher.addURI(authority, RoutineContract.PATH_REPEAT, REPEAT);
        uriMatcher.addURI(authority, RoutineContract.PATH_REPEAT + "/#", REPEAT_ID);

        return uriMatcher;
    }

    private SQLiteOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = new RoutineDbHelper(context);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        Cursor cursor;
        String tableName;

        int type = sUriMatcher.match(uri);
        switch (type) {
            case TASK:
                tableName = RoutineContract.TaskEntry.TABLE_NAME;
                break;
            case TASK_ID:
                long taskId = ContentUris.parseId(uri);
                selection = RoutineContract.TaskEntry._ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(taskId)};
                tableName = RoutineContract.TaskEntry.TABLE_NAME;
                break;
            case TASK_REPEAT:
                long rTaskId = RoutineContract.TaskEntry.parseTaskId(uri);
                selectionArgs = new String[]{String.valueOf(rTaskId)};
                selection = RoutineContract.RepeatEntry.COLUMN_TASK_ID + " = ? ";
                tableName = RoutineContract.RepeatEntry.TABLE_NAME;
                break;
            case TASK_TODO:
                long tTaskId = RoutineContract.TaskEntry.parseTaskId(uri);
                selectionArgs = new String[]{String.valueOf(tTaskId)};
                selection = RoutineContract.TodoEntry.COLUMN_TASK_ID + " = ? ";
                tableName = RoutineContract.TodoEntry.TABLE_NAME;
                break;
            case TASK_DAILY:
                long dTaskId = RoutineContract.TaskEntry.parseTaskId(uri);
                selectionArgs = new String[]{String.valueOf(dTaskId)};
                selection = RoutineContract.DailyEntry.COLUMN_TASK_ID + " = ? ";
                tableName = RoutineContract.DailyEntry.TABLE_NAME;
                break;
            case DAILY_ID:
                long dailyId = ContentUris.parseId(uri);
                selection = RoutineContract.DailyEntry._ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(dailyId)};
                tableName = RoutineContract.DailyEntry.TABLE_NAME;
                break;
            case REPEAT_ID:
                long repeatId = ContentUris.parseId(uri);
                selection = RoutineContract.RepeatEntry._ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(repeatId)};
                tableName = RoutineContract.RepeatEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context context = getContext();
        ContentResolver contentResolver = context.getContentResolver();

        cursor = database.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(contentResolver, uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase writer = mOpenHelper.getWritableDatabase();
        String tableName = null;
        Uri contentUri = null;

        int type = sUriMatcher.match(uri);
        switch (type) {
            case TASK:
                tableName = RoutineContract.TaskEntry.TABLE_NAME;
                contentUri = RoutineContract.TaskEntry.CONTENT_URI;
                break;
            case TODO:
                tableName = RoutineContract.TodoEntry.TABLE_NAME;
                contentUri = RoutineContract.TodoEntry.CONTENT_URI;
                break;
            case DAILY:
                tableName = RoutineContract.DailyEntry.TABLE_NAME;
                contentUri = RoutineContract.DailyEntry.CONTENT_URI;
                break;
            case REPEAT:
                tableName = RoutineContract.RepeatEntry.TABLE_NAME;
                contentUri = RoutineContract.RepeatEntry.CONTENT_URI;
                break;
        }

        long id = writer.insert(tableName, null, values);
        if (id == -1) {
            throw new UnsupportedOperationException("Unknow uri: " + uri);
        }

        Context context = getContext();
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.notifyChange(uri, null);

        Uri rUri = ContentUris.withAppendedId(contentUri, id);
        return rUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase writer = mOpenHelper.getWritableDatabase();
        String tableName;

        int type = sUriMatcher.match(uri);
        switch (type) {
            case TASK:
                tableName = RoutineContract.TaskEntry.TABLE_NAME;
                break;
            case TASK_ID:
                tableName = RoutineContract.TaskEntry.TABLE_NAME;
                long taskId = RoutineContract.TaskEntry.parseTaskId(uri);
                selection = RoutineContract.TaskEntry._ID + " = ? ";
                selectionArgs = new String[]{String.valueOf(taskId)};
                break;
            case TODO:
                tableName = RoutineContract.TodoEntry.TABLE_NAME;
                break;
            case DAILY:
                tableName = RoutineContract.DailyEntry.TABLE_NAME;
                break;
            case REPEAT:
                tableName = RoutineContract.RepeatEntry.TABLE_NAME;
                //TODO: cancel notification
                break;
            case TASK_REPEAT:
            case TASK_TODO:
                tableName = type == TASK_REPEAT ? RoutineContract.RepeatEntry.TABLE_NAME : RoutineContract.TodoEntry.TABLE_NAME;
                String where = (type == TASK_REPEAT ? RoutineContract.RepeatEntry.COLUMN_TASK_ID : RoutineContract.TodoEntry.COLUMN_TASK_ID) + " = ? ";
                long todoId = RoutineContract.TaskEntry.parseTaskId(uri);
                String[] args = {String.valueOf(todoId)};
                return writer.delete(tableName, where, args);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int deleted = writer.delete(tableName, selection, selectionArgs);

        if (selection == null || deleted != 0) {
            Context context = getContext();
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.notifyChange(uri, null);
        }

        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase writer = mOpenHelper.getWritableDatabase();
        String tableName;

        int type = sUriMatcher.match(uri);
        switch (type) {
            case TASK_ID:
                tableName = RoutineContract.TaskEntry.TABLE_NAME;
                //TODO: cancel notification
                break;
            case TODO_ID:
                tableName = RoutineContract.TodoEntry.TABLE_NAME;
                break;
            case DAILY_ID:
                tableName = RoutineContract.DailyEntry.TABLE_NAME;
                break;
            case REPEAT_ID:
                tableName = RoutineContract.RepeatEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rowsUpdated = writer.update(tableName, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            ContentResolver contentResolver = getContext().getContentResolver();
            contentResolver.notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case REPEAT:
            case TODO:
                database.beginTransaction();
                int returnCount = 0;
                try {
                    String table = match == REPEAT ? RoutineContract.RepeatEntry.TABLE_NAME : RoutineContract.TodoEntry.TABLE_NAME;
                    for (ContentValues value : values) {
                        long _id = database.insert(table, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }

                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
