package vn.hcmut.routine.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import vn.hcmut.routine.sync.RoutineAuthenticator;

public class RoutineAuthenticationService extends Service {

    private RoutineAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();

        mAuthenticator = new RoutineAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
