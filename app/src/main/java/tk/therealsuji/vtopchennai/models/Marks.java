package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "marks")
public class Marks {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "course")
    public String course;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "score")
    public String score;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "weightage")
    public String weightage;

    @ColumnInfo(name = "average")
    public String average;

    @ColumnInfo(name = "posted")
    public String posted;
}
