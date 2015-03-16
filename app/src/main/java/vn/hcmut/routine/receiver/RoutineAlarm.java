package vn.hcmut.routine.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Calendar;

import vn.hcmut.routine.R;
import vn.hcmut.routine.activity.DetailScreen;
import vn.hcmut.routine.activity.HomeScreen;
import vn.hcmut.routine.database.RoutineContract;

public class RoutineAlarm extends BroadcastReceiver {

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String TASK_ID = "task_id";
    public static final String REPEAT_ID = "repeat_id";

    public static final int CHANGE = 0;
    public static final int DAILY = 1;
    public static final int REPEAT = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ContentResolver contentResolver = context.getContentResolver();

        int type = intent.getIntExtra(TYPE, -1);
        long taskId = intent.getLongExtra(TASK_ID, -1);
        long repeatId = intent.getLongExtra(REPEAT_ID, -1);

        int id = intent.getIntExtra(ID, -1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        Log.e("Alarm", "type: " + type + ", task: " + taskId + ", repeat: " + repeatId + ", alarm: " + id);

        switch (type) {
            case DAILY:
                Action dailyAction = checkTaskEnable(contentResolver, taskId);
                switch (dailyAction) {
                    case DO:
                        Action action = checkDailyEnable(contentResolver, taskId, today);
                        switch (action) {
                            case DO:
                                Log.e("Daily Do", "Set repeat");
                                setRepeatForDay(alarmManager, context, contentResolver, taskId);
                                break;
                            case SKIP:
                                Log.e("Daily Do", "Skip today");
                                break;
                            case CANCEL:
                                Log.e("Daily Do", "Cancelled");
                                alarmManager.cancel(pendingIntent);
                                break;
                        }
                        break;
                    case CANCEL:
                        Log.e("Daily Cancelled", "Cancelled");
                        alarmManager.cancel(pendingIntent);
                        break;
                }
                break;
            case REPEAT:
                Action repeatAction = checkTaskEnable(contentResolver, taskId);
                switch (repeatAction) {
                    case DO:
                        Cursor cursor = getRepeatItem(contentResolver, repeatId);
                        if (cursor != null && cursor.moveToFirst()) {
                            long finish = cursor.getLong(2);
                            long now = System.currentTimeMillis();
                            if (finish == 0 || now < finish * 1000 + getTimeFromBeginOfDayInMillis()) {
                                Uri uri = RoutineContract.TaskEntry.getTaskUri(taskId);
                                Cursor tCursor = contentResolver.query(uri, TASK_PROJECTION, null, null, null);
                                if (tCursor.moveToFirst()) {
                                    String title = tCursor.getString(1);
                                    String note = tCursor.getString(2);
                                    int notify = tCursor.getInt(3);

                                    pushNotification(context, taskId, title, note, notify);
                                }

                                Log.e("Repeat Do", "Notification " + finish + " " + cursor);
                            } else {
                                Log.e("Repeat Do", "Cancelled Done " + finish);
                                alarmManager.cancel(pendingIntent);
                            }
                        } else {
                            Log.e("Repeat Do", "Cancelled <- data");
                            alarmManager.cancel(pendingIntent);
                        }
                        break;
                    case CANCEL:
                        Log.e("Repeat Do", "Cancelled <- task");
                        alarmManager.cancel(pendingIntent);
                        break;
                }
                break;
            case CHANGE:
                Action taskAction = checkTaskEnable(contentResolver, taskId);
                switch (taskAction) {
                    case DO:
                        Log.e("Change Do", "Set daily");
                        setDaily(alarmManager, contentResolver, context, taskId);
                        break;
                    case CANCEL:
                        Log.e("Change Cancelled", "");
                        // do nothing
                        break;
                }

                break;
        }
    }

    private static final String[] TASK_PROJECTION = {
            RoutineContract.TaskEntry._ID,
            RoutineContract.TaskEntry.COLUMN_TITLE,
            RoutineContract.TaskEntry.COLUMN_NOTE,
            RoutineContract.TaskEntry.COLUMN_NOTIFY,
    };

    private void setRepeatForDay(AlarmManager alarmManager, Context context, ContentResolver contentResolver, long taskId) {
        Uri repeatUri = RoutineContract.TaskEntry.getRepeatUri(taskId);
        Cursor cursor = contentResolver.query(repeatUri, REPEAT_PROJECTION, null, null, null, null);
        while (cursor.moveToNext()) {
            long repeatId = cursor.getLong(0);
            int start = cursor.getInt(1);
            int finish = cursor.getInt(2);
            int interval = cursor.getInt(3);
            if (start + interval > finish) {
                interval = -1;
            }

            Log.e("Repeat", "Set " + start + "/" + interval + " " + taskId + "-" + repeatId);
            setRepeat(alarmManager, context, start, interval, taskId, repeatId);
        }

    }

