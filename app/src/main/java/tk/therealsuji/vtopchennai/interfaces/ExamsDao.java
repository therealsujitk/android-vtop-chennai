package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Exam;

@Dao
public interface ExamsDao {
    @Insert
    Completable insert(List<Exam> exams);

    @Query("DELETE FROM exams")
    Completable deleteAll();

    @Query("SELECT * FROM exams")
    Single<List<Exam>> getExams();

    @Query("SELECT code AS courseCode, slot, courses.title AS courseTitle, start_time AS startTime, end_time AS endTime, exams.venue AS venue, seat_location AS seatLocation, seat_number AS seatNumber " +
            "FROM courses, slots, exams WHERE exams.course_id = courses.id AND slots.course_id = courses.id AND exams.title = :examTitle ORDER BY start_time")
    Single<List<Exam.AllData>> getExams(String examTitle);

    @Query("SELECT code AS courseCode, courses.title AS courseTitle, start_time AS startTime, end_time AS endTime " +
            "FROM courses, exams WHERE exams.course_id = courses.id AND exams.start_time >= :currentTime AND exams.start_time < :futureTime")
    Single<Exam.AllData> getExam(long currentTime, long futureTime);

    @Query("SELECT DISTINCT title FROM exams ORDER BY start_time DESC")
    Single<List<String>> getExamTitles();
}
