package com.behnke.wifilock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiConnectionReceiver extends BroadcastReceiver {
    WifiLockService wifiLockService;
    Boolean run;

    public WifiConnectionReceiver() {}

    public WifiConnectionReceiver(WifiLockService wifiService, Boolean wifiRun) {
        wifiLockService = wifiService;
        run = wifiRun;
    }

    public void setWifiRun(Boolean wifiRun) {
        run = wifiRun;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (run && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            wifiLockService.checkWifiNetworks();
        }
    }
}
