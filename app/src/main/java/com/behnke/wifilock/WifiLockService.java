package com.behnke.wifilock;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class WifiLockService extends Service {
    private Boolean runWifi = false;
    private Boolean runGPS = false;
    private long lockTime = 0;

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

    /**
     *
     * @param password The password set by the user in the app.
     * @param cName The component name used by the WifiLock Admin.
     */
    public void runLocker(String password, ComponentName cName) {
        DevicePolicyManager polMan = (DevicePolicyManager)this.getSystemService(DEVICE_POLICY_SERVICE);
        compName = cName;

        Toast.makeText(this, "This is working, yay!", Toast.LENGTH_SHORT).show();

        Boolean runLoop = true;

        //TODO: Make this event based rather than busy waiting based.
        while (runLoop) {
            if (runWifi) {
                if (wifiCheck()) {
                    unlockScreen(polMan);
                    runLoop = false;
                    break;
                }

                else if (runGPS) {
                    if (gpsCheck()) {
                        unlockScreen(polMan);
                    }

                    else {
                        lockScreen(polMan, password);
                    }
                }

                else {
                    lockScreen(polMan, password);
                }
            }

            else if (runGPS) {
                if (gpsCheck()) {
                    unlockScreen(polMan);
                }

                else {
                    lockScreen(polMan, password);
                }
            }

            else {
                lockScreen(polMan, password);
            }
        }
    }

    /**
     * Setter for the status of the wifi switch
     * @param wifi status of the wifi switch
     */
    public void setWifi(Boolean wifi) {
        runWifi = wifi;
    }

    /**
     * Setter for the status of the wifi switch
     * @param gps status of the gps switch
     */
    public void setRunGPS(Boolean gps) {
        runGPS = gps;
    }

    //TODO: Implement wifi functionality.
    /**
     * Checks whether or not we are in a "safe" wifi zone.
     * @return Boolean
     */
    private Boolean wifiCheck() {
        return true;
    }

    //TODO: Implement GPS functionality.
    /**
     * Checks whether or not we are in a "safe" gps zone.
     * @return Boolean
     */
    private Boolean gpsCheck() {
        return true;
    }

    /**
     * Locks the screen by resetting the password to the users entered password.
     * @param polMan The Admin Policy Manager for the WifiLock app.
     * @param password The user's set password created before starting the service.
     */
    private void lockScreen(DevicePolicyManager polMan, String password) {
        //TODO: Reset the password minimum length requirement.
        polMan.resetPassword(password, 0);
    }

    /**
     * Unlocks the screen by setting an empty password.
     * This is done by eliminating password length requirements.
     * @param polMan The Admin Policy Manager for the WifiLock app.
     */
    private void unlockScreen(DevicePolicyManager polMan) {
        polMan.setPasswordMinimumLength(compName, 0);
        polMan.resetPassword("", 0);
    }
}
