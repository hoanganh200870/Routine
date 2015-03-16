package vn.hcmut.routine.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import vn.hcmut.routine.R;
import vn.hcmut.routine.fragment.DetailFragment;

public class DetailScreen extends Activity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_NOTIFICATION = "notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_screen);

        if (savedInstanceState == null) {
            DetailFragment detailFragment = new DetailFragment();
            Bundle args = new Bundle();

            Intent intent = getIntent();
            long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
            boolean isNotification = intent.getBooleanExtra(EXTRA_NOTIFICATION, false);

            args.putLong(EXTRA_TASK_ID, taskId);
            args.putBoolean(EXTRA_NOTIFICATION, isNotification);
            detailFragment.setArguments(args);

            FragmentManager supportFragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.detail_container, detailFragment);
            fragmentTransaction.commit();
        }
    }
}
