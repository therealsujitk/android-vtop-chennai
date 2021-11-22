package tk.therealsuji.vtopchennai.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "attendance",
        foreignKeys = @ForeignKey(
                entity = Course.class,
                parentColumns = "id",
                childColumns = "course_id",
                onDelete = CASCADE
        )
)
public class Attendance {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "course_id")
    public Integer courseId;

    @ColumnInfo(name = "attended")
    public Integer attended;

    @ColumnInfo(name = "total")
    public Integer total;

    @ColumnInfo(name = "percentage")
    public Integer percentage;
}
