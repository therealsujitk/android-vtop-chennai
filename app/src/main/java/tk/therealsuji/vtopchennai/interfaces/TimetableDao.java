package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.TimetableLab;
import tk.therealsuji.vtopchennai.models.TimetableTheory;

@Dao
public interface TimetableDao {
    default Single<List<TimetableLab>> getLabTimetable(int day) {
        switch (day) {
            case 1:
                return getMonLabTimetable();
            case 2:
                return getTueLabTimetable();
            case 3:
                return getWedLabTimetable();
            case 4:
                return getThuLabTimetable();
            case 5:
                return getFriLabTimetable();
            case 6:
                return getSatLabTimetable();
            default:
                return getSunLabTimetable();
        }
    }

    default Single<List<TimetableTheory>> getTheoryTimetable(int day) {
        switch (day) {
            case 1:
                return getMonTheoryTimetable();
            case 2:
                return getTueTheoryTimetable();
            case 3:
                return getWedTheoryTimetable();
            case 4:
                return getThuTheoryTimetable();
            case 5:
                return getFriTheoryTimetable();
            case 6:
                return getSatTheoryTimetable();
            default:
                return getSunTheoryTimetable();
        }
    }

    @Query("SELECT id, start_time, end_time, sun FROM timetable_lab")
    Single<List<TimetableLab>> getSunLabTimetable();

    @Query("SELECT id, start_time, end_time, sun FROM timetable_theory")
    Single<List<TimetableTheory>> getSunTheoryTimetable();

    @Query("SELECT id, start_time, end_time, mon FROM timetable_lab")
    Single<List<TimetableLab>> getMonLabTimetable();

    @Query("SELECT id, start_time, end_time, mon FROM timetable_theory")
    Single<List<TimetableTheory>> getMonTheoryTimetable();

    @Query("SELECT id, start_time, end_time, tue FROM timetable_lab")
    Single<List<TimetableLab>> getTueLabTimetable();

    @Query("SELECT id, start_time, end_time, tue FROM timetable_theory")
    Single<List<TimetableTheory>> getTueTheoryTimetable();

    @Query("SELECT id, start_time, end_time, wed FROM timetable_lab")
    Single<List<TimetableLab>> getWedLabTimetable();

    @Query("SELECT id, start_time, end_time, wed FROM timetable_theory")
    Single<List<TimetableTheory>> getWedTheoryTimetable();

    @Query("SELECT id, start_time, end_time, thu FROM timetable_lab")
    Single<List<TimetableLab>> getThuLabTimetable();

    @Query("SELECT id, start_time, end_time, thu FROM timetable_theory")
    Single<List<TimetableTheory>> getThuTheoryTimetable();

    @Query("SELECT id, start_time, end_time, fri FROM timetable_lab")
    Single<List<TimetableLab>> getFriLabTimetable();

    @Query("SELECT id, start_time, end_time, fri FROM timetable_theory")
    Single<List<TimetableTheory>> getFriTheoryTimetable();

    @Query("SELECT id, start_time, end_time, sat FROM timetable_lab")
    Single<List<TimetableLab>> getSatLabTimetable();

    @Query("SELECT id, start_time, end_time, sat FROM timetable_theory")
    Single<List<TimetableTheory>> getSatTheoryTimetable();
}
