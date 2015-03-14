package com.behnke.wifilock;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class WifiLockReceiver extends DeviceAdminReceiver {
    public WifiLockReceiver() {
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }
}
