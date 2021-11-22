package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "staff")
public class Staff {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "key")
    public String key;

    @ColumnInfo(name = "value")
    public String value;
}
