package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

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

    @Ignore
    public static final int TYPE_LAB = 1;

    @Ignore
    public static final int TYPE_PROJECT = 2;

    @Ignore
    public static final int TYPE_THEORY = 3;

    public static class AllData {
        public String courseTitle;
        public String courseCode;
        public String courseType;
        public String faculty;
        public String slot;
        public String venue;
        public Integer attendanceAttended;
        public Integer attendanceTotal;
        public Integer attendancePercentage;
        public Integer unreadMarkCount; // Used to store number of unread marks in a course

        @Ignore
        public List<String> slots;
    }
}
