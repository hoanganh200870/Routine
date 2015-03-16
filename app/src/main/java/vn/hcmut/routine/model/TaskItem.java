package vn.hcmut.routine.model;

import android.content.ContentValues;

import java.util.List;

import vn.hcmut.routine.database.RoutineContract;

public class TaskItem {

    public String title, note;
    public boolean notify;
    public boolean[] daily;
    public List<RepeatItem> repeat;
    public List<String> todo;
    public boolean enable;

    public TaskItem(String title, String note, boolean notify, boolean[] daily, List<RepeatItem> repeat, List<String> todo, boolean enable) {
        this.title = title;
        this.note = note;
        this.notify = notify;
        this.daily = daily;
        this.repeat = repeat;
        this.todo = todo;
        this.enable = enable;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();

        values.put(RoutineContract.TaskEntry.COLUMN_TITLE, title);
        values.put(RoutineContract.TaskEntry.COLUMN_NOTE, note);
        values.put(RoutineContract.TaskEntry.COLUMN_NOTIFY, notify ? 1 : 0);
        values.put(RoutineContract.TaskEntry.COLUMN_ENABLE, enable ? 1 : 0);

        return values;
    }

    public ContentValues getDailyValues(long taskId) {
        ContentValues values = new ContentValues();
        values.put(RoutineContract.DailyEntry.COLUMN_TASK_ID, taskId);
        for (int i = 0; i < RoutineContract.DailyEntry.DAYS_OF_WEEK.length; i++) {
            if (i < daily.length) {
                String key = RoutineContract.DailyEntry.DAYS_OF_WEEK[i];
                int value = daily[i] ? 1 : 0;
                values.put(key, value);
            }
        }
        return values;
    }

    public ContentValues[] getRepeatValues(long taskId) {
        int size = repeat.size();
        ContentValues[] values = new ContentValues[size];

        for (int i = 0; i < size; i++) {
            RepeatItem repeatItem = repeat.get(i);
            values[i] = repeatItem.getValues(taskId);
        }
        return values;
    }

    public ContentValues[] getTodoValues(long taskId) {
        int size = todo.size();
        ContentValues[] values = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            String todoItem = todo.get(i);
            values[i] = new ContentValues();
            values[i].put(RoutineContract.TodoEntry.COLUMN_TASK_ID, taskId);
            values[i].put(RoutineContract.TodoEntry.COLUMN_TODO, todoItem);
        }
        return values;
    }
}
