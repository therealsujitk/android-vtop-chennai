package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timetable_theory")
public class TimetableLab {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "start_time")
    public String startTime;

    @ColumnInfo(name = "end_time")
    public String endTime;

    @ColumnInfo(name = "sun")
    public String sunday;

    @ColumnInfo(name = "mon")
    public String monday;

    @ColumnInfo(name = "tue")
    public String tuesday;

    @ColumnInfo(name = "wed")
    public String wednesday;

    @ColumnInfo(name = "thu")
    public String thursday;

    @ColumnInfo(name = "fri")
    public String friday;

    @ColumnInfo(name = "sat")
    public String saturday;
}
