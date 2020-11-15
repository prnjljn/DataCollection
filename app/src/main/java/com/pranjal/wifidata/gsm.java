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
    @ColumnInfo(name ="time")
    public String time;

    public gsm(int strength,String time){
        this.strength = strength;
        this.time = time;
    }
}
