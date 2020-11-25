package com.pranjal.wifidata;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class WifiRepository {
    private wifiDao mWifiDao;
    private magneticDao mmagneticDao;
    private gsmDao mgsmDao;


    WifiRepository(Application application) {
        WifiRoomDatabase db = WifiRoomDatabase.getDatabase(application);
        mWifiDao = db.wifiDao();
        mmagneticDao = db.magneticDao();
        mgsmDao = db.gsmDao();
    }

    public List<wifi> getallwifi(String loc) throws ExecutionException, InterruptedException {
        return new wifiTask((mWifiDao),loc).execute().get();
    }
    public List<gsm> getallgsm(String loc) throws ExecutionException, InterruptedException {
        return new gsmTask(mgsmDao,loc).execute().get();
    }
    public List<Magnetic> getallmagnetic(String loc) throws ExecutionException, InterruptedException {
        return new magneticTask(mmagneticDao,loc).execute().get();
    }


    public void insert (wifi w) {
        new insertAsyncTask(mWifiDao).execute(w);
    }

    public void insertMagnetic (Magnetic m){new magneticAsyncTask(mmagneticDao).execute(m);}

    public void insertGSM(gsm g){new gsmAsyncTask(mgsmDao).execute(g);}

    private static class insertAsyncTask extends AsyncTask<wifi, Void, Void> {

        private wifiDao mAsyncTaskDao;

        insertAsyncTask(wifiDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final wifi... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class magneticAsyncTask extends AsyncTask<Magnetic, Void, Void> {

        private magneticDao mAsyncTaskDao;

        magneticAsyncTask(magneticDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Magnetic... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
    private static class gsmAsyncTask extends AsyncTask<gsm, Void, Void> {

        private gsmDao mAsyncTaskDao;

        gsmAsyncTask(gsmDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final gsm... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class wifiTask extends  AsyncTask<Void,Void,List<wifi>>{
        private wifiDao mAsyncTaskDao;
        private String location;
        wifiTask(wifiDao dao,String loc) {
            mAsyncTaskDao = dao;
            location = loc;
        }
        @Override
        protected List<wifi> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAllWifi(location);
        }
    }

    private static class gsmTask extends  AsyncTask<Void,Void,List<gsm>>{
        private gsmDao mAsyncTaskDao;
        private String location;
        gsmTask(gsmDao dao,String loc) {
            mAsyncTaskDao = dao;
            location = loc;
        }
        @Override
        protected List<gsm> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAllGSM(location);
        }
    }

    private static class magneticTask extends  AsyncTask<Void,Void,List<Magnetic>>{
        private magneticDao mAsyncTaskDao;
        private String location;
        magneticTask(magneticDao dao,String loc) {
            mAsyncTaskDao = dao;
            location =loc;
        }
        @Override
        protected List<Magnetic> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAllMagnetic(location);
        }
    }
}