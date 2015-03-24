package vn.hcmut.routine.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import vn.hcmut.routine.adapter.RoutineSyncAdapter;

public class RoutineSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static RoutineSyncAdapter sSunshineSyncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new RoutineSyncAdapter(this, true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }

}
