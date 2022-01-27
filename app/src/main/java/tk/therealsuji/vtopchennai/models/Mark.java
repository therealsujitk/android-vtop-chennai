package tk.therealsuji.vtopchennai.models;

import static androidx.room.ForeignKey.SET_NULL;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "marks",
        foreignKeys = @ForeignKey(
                entity = Course.class,
                parentColumns = "id",
                childColumns = "course_id",
                onDelete = SET_NULL
        )
)
public class Mark {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "course_id")
    public Integer courseId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "score")
    public Double score;

    @ColumnInfo(name = "max_score")
    public Double maxScore;

    @ColumnInfo(name = "weightage")
    public Double weightage;

    @ColumnInfo(name = "max_weightage")
    public Double maxWeightage;

    @ColumnInfo(name = "average")
    public Double average;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "is_read")
    public Boolean isRead = false;

    @ColumnInfo(name = "signature")
    public Integer signature;

    public static class AllData {
        public String courseCode;
        public String courseTitle;
        public String courseType;
        public String title;
        public Double score;
        public Double maxScore;
        public Double weightage;
        public Double maxWeightage;
        public Double average;
        public String status;
        public Boolean isRead;
    }
}
