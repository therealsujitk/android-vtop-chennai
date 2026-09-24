package tk.therealsuji.vtopchennai.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "timetable",
        foreignKeys = {
                @ForeignKey(
                        entity = Slot.class,
                        parentColumns = "id",
                        childColumns = "sunday",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Slot.class,
                        parentColumns = "id",
                        childColumns = "monday",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Slot.class,
                        parentColumns = "id",
                        childColumns = "tuesday",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Slot.class,
                        parentColumns = "id",
                        childColumns = "wednesday",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Slot.class,
                        parentColumns = "id",
                        childColumns = "thursday",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Slot.class,
                        parentColumns = "id",
                        childColumns = "friday",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Slot.class,
                        parentColumns = "id",
                        childColumns = "saturday",
                        onDelete = CASCADE
                )
        }
)
public class Timetable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "start_time")
    public String startTime;

    @ColumnInfo(name = "end_time")
    public String endTime;

    @ColumnInfo(name = "sunday")
    public Integer sunday;

    @ColumnInfo(name = "monday")
    public Integer monday;

    @ColumnInfo(name = "tuesday")
    public Integer tuesday;

    @ColumnInfo(name = "wednesday")
    public Integer wednesday;

    @ColumnInfo(name = "thursday")
    public Integer thursday;

    @ColumnInfo(name = "friday")
    public Integer friday;

    @ColumnInfo(name = "saturday")
    public Integer saturday;

    public static class AllData {
        public int slotId;
        public String startTime;
        public String endTime;
        public String courseType;
        public String courseCode;
        public String courseTitle;
        public String venue;
        public Integer attendancePercentage;
    }
}
