package com.pranjal.wifidata;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "gsm")
public class gsm {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="strength")
    public int strength;

    public gsm(int strength){
        this.strength = strength;
    }
}
