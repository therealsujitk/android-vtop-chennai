package tk.therealsuji.vtopchennai.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(
        tableName = "exams",
        foreignKeys = @ForeignKey(
                entity = Course.class,
                parentColumns = "id",
                childColumns = "course_id",
                onDelete = CASCADE
        )
)
public class Exam {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "course_id")
    public Integer courseId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "start_time")
    public Long startTime;

    @ColumnInfo(name = "end_time")
    public Long endTime;

    @ColumnInfo(name = "venue")
    public String venue;

    @ColumnInfo(name = "seat_location")
    public String seatLocation;

    @ColumnInfo(name = "seat_number")
    public Integer seatNumber;

    public static class AllData {
        public String courseCode;
        public String courseTitle;
        public String slot;
        public Long startTime;
        public Long endTime;
        public String venue;
        public String seatLocation;
        public Integer seatNumber;

        @Ignore
        public List<String> slots;
    }
}
