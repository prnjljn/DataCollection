package com.pranjal.wifidata;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "magentic_field")
public class Magnetic {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    public int id;

    @ColumnInfo(name="magx")
    public double magx;
    @ColumnInfo(name="magy")
    public double magy;
    @ColumnInfo(name="magz")
    public double magz;
    @ColumnInfo(name ="time")
    public String time;

    public Magnetic(double magx,double magy,double magz,String time){
        this.magx = magx;
        this.magy = magy;
        this.magz = magz;
        this.time = time;
    }
}
