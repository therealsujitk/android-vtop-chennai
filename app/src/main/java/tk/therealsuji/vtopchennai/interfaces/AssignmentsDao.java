package tk.therealsuji.vtopchennai.interfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.MapInfo;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import tk.therealsuji.vtopchennai.models.Assignment;
import tk.therealsuji.vtopchennai.models.Attachment;

@Dao
public interface AssignmentsDao {
    default Completable insert(List<Assignment> assignments) {
        List<Attachment> attachments = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (assignment.introAttachments == null) {
                continue;
            }

            attachments.addAll(assignment.introAttachments);
        }

        return this.delete()
                .andThen(insertAssignments(assignments))
                .andThen(insertAttachments(attachments));
    }

    @Insert
    Completable insertAssignments(List<Assignment> assignments);

    @Insert
    Completable insertAttachments(List<Attachment> attachments);

    @Query("DELETE FROM assignments")
    Completable delete();

    default Single<List<Assignment>> get() {
        return this.getAttachments()
                .concatMap(attachments -> this.getAssignments().map(assignments -> {
                    for (int i = 0; i < assignments.size(); ++i) {
                        int assignmentId = assignments.get(i).id;
                        if (attachments.containsKey(assignmentId)) {
                            assignments.get(i).introAttachments = attachments.get(assignmentId);
                        }
                    }

                    return assignments;
                }));
    }

    @Query("SELECT * FROM assignments")
    Single<List<Assignment>> getAssignments();

    @Query("SELECT * FROM attachments")
    @MapInfo(keyColumn = "assignment_id")
    Single<Map<Integer, List<Attachment>>> getAttachments();
}
