package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Timetable;

@Dao
public interface TimetableDao {
    default Single<List<Timetable.AllData>> get(int day) {
        switch (day) {
            case 1:
                return getMonday();
            case 2:
                return getTuesday();
            case 3:
                return getWednesday();
            case 4:
                return getThursday();
            case 5:
                return getFriday();
            case 6:
                return getSaturday();
            default:
                return getSunday();
        }
    }

    @Insert
    Completable insert(List<Timetable> timetable);

    @Query("DELETE FROM timetable")
    Completable deleteAll();

    @Query("SELECT courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode " +
            "FROM timetable, slots, courses WHERE sunday = slots.id AND course_id = courses.id")
    Single<List<Timetable.AllData>> getSunday();

    @Query("SELECT courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode " +
            "FROM timetable, slots, courses WHERE monday = slots.id AND course_id = courses.id")
    Single<List<Timetable.AllData>> getMonday();

    @Query("SELECT courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode " +
            "FROM timetable, slots, courses WHERE tuesday = slots.id AND course_id = courses.id")
    Single<List<Timetable.AllData>> getTuesday();

    @Query("SELECT courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode " +
            "FROM timetable, slots, courses WHERE wednesday = slots.id AND course_id = courses.id")
    Single<List<Timetable.AllData>> getWednesday();

    @Query("SELECT courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode " +
            "FROM timetable, slots, courses WHERE thursday = slots.id AND course_id = courses.id")
    Single<List<Timetable.AllData>> getThursday();

    @Query("SELECT courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode " +
            "FROM timetable, slots, courses WHERE friday = slots.id AND course_id = courses.id")
    Single<List<Timetable.AllData>> getFriday();

    @Query("SELECT courses.type AS courseType, start_time AS startTime, end_time AS endTime, code AS courseCode " +
            "FROM timetable, slots, courses WHERE saturday = slots.id AND course_id = courses.id")
    Single<List<Timetable.AllData>> getSaturday();
}
