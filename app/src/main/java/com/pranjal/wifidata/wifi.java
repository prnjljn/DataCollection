package com.pranjal.wifidata;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wifi_table")
public class wifi {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    public int id;
    @NonNull
    @ColumnInfo(name = "ssid")
    public String ssid;

    @NonNull
    @ColumnInfo(name = "frequency")
    public int frequency;

    @NonNull
    @ColumnInfo(name = "level")
    public int level;

    public wifi (String ssid,int frequency,int level){
        this.frequency=frequency;
        this.ssid=ssid;
        this.level = level;
    }

}