    private static enum Action {
        SKIP, CANCEL, DO
    }

    public static final String[] REPEAT_PROJECTION = {
            RoutineContract.RepeatEntry._ID,
            RoutineContract.RepeatEntry.COLUMN_START,
            RoutineContract.RepeatEntry.COLUMN_FINISH,
            RoutineContract.RepeatEntry.COLUMN_INTERVAL
    };

    private Cursor getRepeatItem(ContentResolver contentResolver, long repeatId) {
        Uri uri = RoutineContract.RepeatEntry.getRepeatUri(repeatId);
        Cursor cursor = contentResolver.query(uri, REPEAT_PROJECTION, null, null, null, null);
        return cursor;
    }

    private Action checkDailyEnable(ContentResolver contentResolver, long taskId, int today) {
        Uri dailyUri = RoutineContract.TaskEntry.getDailyUri(taskId);
        int dayOfWeek = today > 1 ? today - 2 : 6;
        String[] projection = {RoutineContract.DailyEntry.DAYS_OF_WEEK[dayOfWeek]};
        Cursor dailyCursor = contentResolver.query(dailyUri, projection, null, null, null, null);

        if (dailyCursor.moveToFirst()) {
            int value = dailyCursor.getInt(0);
            if (value == 1) {
                return Action.DO;
            }

            return Action.SKIP;
        }

        return Action.CANCEL;
    }

    private Action checkTaskEnable(ContentResolver contentResolver, long taskId) {
        Uri uri = RoutineContract.TaskEntry.getTaskUri(taskId);
        String[] projection = {RoutineContract.TaskEntry.COLUMN_ENABLE};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            int enable = cursor.getInt(0);
            if (enable == 1) {
                return Action.DO;
            }
        }

        return Action.CANCEL;
    }

    private void setRepeat(AlarmManager alarmManager, Context context, int start, int interval, long taskId, long repeatId) {
        int id = (int) System.currentTimeMillis();

        Intent intent = new Intent(context, RoutineAlarm.class);
        intent.putExtra(TYPE, REPEAT);
        intent.putExtra(TASK_ID, taskId);
        intent.putExtra(REPEAT_ID, repeatId);
        intent.putExtra(ID, id);

        PendingIntent pendingRepeat = PendingIntent.getBroadcast(context, id, intent, 0);
        long now = getTimeFromBeginOfDayInMillis();
        if (interval > 0) {
            alarmManager.setInexactRepeating(AlarmManager.RTC, now + start * 1000, interval * 1000, pendingRepeat);
        } else {
            alarmManager.set(AlarmManager.RTC, now + start * 1000, pendingRepeat);
        }
    }

    public long getTimeFromBeginOfDayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    private void setDaily(AlarmManager alarmManager, ContentResolver contentResolver, Context context, long taskId) {
        Uri repeatUri = RoutineContract.TaskEntry.getRepeatUri(taskId);
        Cursor cursor = contentResolver.query(repeatUri, REPEAT_PROJECTION, null, null, null, null);
        while (cursor.moveToNext()) {
            long repeatId = cursor.getLong(0);
            long start = cursor.getLong(1);

            int id = (int) System.currentTimeMillis();

            Intent intent = new Intent(context, RoutineAlarm.class);
            intent.putExtra(TYPE, DAILY);
            intent.putExtra(TASK_ID, taskId);
            intent.putExtra(REPEAT_ID, repeatId);
            intent.putExtra(ID, id);

            long now = getTimeFromBeginOfDayInMillis();
            PendingIntent pendingRepeat = PendingIntent.getBroadcast(context, id, intent, 0);
            start = now + 1000 * start;
            if (start < System.currentTimeMillis()) {
                start += AlarmManager.INTERVAL_DAY;
            }

            Log.e("Daily - Set Repeat", id + "-" + ((start - System.currentTimeMillis()) / 1000 / 60.0));
            alarmManager.setInexactRepeating(AlarmManager.RTC, start, AlarmManager.INTERVAL_DAY, pendingRepeat);
        }
    }

    public void pushNotification(Context context, long taskId, String title, String note, long notify) {

        Log.e("Notification", taskId + "-" + title + "-" + note + "-" + notify);

        Intent resultIntent = new Intent(context, DetailScreen.class);
        resultIntent.putExtra(DetailScreen.EXTRA_TASK_ID, taskId);
        resultIntent.putExtra(DetailScreen.EXTRA_NOTIFICATION, true);

        if (notify == 0) {
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(resultIntent);
            return;
        }

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title).setContentText(note)
                .setVibrate(new long[]{1000, 1000}).setAutoCancel(true)
                .setTicker(title);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(HomeScreen.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        Notification notification = mBuilder.build();
        mNotificationManager.notify(0, notification);

    }

}
