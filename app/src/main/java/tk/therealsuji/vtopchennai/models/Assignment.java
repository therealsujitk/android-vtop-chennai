package tk.therealsuji.vtopchennai.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "assignments")
public class Assignment implements Parcelable {
    @Ignore
    public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
        @Override
        public Assignment createFromParcel(Parcel in) {
            return new Assignment(in);
        }

        @Override
        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };
    @PrimaryKey
    public int id;
    @ColumnInfo(name = "course")
    public String course;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "intro")
    public String intro;
    @ColumnInfo(name = "due_date")
    public Long dueDate;
    @ColumnInfo(name = "cutoff_date")
    public Long cutoffDate;
    @Ignore
    public List<Attachment> introAttachments;

    public Assignment() {
        // Required empty constructor
    }

    protected Assignment(Parcel in) {
        id = in.readInt();
        course = in.readString();
        title = in.readString();
        intro = in.readString();
        dueDate = in.readLong();
        cutoffDate = in.readLong();

        if (dueDate == 0) dueDate = null;
        if (cutoffDate == 0) cutoffDate = null;

        introAttachments = new ArrayList<>();
        in.readList(introAttachments, Attachment.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(course);
        parcel.writeString(title);
        parcel.writeString(intro);

        if (dueDate != null) parcel.writeLong(dueDate);
        else parcel.writeLong(0);

        if (cutoffDate != null) parcel.writeLong(cutoffDate);
        else parcel.writeLong(0);

        parcel.writeList(introAttachments);
    }
}
