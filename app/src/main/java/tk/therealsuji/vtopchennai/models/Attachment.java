package tk.therealsuji.vtopchennai.models;

import static androidx.room.ForeignKey.CASCADE;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "attachments",
        foreignKeys = @ForeignKey(
                entity = Assignment.class,
                parentColumns = "id",
                childColumns = "assignment_id",
                onDelete = CASCADE
        )
)
public class Attachment implements Parcelable {
    @Ignore
    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
    @PrimaryKey
    public int id;
    @ColumnInfo(name = "assignment_id")
    public Integer assignmentId;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "url")
    public String url;
    @ColumnInfo(name = "mimetype")
    public String mimetype;
    @ColumnInfo(name = "size")
    public Long size;

    public Attachment() {
        // Required empty constructor
    }

    protected Attachment(Parcel in) {
        name = in.readString();
        mimetype = in.readString();
        url = in.readString();
        size = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(mimetype);
        parcel.writeString(url);
        parcel.writeLong(size);
    }
}
