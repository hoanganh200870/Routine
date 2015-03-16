package vn.hcmut.routine.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class CustomToast {

    public static final int TIME = 1000;

    public static final void makeToast(Context context, String message) {
        makeToast(context, message, TIME);
    }

    public static final void makeToast(Context context, String message, int timeInMilis) {
        final Toast mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mToast.cancel();
            }
        }, timeInMilis);
    }

}
