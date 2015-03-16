package vn.hcmut.routine.util;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TimePicker;

import vn.hcmut.routine.model.RepeatItem;
import vn.hcmut.routine.model.TimeItem;

public class CustomTimePickerDialog implements TimePickerDialog.OnTimeSetListener,
        DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {

    public interface OnTimeSet {
        public void onTimeSet(TimeItem time);
    }

    private TimePickerDialog mTimePickerDialog;
    private TimeItem mTimeItem = null;
    private OnTimeSet listener;

    public CustomTimePickerDialog(Context context, int hour, int minute, OnTimeSet listener) {
        mTimePickerDialog = new TimePickerDialog(context, this, hour, minute, true);
        mTimePickerDialog.setOnCancelListener(this);
        mTimePickerDialog.setOnDismissListener(this);
        this.listener = listener;
    }

    public void setTitle(String title) {
        mTimePickerDialog.setTitle(title);
    }

    public void show() {
        mTimePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mTimeItem = new TimeItem(hourOfDay, minute);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mTimeItem = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mTimeItem != null) {
            listener.onTimeSet(mTimeItem);
        }
    }
}
