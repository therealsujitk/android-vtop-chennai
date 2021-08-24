package tk.therealsuji.vtopchennai.models;

import java.util.ArrayList;
import java.util.List;

import tk.therealsuji.vtopchennai.widgets.TimetableItem;

public class Timetable {
    public String endTime, rawCourse, startTime;
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

            if (timetableLabItem.rawCourse != null && !timetableLabItem.rawCourse.equals("null")) {
                timetable.add(timetableLabItem);
                timetableLabItem.courseType = TimetableItem.LAB;
            }

            if (timetableTheoryItem.rawCourse != null && !timetableTheoryItem.rawCourse.equals("null")) {
                timetable.add(timetableTheoryItem);
                timetableLabItem.courseType = TimetableItem.THEORY;
            }
        }

        return timetable;
    }

    private static Timetable fromTimetableLabItem(TimetableLab timetableLabItem, int day) {
        Timetable timetableItem = new Timetable();

        timetableItem.startTime = timetableLabItem.startTime;
        timetableItem.endTime = timetableLabItem.endTime;

        switch (day) {
            case 1:
                timetableItem.rawCourse = timetableLabItem.monday;
                break;
            case 2:
                timetableItem.rawCourse = timetableLabItem.tuesday;
                break;
            case 3:
                timetableItem.rawCourse = timetableLabItem.wednesday;
                break;
            case 4:
                timetableItem.rawCourse = timetableLabItem.thursday;
                break;
            case 5:
                timetableItem.rawCourse = timetableLabItem.friday;
                break;
            case 6:
                timetableItem.rawCourse = timetableLabItem.saturday;
                break;
            default:
                timetableItem.rawCourse = timetableLabItem.sunday;
        }

        return timetableItem;
    }

    private static Timetable fromTimetableTheoryItem(TimetableTheory timetableItemTheoryItem, int day) {
        Timetable timetableItem = new Timetable();

        timetableItem.startTime = timetableItemTheoryItem.startTime;
        timetableItem.endTime = timetableItemTheoryItem.endTime;

        switch (day) {
            case 1:
                timetableItem.rawCourse = timetableItemTheoryItem.monday;
                break;
            case 2:
                timetableItem.rawCourse = timetableItemTheoryItem.tuesday;
                break;
            case 3:
                timetableItem.rawCourse = timetableItemTheoryItem.wednesday;
                break;
            case 4:
                timetableItem.rawCourse = timetableItemTheoryItem.thursday;
                break;
            case 5:
                timetableItem.rawCourse = timetableItemTheoryItem.friday;
                break;
            case 6:
                timetableItem.rawCourse = timetableItemTheoryItem.saturday;
                break;
            default:
                timetableItem.rawCourse = timetableItemTheoryItem.sunday;
        }

        return timetableItem;
    }
}
