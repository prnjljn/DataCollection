package com.pranjal.wifidata;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Writer extends AsyncTask<Void, Void, Void> {
    List<wifi> l;
    List<Magnetic> l2;
    List<gsm> l3;
    Context c;
    Writer (List<wifi> w,List<Magnetic> m,List<gsm> g,Context context){
        l=w;
        l2=m;
        l3=g;
        c=context;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        File exportDir = new File(c.getExternalFilesDir(""), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir,"file1" + ".csv");
        File file2 = new File(exportDir,"file2" + ".csv");
        try {
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file3 = new File(exportDir,"file3" + ".csv");
        try {
            file3.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWrite = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(wifi w:l){
            String arrStr[] = new String[5];
            arrStr[0] =""+w.id;
            arrStr[1]=w.ssid;
            arrStr[2]=""+w.frequency;
            arrStr[3]=""+w.level;
            arrStr[4]=""+w.time;
            csvWrite.writeNext(arrStr);
        }
        try {
            csvWrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter2 = null;
        try {
            csvWriter2 = new CSVWriter(new FileWriter(file2));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(gsm g:l3){
            String arrStr[] = new String[3];
            arrStr[0]=""+g.id;
            arrStr[1]=""+g.strength;
            arrStr[2]=""+g.time;
            csvWriter2.writeNext(arrStr);
        }
        try {
            csvWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSVWriter csvWriter = null;
        try {
            csvWriter = new CSVWriter(new FileWriter(file3));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Magnetic m:l2){
            String arrStr[] = new String[5];
            arrStr[0]=""+m.id;
            arrStr[1]=""+m.magx;
            arrStr[2]=""+m.magy;
            arrStr[3]=""+m.magz;
            arrStr[4]=""+m.time;
            csvWriter.writeNext(arrStr);
        }
        try {
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
