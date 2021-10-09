package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Course;
import tk.therealsuji.vtopchennai.models.CumulativeMark;
import tk.therealsuji.vtopchennai.models.Mark;

@Dao
public interface MarksDao {
    @Insert
    Completable insertMarks(List<Mark> marks);

    @Insert
    Completable insertCumulativeMarks(List<CumulativeMark> cumulativeMarks);

    @Query("DELETE FROM cumulative_marks")
    Completable deleteAllCumulativeMarks();

    @Query("SELECT courses.id AS id, courses.code AS code, courses.title AS title " +
            "FROM marks, courses " +
            "WHERE course_id = courses.id " +
            "GROUP BY courses.code")
    Single<List<Course>> getCourses();

    @Query("SELECT type AS courseType, marks.title AS title, score, max_score AS maxScore, weightage, max_weightage AS maxWeightage, average, status " +
            "FROM marks, courses " +
            "WHERE course_id = courses.id AND code = :courseCode " +
            "ORDER BY type DESC, title")
    Single<List<Mark.AllData>> getMarks(String courseCode);
}
