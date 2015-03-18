package com.behnke.wifilock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import java.util.ArrayList;

public class WifiConnectionReceiver extends BroadcastReceiver {
    WifiLockService wifiLockService;
    ArrayList<Integer> networkIDs;
    WifiManager wifiManager;

    public WifiConnectionReceiver() {
    }

    public WifiConnectionReceiver(WifiLockService wifiService, WifiManager wifiMan, ArrayList<Integer> IDs) {
        wifiLockService = wifiService;
        networkIDs = IDs;
        wifiManager = wifiMan;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {

            if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {

                for (Integer ID : networkIDs) {
                    if (ID.equals(wifiManager.getConnectionInfo().getNetworkId())) {
                        wifiLockService.unlockScreen();
                    }
                }
            } else {
                wifiLockService.lockScreen();
            }
        }
    }
}
