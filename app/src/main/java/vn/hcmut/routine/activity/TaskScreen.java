package vn.hcmut.routine.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import vn.hcmut.routine.R;
import vn.hcmut.routine.adapter.RepeatAdapter;
import vn.hcmut.routine.adapter.RoutineSyncAdapter;
import vn.hcmut.routine.adapter.TodoAdapter;
import vn.hcmut.routine.database.RoutineContract;
import vn.hcmut.routine.database.RoutineDbHelper;
import vn.hcmut.routine.model.RepeatItem;
import vn.hcmut.routine.model.TaskItem;
import vn.hcmut.routine.model.TimeItem;
import vn.hcmut.routine.receiver.RoutineAlarm;
import vn.hcmut.routine.util.CustomTimePickerDialog;
import vn.hcmut.routine.util.CustomToast;

public class TaskScreen extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener, TextView.OnEditorActionListener {

    private EditText txtTitle, txtNote;
    private ListView lvRepeat, lvTodo;
    private LinearLayout groupDayRepeat;
    private View btnSave, btnCancel;
    private RadioGroup rgMode;

    private String title = "", note = "";
    private boolean notify = true;
    private RepeatAdapter repeatAdapter;
    private ArrayList<RepeatItem> repeatData;
    private TodoAdapter todoAdapter = null;
    private ArrayList<String> todoData = null;
    private boolean[] dailyData = null;

    public static final String EXTRA_TASK_ID = "task_id";
    private long taskId;

    private static final int TASK_LOADER = 1;
    private static final int DAILY_LOADER = 2;
    private static final int REPEAT_LOADER = 3;
    private static final int TODO_LOADER = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);

