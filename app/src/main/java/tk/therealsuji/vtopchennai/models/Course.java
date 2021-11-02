package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class Course {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "credits")
    public Integer credits;

    @ColumnInfo(name = "venue")
    public String venue;

    @ColumnInfo(name = "faculty")
    public String faculty;

    public static class AllData {
        public String courseTitle;
        public String courseCode;
        public String courseType;
        public String faculty;
        public String slot;
        public String venue;
        public Integer attendance;
    }
}
