package tk.therealsuji.vtopchennai.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "cumulative_marks")
public class CumulativeMark {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "course_code")
    public String courseCode;

    @ColumnInfo(name = "theory_total")
    public Double theoryTotal;

    @ColumnInfo(name = "lab_total")
    public Double labTotal;

    @ColumnInfo(name = "project_total")
    public Double projectTotal;

    @ColumnInfo(name = "grand_total")
    public Double grandTotal;

    @ColumnInfo(name = "grade")
    public String grade;

    @Ignore
    public int theoryCredits, labCredits, projectCredits;

    public CumulativeMark() {
        this.theoryCredits = 0;
        this.labCredits = 0;
        this.projectCredits = 0;
    }

    public void addWeightage(Double weightageScore, int courseType, Integer credits) {
        if (weightageScore == null || credits == null) {
            return;
        }

        if (courseType == Course.TYPE_THEORY) {
            if (this.theoryTotal == null) {
                this.theoryTotal = (double) 0;
            }

            this.theoryTotal += weightageScore;
            this.theoryCredits = credits;
        } else if (courseType == Course.TYPE_LAB) {
            if (this.labTotal == null) {
                this.labTotal = (double) 0;
            }

            this.labTotal += weightageScore;
            this.labCredits = credits;
        } else if (courseType == Course.TYPE_PROJECT) {
            if (this.projectTotal == null) {
                this.projectTotal = (double) 0;
            }

            this.projectTotal += weightageScore;
            this.projectCredits = credits;
        }
    }
}
