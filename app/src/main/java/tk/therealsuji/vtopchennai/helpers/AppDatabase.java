package tk.therealsuji.vtopchennai.helpers;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.TimetableLab;
import tk.therealsuji.vtopchennai.models.TimetableTheory;

@Database(
        entities = {
                TimetableTheory.class,
                TimetableLab.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "vtop")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public abstract TimetableDao timetableDao();
}
