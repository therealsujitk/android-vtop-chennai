package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spotlight")
public class Spotlight {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "announcement")
    public String announcement;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "link")
    public String link;
}
