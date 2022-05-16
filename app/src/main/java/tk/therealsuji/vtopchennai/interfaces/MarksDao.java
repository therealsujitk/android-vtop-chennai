package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Course;
import tk.therealsuji.vtopchennai.models.CumulativeMark;
import tk.therealsuji.vtopchennai.models.Mark;

@Dao
public interface MarksDao {
    default Completable insertMarks(Map<Integer, Mark> marks) {

        return this.getMarksRead()
                .concatMapCompletable(readMarks -> {
                    for (Mark markItem : readMarks) {
                        if (marks.containsKey(markItem.signature)) {
                            Objects.requireNonNull(marks.get(markItem.signature)).isRead = true;
                        }
                    }

                    return this.deleteMarks().andThen(this.insertMarks(new ArrayList<>(marks.values())));
                });
    }

    @Insert
    Completable insertMarks(List<Mark> marks);

    @Insert
    Completable insertCumulativeMarks(List<CumulativeMark> cumulativeMarks);

    @Query("UPDATE marks SET is_read = 1 WHERE course_id IN (SELECT id FROM courses WHERE code = :courseCode) AND is_read IS 0")
    Completable setMarksRead(String courseCode);

    @Query("DELETE FROM marks")
    Completable deleteMarks();

    @Query("DELETE FROM cumulative_marks")
    Completable deleteCumulativeMarks();

    @Query("SELECT courses.code AS courseCode, courses.title AS courseTitle, COUNT(CASE WHEN is_read IS 0 THEN 1 END) AS unreadMarkCount " +
            "FROM marks, courses " +
            "WHERE course_id = courses.id " +
            "GROUP BY courses.code")
    Single<List<Course.AllData>> getCourses();

    @Query("SELECT id, is_read, signature FROM marks WHERE is_read IS 1")
    Single<List<Mark>> getMarksRead();

    @Query("SELECT COUNT(id) FROM marks WHERE is_read IS 0")
    Single<Integer> getMarksUnreadCount();

    @Query("SELECT type AS courseType, marks.title AS title, score, max_score AS maxScore, weightage, max_weightage AS maxWeightage, average, status, is_read AS isRead " +
            "FROM marks, courses " +
            "WHERE course_id = courses.id AND code = :courseCode " +
            "ORDER BY type DESC, title")
    Single<List<Mark.AllData>> getMarks(String courseCode);

    @Query("SELECT * FROM cumulative_marks WHERE course_code = :courseCode")
    Single<CumulativeMark> getCumulativeMark(String courseCode);
}
