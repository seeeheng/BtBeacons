package com.example.btbeacons;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconData;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    private long unix_seconds;
    private Socket mSocket;
    private boolean phone_home;
    private String ip;

    private void attemptSend(JSONObject message) {
        mSocket.emit("new message", message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
        PhoneHome app = new PhoneHome();

        Intent intent = getIntent();
        Log.d("socket","Phone Home received= " + intent.getStringExtra(MonitoringActivity.PHONE_HOME));
        phone_home = Boolean.parseBoolean(intent.getStringExtra(MonitoringActivity.PHONE_HOME));
        ip = intent.getStringExtra(MonitoringActivity.IP);
        Log.d("socket","ip received= " + ip);
        if (phone_home == true){
            app.setIP(ip);
            mSocket = app.getSocket();
            mSocket.connect();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                List<BeaconData> beaconDataList = new ArrayList<>();
                if (beacons.size() > 0) {
                    String beaconString = "";
//                    Log.d("beacons","There were " + Integer.toString(beacons.size()) + " beacons.");
                    for(Beacon b : beacons){
                        String beaconMac = b.getBluetoothAddress();
                        double beaconDistance = b.getDistance();
                        DecimalFormat numberFormat = new DecimalFormat("#.00");
                        String beaconDistanceStr = numberFormat.format(beaconDistance);

                        beaconString += beaconMac + " = " + beaconDistanceStr + "m.\n";
//                        Log.d("beacons","address = " + beaconMac);

                        if (phone_home==true){
                            JSONObject beaconData = new JSONObject();
                            try {
                                beaconData.put("mac", beaconMac);
                                beaconData.put("distance", b.getDistance());
                            } catch (JSONException e){
                                throw new RuntimeException(e);
                            }
                            attemptSend(beaconData);
                        }
                    }
                    logToDisplay(beaconString);
                }
            }
        };

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {   }
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                ((EditText)findViewById(R.id.rangingText)).setText(line);
//                EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
//                editText.append(line+"\n");
            }
        });
    }
}
