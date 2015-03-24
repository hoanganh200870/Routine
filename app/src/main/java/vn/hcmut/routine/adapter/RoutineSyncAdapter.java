package vn.hcmut.routine.adapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.hcmut.routine.R;
import vn.hcmut.routine.activity.TaskScreen;
import vn.hcmut.routine.database.RoutineContract;
import vn.hcmut.routine.database.RoutineDbHelper;
import vn.hcmut.routine.model.RepeatItem;
import vn.hcmut.routine.model.TaskItem;
import vn.hcmut.routine.model.TimeItem;
import vn.hcmut.routine.util.Constants;
import vn.hcmut.routine.util.PreferenceUtils;

public class RoutineSyncAdapter extends AbstractThreadedSyncAdapter {

    public RoutineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        boolean isGetData = extras.getBoolean("isGetData");

        Log.e("Sync", "Begin " + isGetData);

        final Context context = getContext();
        if (isGetData) {
            AQuery aQuery = new AQuery(context);

            int userId = PreferenceUtils.getInt(Constants.USER_ID, context, -1);
            String url = Constants.getDataApi(userId);

            AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    if (object != null) {
                        try {
                            JSONArray items = object.getJSONArray("data");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                TaskItem task = new TaskItem(item);

                                RoutineDbHelper.saveTask(context, task, -1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            aQuery.ajax(url, JSONObject.class, callback);
        } else {
            JSONArray tasks = RoutineDbHelper.getAllTasks(context);
            AQuery aQuery = new AQuery(context);

            int userId = PreferenceUtils.getInt(Constants.USER_ID, context, -1);
            String url = Constants.getDataApi(userId);

            Map<String, String> params = new HashMap<>();
            params.put("data", tasks.toString());

            AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    Log.e("Sync", "Done " + object);
                }
            };

            aQuery.ajax(url, params, JSONObject.class, callback);
        }
    }

    public static void syncImmediately(Context context, boolean isGetData) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isGetData", isGetData);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        Account syncAccount = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        ContentResolver.requestSync(syncAccount, authority, bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        String name = context.getString(R.string.app_name);
        String type = context.getString(R.string.sync_account_type);
        Account newAccount = new Account(name, type);

        String password = accountManager.getPassword(newAccount);
        if (null == password) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static final int SYNC_INTERVAL = 60 * 60 * 24;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static void onAccountCreated(Account newAccount, Context context) {
        RoutineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        String authority = context.getString(R.string.content_authority);
        ContentResolver.setSyncAutomatically(newAccount, authority, true);
        syncImmediately(context, true);
    }
}
