package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spotlight")
public class Spotlight {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "announcement")
    public String announcement;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "link")
    public String link;

    @ColumnInfo(name = "is_read")
    public Boolean isRead = false;

    @ColumnInfo(name = "signature")
    public Integer signature;
}
