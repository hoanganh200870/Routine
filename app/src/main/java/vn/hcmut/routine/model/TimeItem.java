package vn.hcmut.routine.model;

public class TimeItem implements Comparable<TimeItem> {

    public int hour, minute;

    public TimeItem(String time) {
        String[] data = time.split(":");
        hour = Integer.parseInt(data[0]);
        minute = Integer.parseInt(data[1]);
    }

    public static TimeItem getEndOfDay() {
        return new TimeItem(23, 59);
    }

    public TimeItem(int time) {
        hour = time / 3600;
        minute = time % 3600 / 60;
    }

    public TimeItem(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", hour, minute);
    }

    public int getTime() {
        return hour * 3600 + minute * 60;
    }

    @Override
    public int compareTo(TimeItem another) {
        return Integer.compare(getTime(), another.getTime());
    }
}