//            CustomToast.makeToast(this, "Loading task " + taskId);

            if (taskId != -1) {
                LoaderManager loaderManager = getLoaderManager();
                loaderManager.initLoader(TASK_LOADER, null, this);
                loaderManager.initLoader(DAILY_LOADER, null, this);
                loaderManager.initLoader(REPEAT_LOADER, null, this);
                loaderManager.initLoader(TODO_LOADER, null, this);
            }
        } else {
            title = savedInstanceState.getString(KEY_TITLE, "");
            note = savedInstanceState.getString(KEY_TITLE, "");
            notify = savedInstanceState.getBoolean(KEY_NOTIFY, true);
            dailyData = savedInstanceState.getBooleanArray(KEY_DAILY);
            repeatData = savedInstanceState.getParcelableArrayList(KEY_REPEAT);
            todoData = savedInstanceState.getStringArrayList(KEY_TODO);
        }

        initLayout();
    }

    private static String KEY_TITLE = "title";
    private static String KEY_NOTE = "note";
    private static String KEY_NOTIFY = "notify";
    private static String KEY_DAILY = "daily";
    private static String KEY_REPEAT = "repeat";
    private static String KEY_TODO = "todo";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String title = txtTitle.getText().toString();
        outState.putString(KEY_TITLE, title);

        String note = txtNote.getText().toString();
        outState.putString(KEY_NOTE, note);

        boolean notify = rgMode.getCheckedRadioButtonId() == R.id.radioPushNotification;
        outState.putBoolean(KEY_NOTIFY, notify);

        outState.putBooleanArray(KEY_DAILY, dailyData);
        outState.putParcelableArrayList(KEY_REPEAT, repeatData);
        outState.putStringArrayList(KEY_TODO, todoData);
    }

    private void initLayout() {
        setContentView(R.layout.task_screen);

        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtTitle.setText(title);

        txtNote = (EditText) findViewById(R.id.txtNote);
        txtNote.setText(note);

        rgMode = (RadioGroup) findViewById(R.id.rgMode);
        rgMode.check(notify ? R.id.radioPushNotification : R.id.radioOpenApp);

        groupDayRepeat = (LinearLayout) findViewById(R.id.groupDayRepeat);
        int childCount = groupDayRepeat.getChildCount();
        if (dailyData == null) {
            dailyData = new boolean[childCount];
        }
        for (int index = 0; index < childCount; index++) {
            final View view = groupDayRepeat.getChildAt(index);
            int resId = dailyData[index] ? R.drawable.day_selected_background : R.drawable.day_no_select_background;
            view.setBackgroundResource(resId);
            final int finalIndex = index;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dailyData[finalIndex] = !dailyData[finalIndex];
                    int resId = dailyData[finalIndex] ? R.drawable.day_selected_background : R.drawable.day_no_select_background;
                    view.setBackgroundResource(resId);
                }
            });
        }

        if (repeatData == null) {
            repeatData = new ArrayList<>();
        }
        repeatAdapter = new RepeatAdapter(repeatData, this);

        lvRepeat = (ListView) findViewById(R.id.lvRepeat);
        lvRepeat.setAdapter(repeatAdapter);
        lvRepeat.setDivider(null);
        setHeightListView(repeatAdapter, lvRepeat);

        if (todoData == null) {
            todoData = new ArrayList<>();
        }
        todoAdapter = new TodoAdapter(todoData, this);

        lvTodo = (ListView) findViewById(R.id.lvTodo);
        lvTodo.setAdapter(todoAdapter);
        lvTodo.setDivider(null);
        setHeightListView(todoAdapter, lvTodo);

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static int childHeight = 102;

    public void setHeightListView(BaseAdapter adapter, ListView parent) {
        int count = adapter.getCount();
        adapter.notifyDataSetChanged();
        View child = parent.getChildAt(0);

        if (child != null) {
            childHeight = child.getHeight();
        }

        int height = (childHeight + 2) * count;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        float marginBottom = getResources().getDimension(R.dimen.margin_bottom_with_other);
        params.setMargins(0, 0, 0, (int) marginBottom);
        parent.setLayoutParams(params);
    }

    @Override
    public void onClick(final View v) {

        final int id = v.getId();
        switch (id) {
            case R.id.imgAddTodoItem:
                // hide keyboard
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                final TodoAdapter.ViewHolder viewHolder = (TodoAdapter.ViewHolder) v.getTag();
                Object data = todoAdapter.getItem(viewHolder.position);
                if (data == null) {
                    String item = viewHolder.txtTodoItem.getText().toString();
                    if (item.isEmpty()) {
                        String message = getString(R.string.msg_please_enter_what_to_do_first);
                        CustomToast.makeToast(this, message);
                        return;
                    }

                    todoData.add(item);
                } else {
                    todoData.remove(viewHolder.position);
                }

                todoAdapter.notifyDataSetChanged();
                setHeightListView(todoAdapter, lvTodo);
                break;
            case R.id.txtStart:
                final RepeatAdapter.ViewHolder startHolder = (RepeatAdapter.ViewHolder) v.getTag();
                final String sCurrentTime = startHolder.txtStart.getText().toString();
                final String finishTime = startHolder.txtFinish.getText().toString();

                int sHourOfDay, sMinute;
                if (sCurrentTime.isEmpty()) {
                    Calendar calendar = Calendar.getInstance();
                    sHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    sMinute = calendar.get(Calendar.MINUTE);
                } else {
                    TimeItem timeItem = new TimeItem(sCurrentTime);
                    sHourOfDay = timeItem.hour;
                    sMinute = timeItem.minute;
                }

                CustomTimePickerDialog sDialog = new CustomTimePickerDialog(this, sHourOfDay, sMinute, new CustomTimePickerDialog.OnTimeSet() {
                    @Override
                    public void onTimeSet(TimeItem time) {
                        if (!finishTime.isEmpty()) {
                            TimeItem finish = new TimeItem(finishTime);
                            if (time.compareTo(finish) == 1) {
                                String message = getString(R.string.msg_start_time_must_be_less_than_finish_time);
                                CustomToast.makeToast(TaskScreen.this, message);
                                return;
                            }
                        }

                        startHolder.txtStart.setText(time.toString());
                    }
                });

                String sTitle = getString(R.string.set_start_time);
                sDialog.setTitle(sTitle);
                sDialog.show();
                break;
            case R.id.txtFinish:
                final RepeatAdapter.ViewHolder finishHolder = (RepeatAdapter.ViewHolder) v.getTag();

                String startTime = finishHolder.txtStart.getText().toString();
                if (startTime.isEmpty()) {
                    String message = getString(R.string.msg_please_enter_start_time_first);
                    CustomToast.makeToast(TaskScreen.this, message);
                    return;
                }

                final TimeItem start = new TimeItem(startTime);
                final String fCurrentTime = finishHolder.txtFinish.getText().toString();

                int fHourOfDay, fMinute;
                if (fCurrentTime.isEmpty()) {
                    fHourOfDay = start.hour;
                    fMinute = start.minute + 1;
                } else {
                    TimeItem timeItem = new TimeItem(fCurrentTime);
                    fHourOfDay = timeItem.hour;
                    fMinute = timeItem.minute;
                }

                CustomTimePickerDialog fDialog = new CustomTimePickerDialog(this, fHourOfDay, fMinute, new CustomTimePickerDialog.OnTimeSet() {
                    @Override
                    public void onTimeSet(TimeItem time) {
                        if (start.compareTo(time) >= 0) {
                            String message = getString(R.string.msg_finish_time_must_be_greater_than_start_time);
                            CustomToast.makeToast(TaskScreen.this, message);
                            return;
                        }

                        finishHolder.txtFinish.setText(time.toString());
                    }
                });

                String fTitle = getString(R.string.set_start_time);
                fDialog.setTitle(fTitle);
                fDialog.show();
                break;
            case R.id.txtInterval:
                final RepeatAdapter.ViewHolder intervalHolder = (RepeatAdapter.ViewHolder) v.getTag();

                String iStartTime = intervalHolder.txtStart.getText().toString();
                if (iStartTime.isEmpty()) {
                    String message = getString(R.string.msg_please_enter_start_time_first);
                    CustomToast.makeToast(TaskScreen.this, message);
                    return;
                }

                final String iCurrentTime = intervalHolder.txtInterval.getText().toString();

                int iHourOfDay, iMinute;
                if (iCurrentTime.isEmpty()) {
                    iHourOfDay = 0;
                    iMinute = 1;
                } else {
                    TimeItem timeItem = new TimeItem(iCurrentTime);
                    iHourOfDay = timeItem.hour;
                    iMinute = timeItem.minute + 1;
                }

                CustomTimePickerDialog iDialog = new CustomTimePickerDialog(this, iHourOfDay, iMinute, new CustomTimePickerDialog.OnTimeSet() {
                    @Override
                    public void onTimeSet(TimeItem time) {
                        if (time.hour + time.minute == 0) {
                            String message = getString(R.string.msg_interval_must_be_greater_than_0);
                            CustomToast.makeToast(TaskScreen.this, message);
                            return;
                        }

                        String finish = intervalHolder.txtFinish.getText().toString();
                        if (finish.isEmpty()) {
                            TimeItem endDay = TimeItem.getEndOfDay();
                            intervalHolder.txtFinish.setText(endDay.toString());
                        }
                        intervalHolder.txtInterval.setText(time.toString());
                    }
                });

                String iTitle = getString(R.string.set_interval_time);
                iDialog.setTitle(iTitle);
                iDialog.show();
                break;
            case R.id.imgAddRepeatItem:
                RepeatAdapter.ViewHolder repeatHolder = (RepeatAdapter.ViewHolder) v.getTag();
                RepeatItem item = (RepeatItem) repeatAdapter.getItem(repeatHolder.position);
                if (item == null) {
                    String addStartTime = repeatHolder.txtStart.getText().toString();
                    if (addStartTime.isEmpty()) {
                        String message = getString(R.string.msg_please_enter_start_time_first);
                        CustomToast.makeToast(TaskScreen.this, message);
                        return;
                    }

                    TimeItem addStart = new TimeItem(addStartTime);

                    TimeItem addFinish = null;
                    String addFinishTime = repeatHolder.txtFinish.getText().toString();
                    if (!addFinishTime.isEmpty()) {
                        addFinish = new TimeItem(addFinishTime);
                    }

                    String intervalTime = repeatHolder.txtInterval.getText().toString();
                    TimeItem interval = null;
                    if (!intervalTime.isEmpty()) {
                        interval = new TimeItem(intervalTime);
                    }

                    if (addFinish == null && interval != null) {
                        addFinish = TimeItem.getEndOfDay();
                    }

                    RepeatItem newItem = new RepeatItem(addStart, addFinish, interval);
                    boolean valid = RepeatItem.checkList(repeatData, newItem);
                    if (valid) {
                        repeatData.add(newItem);
                        Collections.sort(repeatData);
                    } else {
                        String message = getString(R.string.msg_the_periods_dont_allow_overlap);
                        CustomToast.makeToast(this, message);
                    }
                } else {
                    repeatData.remove(repeatHolder.position);
                }

                repeatAdapter.notifyDataSetChanged();
                setHeightListView(repeatAdapter, lvRepeat);
                break;
            case R.id.btnSave:
                String title = txtTitle.getText().toString();
                if (title.isEmpty()) {
                    txtTitle.setHint(getString(R.string.msg_please_enter_title));
                    txtTitle.requestFocus();
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(txtTitle, 0);
                    return;
                }

                String note = txtNote.getText().toString();

                boolean notify = rgMode.getCheckedRadioButtonId() == R.id.radioPushNotification;
                TaskItem task = new TaskItem(title, note, notify, dailyData, repeatData, todoData, true);

                long newTaskId = RoutineDbHelper.saveTask(this, task, taskId);
                if (newTaskId != -1) {
                    RoutineSyncAdapter.syncImmediately(this, false);
                    finish();
                }

                int msgId = newTaskId != -1 ? R.string.msg_save_task_successful : R.string.msg_could_not_save_task;
                String message = getString(msgId);
                CustomToast.makeToast(this, message);

                break;
            case R.id.btnCancel:
                finish();
                break;
        }

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            String data = v.getText().toString();
            if (!data.isEmpty()) {
                todoData.add(data);
                todoAdapter.notifyDataSetChanged();
                setHeightListView(todoAdapter, lvTodo);
            }
        }

        return false;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        Uri uri;
        switch (id) {
            case TASK_LOADER:
                uri = RoutineContract.TaskEntry.getTaskUri(taskId);
                break;
            case DAILY_LOADER:
                uri = RoutineContract.TaskEntry.getDailyUri(taskId);
                break;
            case REPEAT_LOADER:
                uri = RoutineContract.TaskEntry.getRepeatUri(taskId);
                break;
            case TODO_LOADER:
                uri = RoutineContract.TaskEntry.getTodoUri(taskId);
                break;
            default:
                return null;
        }

        cursorLoader = new CursorLoader(this, uri, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();

        switch (id) {
            case TASK_LOADER:
                if (data.moveToFirst()) {
                    int titleIndex = data.getColumnIndex(RoutineContract.TaskEntry.COLUMN_TITLE);
                    String title = data.getString(titleIndex);
                    txtTitle.setText(title);

                    int noteIndex = data.getColumnIndex(RoutineContract.TaskEntry.COLUMN_NOTE);
                    String note = data.getString(noteIndex);
                    txtNote.setText(note);

                    int notifyIndex = data.getColumnIndex(RoutineContract.TaskEntry.COLUMN_NOTIFY);
                    int notify = data.getInt(notifyIndex);

                    int radioId = notify == 1 ? R.id.radioPushNotification : R.id.radioOpenApp;
                    rgMode.check(radioId);
                }
                break;
            case DAILY_LOADER:
                if (data.moveToFirst()) {
                    for (int i = 0; i < RoutineContract.DailyEntry.DAYS_OF_WEEK.length; i++) {
                        String columnName = RoutineContract.DailyEntry.DAYS_OF_WEEK[i];
                        int columnIndex = data.getColumnIndex(columnName);
                        int value = data.getInt(columnIndex);
                        dailyData[i] = value == 1;
                    }
                }

                for (int i = 0; i < dailyData.length; i++) {
                    View view = groupDayRepeat.getChildAt(i);
                    int resId = dailyData[i] ? R.drawable.day_selected_background : R.drawable.day_no_select_background;
                    view.setBackgroundResource(resId);
                }
                break;
            case TODO_LOADER:
                List<String> todo = new ArrayList<>();
                while (data.moveToNext()) {
                    int index = data.getColumnIndex(RoutineContract.TodoEntry.COLUMN_TODO);
                    String value = data.getString(index);
                    todo.add(value);
                }
                todoData.clear();
                todoData.addAll(todo);
                repeatAdapter.notifyDataSetChanged();
                setHeightListView(todoAdapter, lvTodo);
                break;
            case REPEAT_LOADER:
                List<RepeatItem> repeat = new ArrayList<>();
                while (data.moveToNext()) {
                    int startIndex = data.getColumnIndex(RoutineContract.RepeatEntry.COLUMN_START);
                    int startValue = data.getInt(startIndex);
                    TimeItem start = new TimeItem(startValue);

                    TimeItem finish = null;
                    int finishIndex = data.getColumnIndex(RoutineContract.RepeatEntry.COLUMN_FINISH);
                    if (!data.isNull(finishIndex)) {
                        int finishValue = data.getInt(finishIndex);
                        finish = new TimeItem(finishValue);
                    }

                    TimeItem interval = null;
                    int intervalIndex = data.getColumnIndex(RoutineContract.RepeatEntry.COLUMN_INTERVAL);
                    if (!data.isNull(intervalIndex)) {
                        int intervalValue = data.getInt(intervalIndex);
                        interval = new TimeItem(intervalValue);
                    }

                    RepeatItem repeatItem = new RepeatItem(start, finish, interval);
                    repeat.add(repeatItem);
                }

                repeatData.clear();
                repeatData.addAll(repeat);

                repeatAdapter.notifyDataSetChanged();
                setHeightListView(repeatAdapter, lvRepeat);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
