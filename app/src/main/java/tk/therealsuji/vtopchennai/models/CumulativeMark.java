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

    @ColumnInfo(name = "theory_max")
    public Double theoryMax;

    @ColumnInfo(name = "lab_total")
    public Double labTotal;

    @ColumnInfo(name = "lab_max")
    public Double labMax;

    @ColumnInfo(name = "project_total")
    public Double projectTotal;

    @ColumnInfo(name = "project_max")
    public Double projectMax;

    @ColumnInfo(name = "grand_total")
    public Double grandTotal;

    @ColumnInfo(name = "grand_max")
    public Double grandMax;

    @ColumnInfo(name = "grade")
    public String grade;

    @Ignore
    public int theoryCredits, labCredits, projectCredits;

    public CumulativeMark(int id) {
        this.id = id;
        this.theoryCredits = 0;
        this.labCredits = 0;
        this.projectCredits = 0;
    }

    public void addWeightage(Double weightageScore, Double maxWeightage, int courseType, Integer credits) {
        if (weightageScore == null || credits == null) {
            return;
        }

        if (courseType == Course.TYPE_THEORY) {
            if (this.theoryTotal == null) {
                this.theoryTotal = (double) 0;
                this.theoryMax = (double) 0;
            }

            this.theoryTotal += weightageScore;
            this.theoryMax += maxWeightage;
            this.theoryCredits = credits;
        } else if (courseType == Course.TYPE_LAB) {
            if (this.labTotal == null) {
                this.labTotal = (double) 0;
                this.labMax = (double) 0;
            }

            this.labTotal += weightageScore;
            this.labMax += maxWeightage;
            this.labCredits = credits;
        } else if (courseType == Course.TYPE_PROJECT) {
            if (this.projectTotal == null) {
                this.projectTotal = (double) 0;
                this.projectMax = (double) 0;
            }

            this.projectTotal += weightageScore;
            this.projectMax += maxWeightage;
            this.projectCredits = credits;
        }
    }
}
