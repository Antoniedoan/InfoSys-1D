package profchoper.calendar;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import profchoper.booking.BookingSlot;
import profchoper.booking.BookingSlotService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static profchoper._config.Constant.*;

@Service
public class WeekCalendarServiceImpl implements WeekCalendarService {
    @Autowired
    private BookingSlotService slotService;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public WeekCalendar getStudentCalendarByCourse(int studentId, String courseId,
                                                   LocalDate startDateOfSchoolTerm,
                                                   LocalDate startDateOfSchoolWeek) {

        List<List<String>> matrix
                = createStudentCourseCalMatrix(courseId, studentId, startDateOfSchoolWeek);

        return new WeekCalendar(startDateOfSchoolTerm, startDateOfSchoolWeek, matrix);
    }

    @Override
    public WeekCalendar getStudentCalendarByProf(int studentId, String profAlias,
                                                 LocalDate startDateOfSchoolTerm,
                                                 LocalDate startDateOfSchoolWeek) {

        List<List<String>> matrix
                = createStudentProfCalMatrix(profAlias, startDateOfSchoolWeek, studentId);

        return new WeekCalendar(startDateOfSchoolTerm, startDateOfSchoolWeek, matrix);
    }

    @Override
    public WeekCalendar getProfCalendar(String profAlias, LocalDate startDateOfSchoolTerm,
                                        LocalDate startDateOfSchoolWeek) {

        List<List<String>> matrix
                = createProfCalMatrix(profAlias, startDateOfSchoolWeek);

        return new WeekCalendar(startDateOfSchoolTerm, startDateOfSchoolWeek, matrix);
    }


    private List<List<String>> createStudentCourseCalMatrix(String courseId, int studentId,
                                                            LocalDate startDateOfSchoolWeek) {
        List<BookingSlot> slotList = slotService.getSlotsByCourseAndSWeek(courseId, startDateOfSchoolWeek);
        List<List<String>> output = new ArrayList<>();
        List<String> temp;

        for (int i = 0; i < WEEK_CAL_ROW; i++) {
            LocalTime timePart = ROW_TO_TIME.get(i);
            String timeRange = timeRangeFormat(timePart);

            temp = new ArrayList<>();
            temp.add(timeRange);

            for (int j = 0; j < WEEK_CAL_COL; j++) {
                LocalDate datePart = startDateOfSchoolWeek.plus(j, ChronoUnit.DAYS);
                LocalDateTime dateTime = LocalDateTime.of(datePart, timePart);

                temp.add(createStudentCourseCalString(dateTime, slotList, studentId));
            }

            output.add(temp);
        }

        return output;
    }

    private String createStudentCourseCalString(LocalDateTime dateTime, List<BookingSlot> slotList,
                                                int studentId) {
        StringBuilder outputBld = new StringBuilder();

        for (BookingSlot slot : slotList) {
            if (slot.getDateTime().equals(dateTime)) {
                if (slot.getBookStatus().equalsIgnoreCase(BOOKED) &&
                        slot.getStudentId() == studentId) {
                    outputBld = new StringBuilder();
                    String appendedStr = "BOOKED\n(" + slot.getProfAlias().toUpperCase() + ")  ";
                    outputBld.append(appendedStr);

                } else if (slot.getBookStatus().equalsIgnoreCase(PENDING)
                        && slot.getStudentId() == studentId) {
                    outputBld = new StringBuilder();
                    String appendedStr = "PENDING\n(" + slot.getProfAlias().toUpperCase() + ")  ";
                    outputBld.append(appendedStr);
                } else {
                    outputBld.append(slot.getProfAlias().toUpperCase());
                    outputBld.append(", ");
                }
            }
        }

        String output = outputBld.toString();
        int len = output.length();

        if (len == 0) return "";
        return output.substring(0, len - 2);
    }


    private List<List<String>> createStudentProfCalMatrix(String profAlias, LocalDate startDateOfSchoolWeek,
                                                          int studentId) {

        List<BookingSlot> slotList = slotService.getSlotsByProfAndSWeek(profAlias, startDateOfSchoolWeek);
        List<List<String>> output = new ArrayList<>();
        List<String> temp;

        for (int i = 0; i < WEEK_CAL_ROW; i++) {
            LocalTime timePart = ROW_TO_TIME.get(i);
            String timeRange = timeRangeFormat(timePart);

            temp = new ArrayList<>();
            temp.add(timeRange);

            for (int j = 0; j < WEEK_CAL_COL; j++) {
                LocalDate datePart = startDateOfSchoolWeek.plus(j, ChronoUnit.DAYS);
                LocalDateTime dateTime = LocalDateTime.of(datePart, timePart);

                temp.add(createStudentProfCalString(dateTime, slotList, studentId));
            }

            output.add(temp);
        }

        return output;
    }

    private String createStudentProfCalString(LocalDateTime dateTime, List<BookingSlot> slotList,
                                              int studentId) {

        StringBuilder outputBld = new StringBuilder();

        for (BookingSlot slot : slotList) {
            if (slot.getDateTime().equals(dateTime)) {
                String appendedStr = "";
                if (slot.getBookStatus().equalsIgnoreCase(AVAIL))
                    appendedStr = slot.getProfAlias().toUpperCase();
                else if (slot.getBookStatus().equalsIgnoreCase(BOOKED) && slot.getStudentId() == studentId)
                    appendedStr = "BOOKED\n(" + slot.getProfAlias().toUpperCase() + ")";
                else if (slot.getBookStatus().equalsIgnoreCase(PENDING) && slot.getStudentId() == studentId)
                    appendedStr = "PENDING\n(" + slot.getProfAlias().toUpperCase() + ")";

                outputBld.append(appendedStr);
            }
        }

        return outputBld.toString();
    }

    private List<List<String>> createProfCalMatrix(String profAlias, LocalDate startDateOfSchoolWeek) {
        List<BookingSlot> slotList = slotService.getSlotsByProfAndSWeek(profAlias, startDateOfSchoolWeek);
        List<List<String>> output = new ArrayList<>();
        List<String> temp;

        for (int i = 0; i < WEEK_CAL_ROW; i++) {
            LocalTime timePart = ROW_TO_TIME.get(i);
            String timeRange = timeRangeFormat(timePart);

            temp = new ArrayList<>();
            temp.add(timeRange);

            for (int j = 0; j < WEEK_CAL_COL; j++) {
                LocalDate datePart = startDateOfSchoolWeek.plus(j, ChronoUnit.DAYS);
                LocalDateTime dateTime = LocalDateTime.of(datePart, timePart);

                temp.add(createProfCalString(dateTime, slotList));
            }

            output.add(temp);
        }

        return output;
    }


    private String createProfCalString(LocalDateTime dateTime, List<BookingSlot> slotList) {
        StringBuilder outputBld = new StringBuilder();

        for (BookingSlot slot : slotList) {
            if (slot.getDateTime().equals(dateTime)) {
                String appendedStr = slot.getBookStatus().toUpperCase();
                outputBld.append(appendedStr);
            }
        }

        return outputBld.toString();
    }

    private String timeRangeFormat(LocalTime timePart) {
        LocalTime timePart2 = timePart.plus(30, ChronoUnit.MINUTES);

        return timePart.format(dtf) + " - " + timePart2.format(dtf);
    }

}
