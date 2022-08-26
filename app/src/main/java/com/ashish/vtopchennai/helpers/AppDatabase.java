package com.ashish.vtopchennai.helpers;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ashish.vtopchennai.interfaces.AssignmentsDao;
import com.ashish.vtopchennai.interfaces.AttendanceDao;
import com.ashish.vtopchennai.interfaces.CoursesDao;
import com.ashish.vtopchennai.interfaces.MarksDao;
import com.ashish.vtopchennai.interfaces.ReceiptsDao;
import com.ashish.vtopchennai.interfaces.SpotlightDao;
import com.ashish.vtopchennai.interfaces.StaffDao;
import com.ashish.vtopchennai.interfaces.TimetableDao;
import com.ashish.vtopchennai.models.Assignment;
import com.ashish.vtopchennai.models.Attachment;
import com.ashish.vtopchennai.models.Attendance;
import com.ashish.vtopchennai.models.Course;
import com.ashish.vtopchennai.models.CumulativeMark;
import com.ashish.vtopchennai.models.Mark;
import com.ashish.vtopchennai.models.Receipt;
import com.ashish.vtopchennai.models.Slot;
import com.ashish.vtopchennai.models.Spotlight;
import com.ashish.vtopchennai.models.Staff;
import com.ashish.vtopchennai.models.Timetable;

@Database(
        entities = {
                Assignment.class,
                Attachment.class,
                Attendance.class,
                Course.class,
                CumulativeMark.class,
                Mark.class,
                Receipt.class,
                Slot.class,
                Spotlight.class,
                Staff.class,
                Timetable.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "vit_student")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public static synchronized void deleteDatabase(Context context) {
        if (instance != null) {
            instance.close();
        }

        instance = null;
        context.deleteDatabase("vit_student");
    }

    public abstract AssignmentsDao assignmentsDao();

    public abstract AttendanceDao attendanceDao();

    public abstract CoursesDao coursesDao();

    public abstract MarksDao marksDao();

    public abstract ReceiptsDao receiptsDao();

    public abstract SpotlightDao spotlightDao();

    public abstract StaffDao staffDao();

    public abstract TimetableDao timetableDao();
}
