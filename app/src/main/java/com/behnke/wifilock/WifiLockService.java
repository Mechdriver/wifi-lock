package com.behnke.wifilock;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class WifiLockService extends Service {
    private Boolean runLoop = false;
    private Boolean runWifi = false;
    private Boolean runGPS = false;
    private long lockTime = 0;

    private DevicePolicyManager polMan;
    private ComponentName compName;

    private final IBinder wifiBinder = new WifiBinder();

    public class WifiBinder extends Binder {
        WifiLockService getService() {
            return WifiLockService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return wifiBinder;
    }

    public void runLocker() {
        polMan = (DevicePolicyManager)this.getSystemService(this.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, WifiLockReceiver.class);

        Toast.makeText(this, "This is working, yay!", Toast.LENGTH_SHORT).show();

        while (runLoop) {
            if (runWifi) {
                if (wifiCheck()) {
                    unlockScreen(polMan);
                }

                else if (runGPS) {
                    if (gpsCheck()) {
                        unlockScreen(polMan);
                    }

                    else {
                        lockScreen(polMan);
                    }
                }

                else {
                    lockScreen(polMan);
                }
            }

            else if (runGPS) {
                if (gpsCheck()) {
                    unlockScreen(polMan);
                }

                else {
                    lockScreen(polMan);
                }
            }

            else {
                lockScreen(polMan);
            }
        }
    }

    public void setWifi(Boolean wifi) {
        runWifi = wifi;
    }

    public void setRunGPS(Boolean gps) {
        runGPS = gps;
    }

    private Boolean wifiCheck() {
        return true;
    }

    private Boolean gpsCheck() {
        return true;
    }

    private void lockScreen(DevicePolicyManager polMan) {

    }

    private void unlockScreen(DevicePolicyManager polMan) {

    }
}
