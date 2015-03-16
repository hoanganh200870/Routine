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
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import vn.hcmut.routine.R;
import vn.hcmut.routine.activity.TaskScreen;
import vn.hcmut.routine.adapter.TaskAdapter;
import vn.hcmut.routine.database.RoutineContract;

public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private boolean isTwoPane;

    public void setIsTwoPane(boolean isTwoPane) {
        this.isTwoPane = isTwoPane;
    }

    public interface Callback {
        void onItemSelected(long taskId);
    }

    private TaskAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        final Context context = getActivity();
        view.findViewById(R.id.btnCreate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskScreen.class);
                startActivity(intent);
            }
        });

        adapter = new TaskAdapter(context);
        ListView lvTask = (ListView) view.findViewById(R.id.lvTask);
        lvTask.setAdapter(adapter);

        lvTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Activity activity = getActivity();
                if (activity instanceof Callback) {
                    ((Callback) activity).onItemSelected(id);
                }
            }
        });

        registerForContextMenu(lvTask);
        getLoaderManager().initLoader(0, null, this);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri uri = RoutineContract.TaskEntry.CONTENT_URI;
        Context context = getActivity();
        return new CursorLoader(context, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        adapter.swapCursor(cursor);
        if (isTwoPane && cursor.moveToFirst()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    int index = cursor.getColumnIndex(RoutineContract.TaskEntry._ID);
                    long id = cursor.getLong(index);
                    Activity activity = getActivity();
                    if (activity instanceof Callback) {
                        ((Callback) activity).onItemSelected(id);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
