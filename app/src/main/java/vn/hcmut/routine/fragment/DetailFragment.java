package vn.hcmut.routine.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import vn.hcmut.routine.R;
import vn.hcmut.routine.database.RoutineContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 1;
    private static final int TODO_LOADER = 2;

    private long taskId;
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_NOTIFICATION = "notification";
    private boolean isNotification;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        taskId = arguments.getLong(EXTRA_TASK_ID, -1);
        if (taskId == -1) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        isNotification = arguments.getBoolean(EXTRA_NOTIFICATION, false);

        View view = inflater.inflate(R.layout.detail_fragment, container, false);
        initLayout(view);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(TASK_LOADER, null, this);
        loaderManager.initLoader(TODO_LOADER, null, this);

        return view;
    }

    private CursorAdapter adapter;
    private ListView lvTodo;
    private TextView txtTitle, txtNote;

    private void initLayout(View view) {
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        txtNote = (TextView) view.findViewById(R.id.txtNote);

        String[] from = {RoutineContract.TodoEntry.COLUMN_TODO};
        int[] to = {R.id.cbTask};

        Context context = getActivity();
        adapter = new SimpleCursorAdapter(context, R.layout.detail_item, null, from, to, 0);
        lvTodo = (ListView) view.findViewById(R.id.lvTodo);
        lvTodo.setAdapter(adapter);

        View dismiss = view.findViewById(R.id.btnDismiss);
        dismiss.setVisibility(isNotification ? View.VISIBLE : View.GONE);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNotification) {
                    getActivity().finish();
                }
            }

        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Uri uri;
        String select = null;
        String[] args = {String.valueOf(taskId)};

        switch (id) {
            case TASK_LOADER:
                uri = RoutineContract.TaskEntry.CONTENT_URI;
                select = RoutineContract.TaskEntry._ID + " = ? ";
                break;
            case TODO_LOADER:
                uri = RoutineContract.TaskEntry.getTodoUri(taskId);
                break;
            default:
                return null;
        }

        Context context = getActivity();
        return new CursorLoader(context, uri, null, select, args, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        int id = cursorLoader.getId();
        switch (id) {
            case TASK_LOADER:
                if (cursor.moveToFirst()) {
                    int titleIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_TITLE);
                    String title = cursor.getString(titleIndex);
                    txtTitle.setText(title);

                    int noteIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_NOTE);
                    String note = cursor.getString(noteIndex);
                    txtNote.setText(note);
                }
                break;
            case TODO_LOADER:
                adapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
