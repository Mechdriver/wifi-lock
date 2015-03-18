package com.behnke.wifilock;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;

public class WifiLockService extends Service {
    private Boolean runWifi = false;
    private Boolean runGPS = false;
    private ArrayList<Integer> netWorkIDs;
    private long lockTime = 0;
    private String password;

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

    /**
     *
     * @param pass The password set by the user in the app.
     * @param cName The component name used by the WifiLock Admin.
     */
    public void runLocker(String pass, ComponentName cName, ArrayList<Integer> IDs, WifiManager wifiMan) {
        polMan = (DevicePolicyManager)this.getSystemService(DEVICE_POLICY_SERVICE);
        compName = cName;
        password = pass;
        netWorkIDs = IDs;
        WifiConnectionReceiver wifiConRec = new WifiConnectionReceiver(this, wifiMan, netWorkIDs);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(wifiConRec ,intentFilter);

        Toast.makeText(this, "This is working, yay!", Toast.LENGTH_SHORT).show();

        //Boolean runLoop = true;

        //TODO: Make this event based rather than busy waiting based.
/*        while (runLoop) {
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
        }*/
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

    /**
     * Adds network IDs to the white list.
     * @param ID NetworkID used to identify white-listed networks.
     */
    public void addID(Integer ID) {netWorkIDs.add(ID);}

    public void clearIDs() {netWorkIDs.clear();}

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
     */
    protected void lockScreen() {
        //TODO: Reset the password minimum length requirement.
        polMan.resetPassword(password, 0);
    }

    /**
     * Unlocks the screen by setting an empty password.
     * This is done by eliminating password length requirements.
     */
    protected void unlockScreen() {
        polMan.setPasswordMinimumLength(compName, 0);
        polMan.resetPassword("", 0);
    }
}
