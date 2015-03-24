package vn.hcmut.routine.model;

import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.hcmut.routine.database.RoutineContract;

public class TaskItem {

    public String title, note;
    public boolean notify;
    public boolean[] daily;
    public List<RepeatItem> repeat;
    public List<String> todo;
    public boolean enable;

    public TaskItem(JSONObject data) {
        try {
            title = data.getString("title");
            note = data.getString("note");
            notify = data.getBoolean("notify");
            JSONArray dailyArray = data.getJSONArray("daily");
            int count = dailyArray.length();
            daily = new boolean[count];
            for (int i = 0; i < count; i++) {
                daily[i] = dailyArray.getBoolean(0);
            }

            JSONArray repeatArray = data.getJSONArray("repeat");
            repeat = new ArrayList<>();
            for (int i = 0; i < repeatArray.length(); i++) {
                JSONObject repeatItem = repeatArray.getJSONObject(i);
                repeat.add(new RepeatItem(repeatItem));
            }

            JSONArray todoArray = data.getJSONArray("todo");
            todo = new ArrayList<>();
            for (int i = 0; i < todoArray.length(); i++) {
                String todoItem = todoArray.getString(i);
                todo.add(todoItem);
            }

            enable = data.getBoolean("enable");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final String JSON_KEY_TITLE = "title";
    public static final String JSON_KEY_NOTE = "note";
    public static final String JSON_KEY_NOTIFY = "notify";
    public static final String JSON_KEY_DAILY = "daily";
    public static final String JSON_KEY_REPEAT = "repeat";
    public static final String JSON_KEY_TODO = "todo";
    public static final String JSON_KEY_ENABLE = "enable";

    public JSONObject toJson() {
        try {
            JSONObject object = new JSONObject();
            object.put(JSON_KEY_TITLE, title);
            object.put(JSON_KEY_NOTE, note);
            object.put(JSON_KEY_NOTIFY, notify);

            JSONArray dailyArray = new JSONArray();
            for (boolean dailyItem : daily) {
                dailyArray.put(dailyItem);
            }
            object.put(JSON_KEY_DAILY, dailyArray);

            JSONArray repeatArray = new JSONArray();
            for (RepeatItem repeatItem : repeat) {
                repeatArray.put(repeatItem.toJson());
            }
            object.put(JSON_KEY_REPEAT, repeatArray);

            JSONArray todoArray = new JSONArray();
            for (String todoItem : todo) {
                todoArray.put(todoItem);
            }
            object.put(JSON_KEY_TODO, todoArray);

            object.put(JSON_KEY_ENABLE, enable);
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

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
