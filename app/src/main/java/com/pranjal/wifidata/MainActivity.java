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
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Button wifiButton;
    private WifiManager wifiManager;
    private WifiRepository wifiRepository;
    private SensorManager sensorManager;
    private BroadcastReceiver wifiScanReceiver;
    private Button magneticButton;
    Context context = this;
    private Button gsmButton;
    private TelephonyManager telephonyManager;
    private Button syncButton;
    String [] appPermissions = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForPermissions();
        wifiButton = findViewById(R.id.startwifi);
        wifiRepository = new WifiRepository(getApplication());
        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWifiData();
            }
        });
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
        magneticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSensorData();
            }
        });
        gsmButton = findViewById(R.id.startgsm);
        gsmButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View view) {
                getGsmData();
            }
        });
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        syncButton = findViewById(R.id.sync);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sync();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sync() throws ExecutionException, InterruptedException, IOException {
        List<wifi> l = wifiRepository.getallwifi();
      List<Magnetic> l2 = wifiRepository.getallmagnetic();
        List<gsm> l3 = wifiRepository.getallgsm();
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir,"file1" + ".csv");
        File file2 = new File(exportDir,"file2" + ".csv");
        file2.createNewFile();
        File file3 = new File(exportDir,"file3" + ".csv");
        file3.createNewFile();
        file.createNewFile();
        CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
        for(wifi w:l){
            String arrStr[] = new String[4];
            arrStr[0] =""+w.id;
            arrStr[1]=w.ssid;
            arrStr[2]=""+w.frequency;
            arrStr[3]=""+w.level;
            csvWrite.writeNext(arrStr);
        }
        csvWrite.close();
        CSVWriter csvWriter2 = new CSVWriter(new FileWriter(file2));
        for(gsm g:l3){
            String arrStr[] = new String[2];
            arrStr[0]=""+g.id;
            arrStr[1]=""+g.strength;
            csvWriter2.writeNext(arrStr);
        }
        csvWriter2.close();
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file3));
        for(Magnetic m:l2){
            String arrStr[] = new String[4];
            arrStr[0]=""+m.id;
            arrStr[1]=""+m.magx;
            arrStr[2]=""+m.magy;
            arrStr[3]=""+m.magz;
            csvWriter.writeNext(arrStr);
        }
        csvWriter.close();
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

        gsm g = new gsm(t);
        wifiRepository.insertGSM(g);
        Toast.makeText(this,"Updated" ,Toast.LENGTH_LONG).show();
    }

    private void getWifiData() {
        wifiButton.setVisibility(View.INVISIBLE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            Toast.makeText(this,"Scan Failed",Toast.LENGTH_LONG).show();
            scanFailure();
        }
        else{
            Toast.makeText(this,"Passed",Toast.LENGTH_LONG).show();
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
            Toast.makeText(this,"No WIFI Found",Toast.LENGTH_LONG).show();
            wifiButton.setVisibility(View.VISIBLE);
            context.unregisterReceiver(wifiScanReceiver);
            return;
        }
        for (ScanResult s: results){
            String ssid = s.SSID;
            int frequency = s.frequency;
            int level = s.level;
            wifi w = new wifi(ssid,frequency,level);
            wifiRepository.insert(w);
        }
        Toast.makeText(this,"Updated",Toast.LENGTH_LONG).show();
        wifiButton.setVisibility(View.VISIBLE);
        context.unregisterReceiver(wifiScanReceiver);
    }

    private void scanFailure() {

        wifiButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            float magX = sensorEvent.values[0];
            float magY = sensorEvent.values[1];
            float magZ = sensorEvent.values[2];
            Magnetic m = new Magnetic(magX, magY, magZ);
            wifiRepository.insertMagnetic(m);
            Toast.makeText(this,"Updated",Toast.LENGTH_LONG).show();
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}

