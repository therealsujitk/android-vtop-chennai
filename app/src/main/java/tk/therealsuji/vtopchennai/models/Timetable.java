package tk.therealsuji.vtopchennai.models;

import java.util.ArrayList;
import java.util.List;

import tk.therealsuji.vtopchennai.widgets.TimetableItem;

public class Timetable {
    public String courseCode, endTime, startTime;
    public String[] rawCourse;
    public int courseType;

    /**
     * Builds a list of Timetable objects containing the timings, course & course type.
     * This function ignores null courses.
     *
     * @param timetableLab    The lab timetable
     * @param timetableTheory The theory timetable
     * @param day             The timetable's day
     * @return Returns a list of Timetable objects
     */
    public static List<Timetable> buildTimetable(List<TimetableLab> timetableLab, List<TimetableTheory> timetableTheory, int day) {
        List<Timetable> timetable = new ArrayList<>();

        for (int i = 0; i < timetableLab.size() && i < timetableTheory.size(); ++i) {
            Timetable timetableLabItem = Timetable.fromTimetableLabItem(timetableLab.get(i), day);
            Timetable timetableTheoryItem = Timetable.fromTimetableTheoryItem(timetableTheory.get(i), day);

            if (timetableLabItem.courseCode != null) {
                timetable.add(timetableLabItem);
                timetableLabItem.courseType = TimetableItem.CLASS_LAB;
            }

            if (timetableTheoryItem.courseCode != null) {
                timetable.add(timetableTheoryItem);
                timetableLabItem.courseType = TimetableItem.CLASS_THEORY;
            }
        }

        return timetable;
    }

    private static Timetable fromTimetableLabItem(TimetableLab timetableLabItem, int day) {
        Timetable timetableItem = new Timetable();
        String rawCourseString;

        timetableItem.startTime = timetableLabItem.startTime;
        timetableItem.endTime = timetableLabItem.endTime;

        switch (day) {
            case 1:
                rawCourseString = timetableLabItem.monday;
                break;
            case 2:
                rawCourseString = timetableLabItem.tuesday;
                break;
            case 3:
                rawCourseString = timetableLabItem.wednesday;
                break;
            case 4:
                rawCourseString = timetableLabItem.thursday;
                break;
            case 5:
                rawCourseString = timetableLabItem.friday;
                break;
            case 6:
                rawCourseString = timetableLabItem.saturday;
                break;
            default:
                rawCourseString = timetableLabItem.sunday;
        }

        timetableItem.rawCourse = rawCourseString.split("-");

        if (timetableItem.rawCourse.length > 1) {
            timetableItem.courseCode = timetableItem.rawCourse[1];
        }

        return timetableItem;
    }

    private static Timetable fromTimetableTheoryItem(TimetableTheory timetableItemTheoryItem, int day) {
        Timetable timetableItem = new Timetable();
        String rawCourseString;

        timetableItem.startTime = timetableItemTheoryItem.startTime;
        timetableItem.endTime = timetableItemTheoryItem.endTime;

        switch (day) {
            case 1:
                rawCourseString = timetableItemTheoryItem.monday;
                break;
            case 2:
                rawCourseString = timetableItemTheoryItem.tuesday;
                break;
            case 3:
                rawCourseString = timetableItemTheoryItem.wednesday;
                break;
            case 4:
                rawCourseString = timetableItemTheoryItem.thursday;
                break;
            case 5:
                rawCourseString = timetableItemTheoryItem.friday;
                break;
            case 6:
                rawCourseString = timetableItemTheoryItem.saturday;
                break;
            default:
                rawCourseString = timetableItemTheoryItem.sunday;
        }

        if (rawCourseString == null) {
            return timetableItem;
        }

        timetableItem.rawCourse = rawCourseString.split("-");

        if (timetableItem.rawCourse.length > 1) {
            timetableItem.courseCode = timetableItem.rawCourse[1];
        }

        return timetableItem;
    }
}
