package com.willowtree.andoridibeacon;

/**
 * Created by david.hodge on 12/4/13.
 */
import java.util.Collection;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.Region;
import com.radiusnetworks.ibeacon.RangeNotifier;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;

public class RangingActivity extends Activity implements IBeaconConsumer {
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        editText = (EditText) findViewById(R.id.rangingText);

        verifyBluetooth();
        iBeaconManager.bind(this);
    }

    private void verifyBluetooth() {
        try {
            if (!IBeaconManager.getInstanceForApplication(this).checkAvailability()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Want to go to the settings and enable bluetooth?");
                builder.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 0);
                    }
                });
                builder.setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }else{
                Toast.makeText(getApplicationContext(), "Device supported!", Toast.LENGTH_SHORT).show();
            }
        }catch (RuntimeException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                    RangingActivity.this.finish();
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    logToDisplay("Beacon " + iBeacons.iterator().next().getAccuracy() + " meters away");
                    logToDisplay("Beacon Proximity = " + Integer.toString(iBeacons.iterator().next().getProximity()));
                    logToDisplay("Beacon Power = " + Integer.toString(iBeacons.iterator().next().getTxPower()));
                }else{
                    logToDisplay("No Beacons near by");
                }
            }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            //emptiness
            e.printStackTrace();
        }
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                editText.append(line+"\n");
            }
        });
    }
}
