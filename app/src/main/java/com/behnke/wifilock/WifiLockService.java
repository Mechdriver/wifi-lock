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
    private ArrayList<Integer> networkIDs;
    private String password;

    private DevicePolicyManager polMan;
    private WifiManager wifiManager;
    private ComponentName compName;

    private final IBinder wifiBinder = new WifiBinder();

    public class WifiBinder extends Binder {
        WifiLockService getService() {
            return WifiLockService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        polMan = (DevicePolicyManager)this.getSystemService(DEVICE_POLICY_SERVICE);
        WifiConnectionReceiver wifiConRec = new WifiConnectionReceiver(this);
        networkIDs = new ArrayList<Integer>();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(wifiConRec ,intentFilter);

        //Toast.makeText(this, "This is working, yay!", Toast.LENGTH_SHORT).show();

        return wifiBinder;
    }

    /**
     * Called when the service should start work.
     * @param IDs Safe Wifi IDs
     */
    public void runLocker(ArrayList<Integer> IDs) {
        networkIDs.addAll(IDs);
        checkWifiNetworks();
    }

    public void setWifiManager(WifiManager wifiMan) {
        wifiManager = wifiMan;
    }

    public void setPassword(String pass) {
        password = pass;
    }

    public void setComponentName(ComponentName name) {
        compName = name;
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
    public void addID(Integer ID) {
        networkIDs.add(ID);
        checkWifiNetworks();
    }

    public void delID(Integer ID) {
        networkIDs.remove(ID);
        checkWifiNetworks();
    }

    public void clearIDs() {
        networkIDs.clear();
        checkWifiNetworks();
    }

    //TODO: Implement wifi functionality.
    /**
     * Checks whether or not we are in a "safe" wifi zone.
     */
    public void checkWifiNetworks() {
        for (Integer ID : networkIDs) {
            if (ID.equals(wifiManager.getConnectionInfo().getNetworkId())) {
                unlockScreen();
                return;
            }
        }
        lockScreen();
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
