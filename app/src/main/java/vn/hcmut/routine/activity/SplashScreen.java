package vn.hcmut.routine.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import vn.hcmut.routine.R;
import vn.hcmut.routine.adapter.RoutineSyncAdapter;
import vn.hcmut.routine.util.Constants;
import vn.hcmut.routine.util.CustomToast;
import vn.hcmut.routine.util.PreferenceUtils;

public class SplashScreen extends Activity {

    private String deviceId;
    private TextView txtError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        txtError = (TextView) findViewById(R.id.txtError);

        ContentResolver contentResolver = getContentResolver();
        deviceId = Secure.getString(contentResolver, Secure.ANDROID_ID);

        int userId = PreferenceUtils.getInt(Constants.USER_ID, this, -1);
        if (userId != -1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    gotoHomeScreen();
                }
            }, 2000);
        } else {
            register();
        }
    }

    private void register() {
        AQuery aQuery = new AQuery(this);

        Map<String, String> params = new HashMap<>();
        params.put(Constants.PARAM_DEVICE_ID, deviceId);

        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                if (object != null) {
                    try {
                        int userId = object.getInt(Constants.JSON_USER_ID);
                        PreferenceUtils.saveInt(Constants.USER_ID, SplashScreen.this, userId);
                        gotoHomeScreen();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        String message = getString(R.string.message_server_error);
                        txtError.setText(message);
                    }
                } else {
                    String message = getString(R.string.message_on_cant_connect_internet);
                    txtError.setText(message);
                }
            }
        };

        aQuery.ajax(Constants.API_USERS, params, JSONObject.class, callback);
    }

    private void gotoHomeScreen() {
        Intent intent = new Intent(SplashScreen.this, HomeScreen.class);
        startActivity(intent);
        finish();

        RoutineSyncAdapter.getSyncAccount(this);
    }

}
