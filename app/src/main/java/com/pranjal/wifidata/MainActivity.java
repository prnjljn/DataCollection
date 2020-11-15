package com.pranjal.wifidata;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private CheckBox wifiButton;
    private WifiManager wifiManager;
    private WifiRepository wifiRepository;
    private SensorManager sensorManager;
    private BroadcastReceiver wifiScanReceiver;
    private CheckBox magneticButton;
    Context context = this;
    private CheckBox gsmButton;
    private TelephonyManager telephonyManager;
    private Button syncButton;
    private Button startButton;
    private Button stopButton;
    String [] appPermissions = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForPermissions();
        wifiButton = findViewById(R.id.startwifi);
        wifiRepository = new WifiRepository(getApplication());
        wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);

        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                scanSuccess();
            }
        };
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticButton = findViewById(R.id.startmagnetic);
        gsmButton = findViewById(R.id.startgsm);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        syncButton = findViewById(R.id.sync);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sync();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        startButton = findViewById(R.id.start);
        stopButton  = findViewById(R.id.stop);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });
    }

    private void stop() {
        if(wifiButton.isChecked()){
            context.unregisterReceiver(wifiScanReceiver);
        }
        if(magneticButton.isChecked()){
            sensorManager.unregisterListener(this);
        }
        startButton.setVisibility(View.VISIBLE);
    }

    private void start() {
        if(wifiButton.isChecked()){
            getWifiData();
        }
        if(magneticButton.isChecked()){
            getSensorData();
        }
        startButton.setVisibility(View.INVISIBLE);
    }

    private void sync() throws ExecutionException, InterruptedException {
        List<wifi> l = wifiRepository.getallwifi();
        List<Magnetic> l2 = wifiRepository.getallmagnetic();
        List<gsm> l3 = wifiRepository.getallgsm();
        new Writer(l,l2,l3,context).execute();
        Toast.makeText(this,"Exported",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 123){
            boolean denied  = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    denied = true;
                }
            }
            if(denied){
                Toast.makeText(this,"The app needs Permissions.Please restart.",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkForPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        for(String per:appPermissions){
            if(ContextCompat.checkSelfPermission(context,per)!=PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(per);
            }
        }
        if(!permissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,permissionsNeeded.toArray(new String[permissionsNeeded.size()]),123);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getGsmData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        CellInfo cellInfo = telephonyManager.getAllCellInfo().get(0);
        int t= 0;
        if (cellInfo instanceof CellInfoCdma) {
            t = ((CellInfoCdma) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoGsm) {
            t =  ((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm();
        }
        if (cellInfo instanceof CellInfoLte) {
            t =  ((CellInfoLte) cellInfo).getCellSignalStrength().getDbm();
        }

        gsm g = new gsm(t,getTime());
        wifiRepository.insertGSM(g);
    }

    private void getWifiData() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            Toast.makeText(this,"Scan Failed",Toast.LENGTH_LONG).show();
            scanFailure();
        }


    }
    private void getSensorData(){
     sensorManager.registerListener(this,
             sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
             SensorManager.SENSOR_DELAY_NORMAL);
     }
    private void scanSuccess(){
        List<ScanResult> results = wifiManager.getScanResults();
        if(results.size()==0){
            return;
        }
        for (ScanResult s: results){
            String ssid = s.SSID;
            int frequency = s.frequency;
            int level = s.level;
            wifi w = new wifi(ssid,frequency,level,getTime());
            wifiRepository.insert(w);
        }

    }

    private void scanFailure() {

        wifiButton.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            float magX = sensorEvent.values[0];
            float magY = sensorEvent.values[1];
            float magZ = sensorEvent.values[2];
            Magnetic m = new Magnetic(magX, magY, magZ,getTime());
            wifiRepository.insertMagnetic(m);
            if(gsmButton.isChecked()){
                getGsmData();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public String getTime(){
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String str = dateFormat.format(date);
        return str;
    }


}

