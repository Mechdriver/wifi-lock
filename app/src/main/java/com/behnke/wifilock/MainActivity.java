package com.behnke.wifilock;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

//TODO: Make backend for better password security

/**
 * The heart of the app. Where everything gets called.
 */
public class MainActivity extends Activity {

    private final Context context = this;

    private DevicePolicyManager dpManager;

    private ComponentName compName;

    private WifiLockService wifiService;

    private Button startButton;
    private Button passwordButton;

    private Switch adminSwitch;
    private Switch wifiSwitch;
    private Switch gpsSwitch;

    private EditText editField;
    private EditText confirmField;

    private String password = null;
    private String passwordConfirm = null;

    private Boolean isBound = false;
    private Boolean adminOff = true;
    private Boolean isOn = false;

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

        //Checks whether or not the app can be started.
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOn) {
                    startButton.setText("Stop");
                    isOn = true;
                    wifiService.setWifi(wifiSwitch.isChecked());
                    wifiService.setRunGPS(gpsSwitch.isChecked());
                    wifiService.runLocker(password, compName);
                }

                else {
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
                }

                else {
                    dpManager.removeActiveAdmin(compName);

                        startButton.setEnabled(false);
                        adminSwitch.setChecked(false);
                        adminOff = true;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(context, WifiLockService.class);
        bindService(intent, lockConnection, Context.BIND_AUTO_CREATE);
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

        if (!dpManager.isAdminActive(compName)) {
            startButton.setEnabled(false);
            adminSwitch.setChecked(false);
            adminOff = true;
        } else {
            startButton.setEnabled(true);
            adminSwitch.setChecked(true);
            adminOff = false;
        }

        if (password == null) {
            startButton.setEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!dpManager.isAdminActive(compName)) {
            startButton.setEnabled(false);
            adminSwitch.setChecked(false);
            adminOff = true;
        } else {
            startButton.setEnabled(true);
            adminSwitch.setChecked(true);
            adminOff = false;
        }
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
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
