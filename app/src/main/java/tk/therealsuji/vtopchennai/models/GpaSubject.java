package tk.therealsuji.vtopchennai.models;

public class GpaSubject {
    private final String subjectName;
    private final String grade;
    private final int credits;
    private final double gradePoints;

    public GpaSubject(String subjectName, String grade, int credits, double gradePoints) {
        this.subjectName = subjectName;
        this.grade = grade;
        this.credits = credits;
        this.gradePoints = gradePoints;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getGrade() {
        return grade;
    }

    public int getCredits() {
        return credits;
    }

    public double getGradePoints() {
        return gradePoints;
    }
}

