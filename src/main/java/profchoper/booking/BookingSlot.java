package profchoper.booking;


import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static profchoper._config.Constant.*;

public class BookingSlot {
    private Timestamp timestamp;
    private String profAlias;
    private Integer studentId = null;
    private String bookStatus = AVAIL;

    public BookingSlot() {
        // default constructor
    }

    public BookingSlot(String profAlias, Timestamp startTimestamp) {
        this.profAlias = profAlias;
        this.timestamp = startTimestamp;
    }

    public LocalDateTime getDateTime() {
        return timestamp.toLocalDateTime();
    }

    public DayOfWeek getDayOfWeek() {
        return timestamp.toLocalDateTime().getDayOfWeek();
    }

    public LocalDate getDate() {
        return timestamp.toLocalDateTime().toLocalDate();
    }

    public LocalTime getStartTime() {
        return timestamp.toLocalDateTime().toLocalTime();
    }

    public LocalTime getEndTime() {
        return getStartTime().plus(30, ChronoUnit.MINUTES);
    }

    @Override
    public String toString() {
        return getDayWeekYear(getDate()) + " : " + getStartTime() + " to " + getEndTime()
                + " with Prof. " + getProfAlias().toUpperCase();
    }

    private String getDayWeekYear(LocalDate data) {
        String dataString = data.toString();
        String[] dataArr = dataString.split("-");
        return dataArr[2] + "/" + dataArr[1] + "/" + dataArr[0];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass())
            return false;

        BookingSlot comparedSlot = (BookingSlot) obj;

        if (!comparedSlot.timestamp.equals(this.timestamp))
            return false;

        if (!comparedSlot.profAlias.equalsIgnoreCase(this.profAlias))
            return false;

        return true;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public void setBookStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public String getProfAlias() {
        return profAlias;
    }

    public String getBookStatus() {
        return bookStatus;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setProfAlias(String profAlias) {
        this.profAlias = profAlias;
    }
}
