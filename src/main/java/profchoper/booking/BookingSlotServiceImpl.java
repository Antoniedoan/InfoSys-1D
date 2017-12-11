package profchoper.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import profchoper.professor.Professor;
import profchoper.professor.ProfessorService;
import profchoper.student.Student;
import profchoper.student.StudentService;
import profchoper.user.User;
import profchoper.user.UserService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static profchoper._config.Constant.*;

@Service
public class BookingSlotServiceImpl implements BookingSlotService {
    private final BookingSlotRepository slotRepository;
    private final ProfessorService professorService;
    private final StudentService studentService;
    private final UserService userService;

    @Autowired
    public BookingSlotServiceImpl(BookingSlotRepository slotRepository, ProfessorService professorService,
                                  StudentService studentService, UserService userService) {
        this.slotRepository = slotRepository;
        this.professorService = professorService;
        this.studentService = studentService;
        this.userService = userService;
    }

    @Override
    public List<BookingSlot> getAllSlots() {
        return slotRepository.findAll();
    }

    @Override
    public BookingSlot bookSlot(BookingSlot slot, String studentEmail) {
        User user = userService.getUserByUsername(studentEmail);
        if (!user.getRole().equalsIgnoreCase(ROLE_STUDENT)) return null;

        Student student = studentService.getStudentByEmail(studentEmail);
        if (!slot.getBookStatus().equalsIgnoreCase(AVAIL)) return null;

        slot.setBookStatus(PENDING);
        slot.setStudentId(student.getId());

        if (!slotRepository.update(slot)) return null;
        return slot;
    }

    @Override
    public BookingSlot cancelBookSlot(BookingSlot slot, String studentEmail) {
        User user = userService.getUserByUsername(studentEmail);
        if (!user.getRole().equalsIgnoreCase(ROLE_STUDENT)) return null;

        Student student = studentService.getStudentByEmail(studentEmail);
        if (slot.getBookStatus().equalsIgnoreCase(AVAIL)) return null;
        if (slot.getStudentId() != student.getId()) return null;

        slot.setBookStatus(CANCELLED);

        if (!slotRepository.update(slot)) return null;
        return slot;
    }

    @Override
    public BookingSlot respondBookSlot(BookingSlot slot, String profEmail, boolean accept) {
        User user = userService.getUserByUsername(profEmail);
        if (!user.getRole().equalsIgnoreCase(ROLE_PROF)) return null;

        Professor prof = professorService.getProfessorByEmail(profEmail);
        if (slot.getBookStatus().equalsIgnoreCase(AVAIL)) return null;
        if (!slot.getProfAlias().equalsIgnoreCase(prof.getAlias())) return null;

        if (accept) slot.setBookStatus(BOOKED);
        else slot.setBookStatus(REJECTED);

        if (!slotRepository.update(slot)) return null;
        return slot;
    }

    @Override
    public BookingSlot addSlot(BookingSlot slot, String profEmail) {
        User user = userService.getUserByUsername(profEmail);
        if (!user.getRole().equalsIgnoreCase(ROLE_PROF)) return null;

        if (!slotRepository.create(slot)) return null;
        return slot;
    }

    @Override
    public BookingSlot deleteSlot(BookingSlot slot, String profEmail) {
        User user = userService.getUserByUsername(profEmail);
        if (!user.getRole().equalsIgnoreCase(ROLE_PROF)) return null;

        Professor prof = professorService.getProfessorByEmail(profEmail);
        if (!slot.getBookStatus().equalsIgnoreCase(AVAIL)) return null;
        if (!slot.getProfAlias().equalsIgnoreCase(prof.getAlias())) return null;

        if (!slotRepository.delete(slot)) return null;
        return slot;
    }

    @Override
    public List<BookingSlot> getSlotsByProfAlias(String profAlias) {
        return slotRepository.findByProfAlias(profAlias.toLowerCase());
    }


    @Override
    public List<BookingSlot> getSlotsByStudentId(int studentId) {
        return slotRepository.findByStudentId(studentId);
    }

    @Override
    public List<BookingSlot> getSlotsByCourseId(String courseId) {
        List<Professor> professors = professorService.getProfessorsByCourseId(courseId);
        List<BookingSlot> output = new ArrayList<>();

        for (Professor prof : professors) {
            output.addAll(getSlotsByProfAlias(prof.getAlias().toLowerCase()));
        }

        return output;
    }

    @Override
    public List<BookingSlot> getSlotsByProfAndSWeek(String profAlias, LocalDate startDateOfSchoolWeek) {
        List<BookingSlot> slotList = getSlotsBySchoolWeek(startDateOfSchoolWeek);
        List<BookingSlot> output = new ArrayList<>();

        for (BookingSlot slot : slotList) {
            if (slot.getProfAlias().equalsIgnoreCase(profAlias))
                output.add(slot);
        }

        return output;
    }

    @Override
    public List<BookingSlot> getSlotsByStudentAndSWeek(int studentId, LocalDate startDateOfSchoolWeek) {
        LocalDate endDateOfSchoolWeek = startDateOfSchoolWeek.plus(5, ChronoUnit.DAYS);
        List<BookingSlot> studentSlots = getSlotsByStudentId(studentId);
        List<BookingSlot> output = new ArrayList<>();

        for (BookingSlot slot : studentSlots) {
            if (!(slot.getDate().isBefore(startDateOfSchoolWeek)
                    || slot.getDate().isAfter(endDateOfSchoolWeek)))

                if (!slot.getBookStatus().equalsIgnoreCase(CANCELLED)) output.add(slot);
        }

        return output;
    }

    @Override
    public BookingSlot getSlotByProfAndDateTime(String profAlias, Timestamp startTime) {
        return slotRepository.findByProfAndDateTime(profAlias, startTime);
    }

    @Override
    public List<BookingSlot> getSlotsByCourseAndSWeek(String courseId, LocalDate startDateOfSchoolWeek) {
        List<BookingSlot> slotList = getSlotsBySchoolWeek(startDateOfSchoolWeek);
        List<BookingSlot> output = new ArrayList<>();
        List<Professor> professors = professorService.getProfessorsByCourseId(courseId);

        for (BookingSlot slot : slotList) {
            for (Professor prof : professors) {
                if (slot.getProfAlias().equalsIgnoreCase(prof.getAlias()))
                    output.add(slot);
            }
        }

        return output;
    }

    @Override
    public List<BookingSlot> getSlotsByDateTime(LocalDateTime dateTime) {
        Timestamp timestamp = Timestamp.valueOf(dateTime);

        return slotRepository.findByDateTime(timestamp);
    }

    @Override
    public List<BookingSlot> getSlotsByDate(LocalDate date) {
        return getSlotsByDateRangeType(DATE, date);
    }

    @Override
    public List<BookingSlot> getSlotsBySchoolWeek(LocalDate startDateOfSchoolWeek) {
        return getSlotsByDateRangeType(SCHOOL_WEEK, startDateOfSchoolWeek);
    }

    private List<BookingSlot> getSlotsByDateRangeType(String type, LocalDate startDate) {
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

            default:
                endDateTime = startDateTime.plus(SLOT_TIME, ChronoUnit.MINUTES);
                break;
        }

        Timestamp startTimestamp = Timestamp.valueOf(startDateTime);
        Timestamp endTimestamp = Timestamp.valueOf(endDateTime);
        return slotRepository.findByDateTimeRange(startTimestamp, endTimestamp);
    }
}
