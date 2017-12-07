package profchoper.slot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static profchoper._misc.Constant.*;

@Service
public class SlotService {
    @Autowired
    private SlotDAO slotDAO;

    public List<Slot> getAllSlots() {
        return slotDAO.findAll();
    }

    public List<Slot> getSlotsByProfAlias(String profAlias) {
        return slotDAO.findByProfAlias(profAlias);
    }

    public List<Slot> getSlotsByDateTime(LocalDateTime dateTime) {
        Timestamp timestamp = Timestamp.valueOf(dateTime);
        return slotDAO.findByDateTime(timestamp);
    }

    public List<Slot> getSlotsByDate(LocalDate date) {
        return getSlotsByDateRangeType(DATE, date);
    }

    public List<Slot> getSlotsBySchoolWeek(LocalDate startDateOfSchoolWeek) {
        return getSlotsByDateRangeType(SCHOOL_WEEK, startDateOfSchoolWeek);
    }

    public List<Slot> getSlotsByWeek(LocalDate startDateOfWeek) {
        return getSlotsByDateRangeType(WEEK, startDateOfWeek);
    }

    public List<Slot> getSlotsByMonth(LocalDate startDateOfMonth) {
        return getSlotsByDateRangeType(MONTH, startDateOfMonth);
    }

    public List<Slot> getSlotsByTerm(LocalDate startDateOfTerm) {
        return getSlotsByDateRangeType(TERM, startDateOfTerm);
    }

    public boolean bookSlot(Slot slot, int studentID) {
        if (!slot.getBookStatus().equals(AVAIL)) return false;

        slot.setBookStatus(PENDING);
        slot.setStudentId(studentID);

        return slotDAO.update(slot);
    }

    public boolean cancelBookSlot(Slot slot, int studentID) {
        if (slot.getBookStatus().equals(AVAIL)
                || slot.getStudentId() != studentID) return false;

        slot.setBookStatus(AVAIL);
        slot.setStudentId(null);

        return slotDAO.update(slot);
    }

    public boolean confirmBookSlot(Slot slot, String profAlias) {
        if (slot.getBookStatus().equals(AVAIL)
                || !slot.getProfAlias().equals(profAlias)) return false;

        slot.setBookStatus(BOOKED);

        return slotDAO.update(slot);
    }

    public boolean deleteSlot(Slot slot, String profAlias) {
        if (!slot.getBookStatus().equals(AVAIL)
                || !slot.getProfAlias().equals(profAlias)) return false;

        return slotDAO.delete(slot);
    }

    private List<Slot> getSlotsByDateRangeType(String type, LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime;

        switch (type) {
            case DATE:
                endDateTime = startDateTime.plus(1, ChronoUnit.DAYS);
                break;

            case SCHOOL_WEEK:
                endDateTime = startDateTime.plus(5, ChronoUnit.DAYS);
                break;

            case WEEK:
                endDateTime = startDateTime.plus(1, ChronoUnit.WEEKS);
                break;

            case MONTH:
                endDateTime = startDateTime.plus(1, ChronoUnit.MONTHS);
                break;

            case TERM:
                endDateTime = startDateTime.plus(14, ChronoUnit.MONTHS);
                break;

            default:
                endDateTime = startDateTime.plus(SLOT_TIME, ChronoUnit.MINUTES);
                break;
        }

        Timestamp startTimestamp = Timestamp.valueOf(startDateTime);
        Timestamp endTimestamp = Timestamp.valueOf(endDateTime);
        return slotDAO.findByDateTimeRange(startTimestamp, endTimestamp);
    }
}
