package vn.hcmut.routine.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.hcmut.routine.model.RepeatItem;
import vn.hcmut.routine.model.TaskItem;
import vn.hcmut.routine.model.TimeItem;
import vn.hcmut.routine.receiver.RoutineAlarm;

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

    public static JSONArray getAllTasks(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(RoutineContract.TaskEntry.CONTENT_URI, null, null, null, null);

        JSONArray tasks = new JSONArray();

        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(RoutineContract.TaskEntry._ID);
            long id = cursor.getLong(idIndex);

            int titleIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_TITLE);
            String title = cursor.getString(titleIndex);

            int noteIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_NOTE);
            String note = cursor.getString(noteIndex);

            int notifyIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_NOTIFY);
            boolean notify = cursor.getInt(notifyIndex) == 1;

            int enableIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_ENABLE);
            boolean enable = cursor.getInt(enableIndex) == 1;

            Uri dailyUri = RoutineContract.TaskEntry.getDailyUri(id);
            Cursor dailyCursor = contentResolver.query(dailyUri, null, null, null, null);

            boolean[] daily = new boolean[RoutineContract.DailyEntry.DAYS_OF_WEEK.length];
            if (dailyCursor.moveToFirst()) {
                for (int i = 0; i < RoutineContract.DailyEntry.DAYS_OF_WEEK.length; i++) {
                    String columnName = RoutineContract.DailyEntry.DAYS_OF_WEEK[i];
                    int columnIndex = dailyCursor.getColumnIndex(columnName);
                    int value = dailyCursor.getInt(columnIndex);
                    daily[i] = value == 1;
                }
            }

            Uri repeatUri = RoutineContract.TaskEntry.getRepeatUri(id);
            Cursor repeatCursor = contentResolver.query(repeatUri, null, null, null, null);

            List<RepeatItem> repeat = new ArrayList<>();
            while (repeatCursor.moveToNext()) {
                int startIndex = repeatCursor.getColumnIndex(RoutineContract.RepeatEntry.COLUMN_START);
                int startValue = repeatCursor.getInt(startIndex);
                TimeItem start = new TimeItem(startValue);

                TimeItem finish = null;
                int finishIndex = repeatCursor.getColumnIndex(RoutineContract.RepeatEntry.COLUMN_FINISH);
                if (!repeatCursor.isNull(finishIndex)) {
                    int finishValue = repeatCursor.getInt(finishIndex);
                    finish = new TimeItem(finishValue);
                }

                TimeItem interval = null;
                int intervalIndex = repeatCursor.getColumnIndex(RoutineContract.RepeatEntry.COLUMN_INTERVAL);
                if (!repeatCursor.isNull(intervalIndex)) {
                    int intervalValue = repeatCursor.getInt(intervalIndex);
                    interval = new TimeItem(intervalValue);
                }

                RepeatItem repeatItem = new RepeatItem(start, finish, interval);
                repeat.add(repeatItem);
            }

            Uri todoUri = RoutineContract.TaskEntry.getTodoUri(id);
            Cursor todoCursor = contentResolver.query(todoUri, null, null, null, null);

            List<String> todo = new ArrayList<>();
            while (todoCursor.moveToNext()) {
                int index = todoCursor.getColumnIndex(RoutineContract.TodoEntry.COLUMN_TODO);
                String value = todoCursor.getString(index);
                todo.add(value);
            }

            TaskItem taskItem = new TaskItem(title, note, notify, daily, repeat, todo, enable);
            JSONObject task = taskItem.toJson();
            tasks.put(task);
        }

        return tasks;
    }

    public static long saveTask(Context context, TaskItem task, long taskId) {
        ContentResolver contentResolver = context.getContentResolver();
        long newTaskId;
        ContentValues taskValues = task.getValues();
        if (taskId != -1) {
            String where = RoutineContract.TaskEntry._ID + " = ? ";
            Uri uri = RoutineContract.TaskEntry.getTaskUri(taskId);
            String[] args = {String.valueOf(taskId)};
            int i = contentResolver.update(uri, taskValues, where, args);
            Log.e("Update", "Task " + taskId + "-" + i);
            newTaskId = taskId;
        } else {
            Uri taskUri = contentResolver.insert(RoutineContract.TaskEntry.CONTENT_URI, taskValues);
            newTaskId = ContentUris.parseId(taskUri);
        }

        if (newTaskId != -1) {
            long dailyId = -1;
            ContentValues dailyValues = task.getDailyValues(newTaskId);
            if (taskId == -1) {
                Uri dailyUri = contentResolver.insert(RoutineContract.DailyEntry.CONTENT_URI, dailyValues);
                dailyId = ContentUris.parseId(dailyUri);
            } else {
                Uri uri = RoutineContract.DailyEntry.getDailyUri(newTaskId);
                String where = RoutineContract.DailyEntry.COLUMN_TASK_ID + " = ? ";
                String[] args = new String[]{String.valueOf(newTaskId)};
                int i = contentResolver.update(uri, dailyValues, where, args);
                Log.e("Update", "Daily " + newTaskId + "-" + i);
            }

            ContentValues[] repeatValues = task.getRepeatValues(newTaskId);
            Uri repeatUri = RoutineContract.TaskEntry.getRepeatUri(newTaskId);
            contentResolver.delete(repeatUri, null, null);
            int countRepeat = contentResolver.bulkInsert(RoutineContract.RepeatEntry.CONTENT_URI, repeatValues);

            ContentValues[] todoValues = task.getTodoValues(newTaskId);
            Uri todoUri = RoutineContract.TaskEntry.getTodoUri(newTaskId);
            contentResolver.delete(todoUri, null, null);
            int countTodo = contentResolver.bulkInsert(RoutineContract.TodoEntry.CONTENT_URI, todoValues);

            Intent intent = new Intent(context, RoutineAlarm.class);
            intent.putExtra(RoutineAlarm.TYPE, RoutineAlarm.CHANGE);
            intent.putExtra(RoutineAlarm.TASK_ID, newTaskId);
            context.sendBroadcast(intent);

            Log.e(taskId == -1 ? "Saved" : "Update", newTaskId + "-" + dailyId + " " + countRepeat + "-" + countTodo);
            return newTaskId;
        }

        return -1;
    }
}
