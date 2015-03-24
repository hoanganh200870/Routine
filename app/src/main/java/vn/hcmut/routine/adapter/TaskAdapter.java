package vn.hcmut.routine.adapter;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import vn.hcmut.routine.R;
import vn.hcmut.routine.activity.TaskScreen;
import vn.hcmut.routine.database.RoutineContract;

public class TaskAdapter extends CursorAdapter {

    public TaskAdapter(Context context) {
        super(context, null, true);
    }

    public static class ViewHolder {
        public TextView txtTitle, txtNote;
        public View imgEdit, imgDelete;
        public ToggleButton tgEnable;

        public ViewHolder(View view) {
            txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtNote = (TextView) view.findViewById(R.id.txtNote);
            imgEdit = view.findViewById(R.id.imgEdit);
            imgDelete = view.findViewById(R.id.imgDelete);
            tgEnable = (ToggleButton) view.findViewById(R.id.tgEnable);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.task_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int titleIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_TITLE);
        String title = cursor.getString(titleIndex);
        viewHolder.txtTitle.setText(title);

        int noteIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_NOTE);
        String note = cursor.getString(noteIndex);
        viewHolder.txtNote.setText(note);

        int enableIndex = cursor.getColumnIndex(RoutineContract.TaskEntry.COLUMN_ENABLE);
        boolean enable = cursor.getInt(enableIndex) == 1;
        viewHolder.tgEnable.setChecked(enable);

        int idIndex = cursor.getColumnIndex(RoutineContract.TaskEntry._ID);
        final long id = cursor.getLong(idIndex);
        viewHolder.tgEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Uri uri = RoutineContract.TaskEntry.getTaskUri(id);
                ContentValues values = new ContentValues();
                values.put(RoutineContract.TaskEntry.COLUMN_ENABLE, isChecked ? 1 : 0);
                ContentResolver contentResolver = context.getContentResolver();
                String where = RoutineContract.TaskEntry._ID + " = ? ";
                String[] args = {String.valueOf(id)};
                int update = contentResolver.update(uri, values, where, args);
                RoutineSyncAdapter.syncImmediately(context, false);
                Log.e("Toggle", id + " " + isChecked + " " + update);
            }
        });

        viewHolder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskScreen.class);
                intent.putExtra(TaskScreen.EXTRA_TASK_ID, id);
                context.startActivity(intent);
            }
        });

        viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ok = context.getString(R.string.positive_button_label);
                String message = context.getString(R.string.confirm_delete_message);
                String title = context.getString(R.string.delete_dialog_title);
                String cancel = context.getString(R.string.cancel);
                new AlertDialog.Builder(context).setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContentResolver contentResolver = context.getContentResolver();
                                Uri uri = RoutineContract.TaskEntry.getTaskUri(id);
                                contentResolver.delete(uri, null, null);

                                RoutineSyncAdapter.syncImmediately(context, false);
                            }
                        })
                        .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .show();
            }
        });
    }
}
