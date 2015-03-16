package vn.hcmut.routine.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

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
