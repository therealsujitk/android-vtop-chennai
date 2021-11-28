package tk.therealsuji.vtopchennai.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Assignment implements Parcelable {
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
    public int activityId;
    public String course;
    public String title;
    public String intro;
    public List<Attachment> introAttachments;
    public Date dueDate;
    public Date cutoffDate;
    public int id;

    public Assignment() {
        // Required empty constructor
    }

    protected Assignment(Parcel in) {
        id = in.readInt();
        activityId = in.readInt();
        course = in.readString();
        title = in.readString();
        intro = in.readString();

        introAttachments = new ArrayList<>();
        in.readList(introAttachments, (ClassLoader) Attachment.CREATOR);

        dueDate = new Date(in.readLong());
        cutoffDate = new Date(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(activityId);
        parcel.writeString(course);
        parcel.writeString(title);
        parcel.writeString(intro);
        parcel.writeList(introAttachments);
        parcel.writeLong(dueDate.getTime());
        parcel.writeLong(cutoffDate.getTime());
    }

    public static class Attachment implements Parcelable {
        public String name;
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
        public String url;
        public int size;
        public String mimetype;

        public Attachment() {
            // Required empty constructor
        }

        protected Attachment(Parcel in) {
            name = in.readString();
            mimetype = in.readString();
            url = in.readString();
            size = in.readInt();
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
            parcel.writeInt(size);
        }
    }
}
