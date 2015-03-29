package com.behnke.wifilock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiConnectionReceiver extends BroadcastReceiver {
    WifiLockService wifiLockService;

    public WifiConnectionReceiver() {}

    public WifiConnectionReceiver(WifiLockService wifiService) {
        wifiLockService = wifiService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {

            if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                wifiLockService.checkWifiNetworks();
            }
        }
    }
}
