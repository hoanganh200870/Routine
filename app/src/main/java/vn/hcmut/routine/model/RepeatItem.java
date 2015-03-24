package vn.hcmut.routine.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import vn.hcmut.routine.database.RoutineContract;

public class RepeatItem implements Comparable<RepeatItem>, Parcelable {

    public TimeItem start, finish, interval;

    public boolean contain(TimeItem time) {
        if (finish != null) {
            return time.compareTo(start) == 1 && time.compareTo(finish) == -1;
        }

        return time.compareTo(start) == 0;
    }

    public RepeatItem(JSONObject data) {
        try {
            String start = data.getString(JSON_KEY_START);
            this.start = new TimeItem(start);

            if (data.has(JSON_KEY_FINISH)) {
                String finish = data.getString(JSON_KEY_FINISH);
                this.finish = new TimeItem(finish);
            }

            if (data.has(JSON_KEY_INTERVAL)) {
                String interval = data.getString(JSON_KEY_INTERVAL);
                this.interval = new TimeItem(interval);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final String JSON_KEY_START = "start";
    public static final String JSON_KEY_FINISH = "finish";
    public static final String JSON_KEY_INTERVAL = "interval";

    public JSONObject toJson() {
        try {
            JSONObject object = new JSONObject();
            object.put(JSON_KEY_START, start.toString());
            if (finish != null) {
                object.put(JSON_KEY_FINISH, finish.toString());
            }
            if (interval != null) {
                object.put(JSON_KEY_INTERVAL, interval.toString());
            }
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public RepeatItem(TimeItem start, TimeItem finish, TimeItem interval) {
        this.start = start;
        this.finish = finish;
        this.interval = interval;
    }

    @Override
    public int compareTo(RepeatItem another) {
        return start.compareTo(another.start);
    }

    public static boolean checkList(List<RepeatItem> items, RepeatItem newItem) {
        for (RepeatItem item : items) {

            if (item.finish == null) {
                boolean inValid = newItem.contain(item.start);
                if (inValid) {
                    return false;
                }
            } else {
                boolean inValid = item.contain(newItem.start) || newItem.finish != null && item.contain(newItem.finish);
                if (inValid) {
                    return false;
                }
            }
        }

        return true;
    }

    public ContentValues getValues(long taskId) {
        ContentValues values = new ContentValues();
        values.put(RoutineContract.RepeatEntry.COLUMN_TASK_ID, taskId);
        values.put(RoutineContract.RepeatEntry.COLUMN_START, start.getTime());
        if (finish != null) {
            values.put(RoutineContract.RepeatEntry.COLUMN_FINISH, finish.getTime());
        }

        if (interval != null) {
            values.put(RoutineContract.RepeatEntry.COLUMN_INTERVAL, interval.getTime());
        }
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
