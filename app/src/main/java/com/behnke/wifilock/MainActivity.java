package com.behnke.wifilock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

//TODO: Make backend for better password security.
//TODO: Display a list of saved networks for the user.
//TODO: Delete saved network.
//TODO: Make "running" notification.
//TODO: Make an initial password prompt that goes away.
//TODO: Initial tutorial screen.
//TODO: Scan phone to look for saved networks?

/**
 * The heart of the app. Where everything gets called.
 */
public class MainActivity extends Activity {

    private final Context context = this;

    private DevicePolicyManager dpManager;

    private ComponentName compName;

    private WifiLockService wifiService;
    private WifiManager wifiManager;

    private Button startButton;
    private Button passwordButton;
    private Button netWorkButton;

    private Switch adminSwitch;
    private Switch wifiSwitch;
    private Switch gpsSwitch;

    private EditText editField;
    private EditText confirmField;

    private String password;
    private String passwordConfirm;
    private String passwordFinal = null;

    private Boolean isBound = false;
    private Boolean adminOff = true;
    private Boolean isOn = false;

    private ArrayList<Integer> networkIDs;

    /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection lockConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WifiLockService.WifiBinder binder = (WifiLockService.WifiBinder) service;
            wifiService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button)findViewById(R.id.startButton);
        passwordButton = (Button)findViewById(R.id.passwordButton);
        netWorkButton = (Button)findViewById(R.id.netWorkButton);
        adminSwitch = (Switch)findViewById(R.id.admin);
        wifiSwitch = (Switch)findViewById(R.id.wifi);
        gpsSwitch = (Switch)findViewById(R.id.gps);
        editField = (EditText)findViewById(R.id.editPassword);
        confirmField = (EditText)findViewById(R.id.confirmPassword);

        dpManager = (DevicePolicyManager)this.getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, WifiLockReceiver.class);

        if (!dpManager.isAdminActive(compName)) {
            startButton.setEnabled(false);
        } else {
            startButton.setEnabled(true);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WifiLockService.class);

                if (!isOn && checkStart()) {
                    startButton.setText("Stop");
                    isOn = true;
                    wifiService.setWifi(wifiSwitch.isChecked());
                    wifiService.setRunGPS(gpsSwitch.isChecked());
                    wifiService.setComponentName(compName);
                    wifiService.setPassword(passwordFinal);
                    wifiService.setWifiManager(wifiManager);
                    wifiService.runLocker(networkIDs);

                    startService(intent);
                }

                else {
                    stopService(intent);

                    if (isBound) {
                        unbindService(lockConnection);
                        isBound = false;
                    }

                    startButton.setText("Start");
                    isOn = false;
                }
            }
        });

        //TODO: Limit the password to 4 - 17 characters per Androids default setting.
        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = editField.getText().toString();
                passwordConfirm = confirmField.getText().toString();

                if (password.equals(passwordConfirm)) {
                    passwordFinal = password;
                    Toast.makeText(context,
                            "Your password has been set.",
                            Toast.LENGTH_SHORT).show();

                    if (dpManager.isAdminActive(compName)) {
                        startButton.setEnabled(true);
                    }
                } else {
                    Toast.makeText(context,
                            "Your entered passwords did not match!",
                            Toast.LENGTH_SHORT).show();

                    startButton.setEnabled(false);
                }
            }
        });

        adminSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (adminOff) {
                    Intent adminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

                    adminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);

                    adminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "This app needs extra permissions in " +
                                    "order to enable and disable your lock screen.");

                    startActivityForResult(adminIntent, 1);
                } else {
                    dpManager.removeActiveAdmin(compName);

                        startButton.setEnabled(false);
                        adminSwitch.setChecked(false);
                        adminOff = true;
                }
            }
        });

        netWorkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ID = wifiManager.getConnectionInfo().getNetworkId();

                if (ID == -1) {
                    Toast.makeText(context,
                            "You must be connected to a Wifi Network!",
                            Toast.LENGTH_SHORT).show();
                }

                else {

                    if (isOn) {
                        wifiService.addID(ID);
                    }

                    networkIDs.add(ID);

                    Toast.makeText(context,
                                   "Your network has been added.",
                                   Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openManageNetworks() {

    }

    private void setState() {

        if (wifiService != null && wifiService.isRebound()) {
            passwordFinal = wifiService.getPassword();

            wifiSwitch.setChecked(wifiService.getRunWifi());
            gpsSwitch.setChecked(wifiService.getRunGPS());
            editField.setText(passwordFinal);
            confirmField.setText(passwordFinal);
            startButton.setEnabled(true);
            startButton.setText("Stop");
            isOn = true;
        }

        if (!dpManager.isAdminActive(compName)) {
            startButton.setEnabled(false);
            adminSwitch.setChecked(false);
            adminOff = true;
        } else {
            startButton.setEnabled(true);
            adminSwitch.setChecked(true);
            adminOff = false;
        }

        if (passwordFinal == null) {
            isOn = false;
            startButton.setText("Start");
            startButton.setEnabled(false);
        }

        gpsSwitch.setEnabled(false);
    }

    /**
     * Checks whether or not the app can be started.
     * @return Boolean
     */
    protected Boolean checkStart() {
        return  passwordFinal != null &&
                adminSwitch.isChecked() &&
                (wifiSwitch.isChecked() || gpsSwitch.isChecked());
    }

    @Override
    protected void onStart() {
        super.onStart();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Intent intent = new Intent(context, WifiLockService.class);
        bindService(intent, lockConnection, Context.BIND_AUTO_CREATE);

        //TODO: Change to HashSet.
        //TODO: Store all info. Only pass IDs to the service.
        networkIDs = new ArrayList<Integer>();

        setState();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isBound) {
            unbindService(lockConnection);
            isBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_networks:
                openManageNetworks();
                return true;

            case R.id.action_settings:
                //TODO: Settings Menu
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
