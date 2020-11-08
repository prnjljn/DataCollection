package com.pranjal.wifidata;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface gsmDao {
    @Insert
    void insert(gsm g);
    @Query("SELECT * FROM gsm")
    List<gsm> getAllGSM();
}
