package tk.therealsuji.vtopchennai.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "slots",
        foreignKeys = @ForeignKey(
                entity = Course.class,
                parentColumns = "id",
                childColumns = "course_id",
                onDelete = CASCADE
        )
)
public class Slot {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "slot")
    public String slot;

    @ColumnInfo(name = "course_id")
    public Integer courseId;
}
