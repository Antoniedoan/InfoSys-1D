package profchoper._controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import profchoper._security.ProfChoperAuthFacade;
import profchoper.booking.BookingSlot;
import profchoper.booking.BookingSlotJS;
import profchoper.booking.BookingSlotService;
import profchoper.calendar.WeekCalendar;
import profchoper.calendar.WeekCalendarService;
import profchoper.professor.Professor;
import profchoper.professor.ProfessorService;
import profchoper.student.StudentService;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Controller
public class ProfCalendarController {
    private final WeekCalendarService weekCalendarService;
    private final BookingSlotService bookingSlotService;
    private final StudentService studentService;
    private final ProfessorService professorService;
    private final ProfChoperAuthFacade authFacade;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("dd/MM/yy - HH:mm");

    @Autowired
    public ProfCalendarController(WeekCalendarService weekCalendarService, StudentService studentService,
                                  ProfessorService professorService, ProfChoperAuthFacade authFacade,
                                  BookingSlotService bookingSlotService) {

        this.weekCalendarService = weekCalendarService;
        this.studentService = studentService;
        this.professorService = professorService;
        this.bookingSlotService = bookingSlotService;
        this.authFacade = authFacade;
    }

    @GetMapping("/prof")
    public String prof(Model model) {
        LocalDate currDate = LocalDate.now();
        if (currDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                || currDate.getDayOfWeek().equals(DayOfWeek.SUNDAY))
            currDate = currDate.plus(3, ChronoUnit.DAYS);

        LocalDate startDateOfSchoolWeek = currDate.with(DayOfWeek.MONDAY);
        LocalDate startDateOfSchoolTerm = LocalDate.of(2017, 9, 11);

        // String profEmail = authFacade.getAuthentication().getName();
        String profEmail = "jit_biswas@sutd.edu.sg";
        Professor prof = professorService.getProfessorByEmail(profEmail);

        WeekCalendar wkCal = weekCalendarService
                .getProfCalendar(prof.getAlias(), startDateOfSchoolTerm,
                        startDateOfSchoolWeek);

        model.addAttribute("calendar", wkCal);
        model.addAttribute("profName", prof.getName());


        return "prof";
    }

    @GetMapping(value = "/prof/calendar", params = {"date"})
    public String getProfCalendar(@RequestParam String date, Model model) {

        String profEmail = "jit_biswas@sutd.edu.sg";
        // String profEmail = authFacade.getAuthentication().getName();
        Professor prof = professorService.getProfessorByEmail(profEmail);

        LocalDate startDateOfSchoolWeek = LocalDate.parse(date, dtf);
        LocalDate startDateOfSchoolTerm = LocalDate.of(2017, 9, 11);


        WeekCalendar wkCal = weekCalendarService
                .getProfCalendar(prof.getAlias(), startDateOfSchoolTerm,
                        startDateOfSchoolWeek);

        model.addAttribute("calendar", wkCal);
        return "fragments/prof_cal";
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/prof", params = {"action"})
    public CalendarResponse confirmSlot(@RequestBody BookingSlotJS slotJS, @RequestParam String action) {

        String profEmail = "jit_biswas@sutd.edu.sg";
        // String profEmail = authFacade.getAuthentication().getName();

        Timestamp time = Timestamp.valueOf(LocalDateTime.parse(slotJS.getTime(), DATE_TIME_FORMATTER));
        BookingSlot slot = new BookingSlot(slotJS.getProfAlias(), time);

        if (action.equalsIgnoreCase("confirm")) {
            BookingSlot returnedSlot = bookingSlotService.bookSlot(slot, profEmail);
            if (returnedSlot != null)
                return new CalendarResponse("CONFIRM_DONE", returnedSlot);
            else
                return new CalendarResponse("CONFIRM_FAIL", slot);

        } else if (action.equalsIgnoreCase("reject")) {
            BookingSlot returnedSlot = bookingSlotService.cancelBookSlot(slot, profEmail);
            if (returnedSlot != null)
                return new CalendarResponse("CANCEL_DONE", returnedSlot);
            else
                return new CalendarResponse("CANCEL_FAIL", slot);
        }

        return new CalendarResponse("ERROR", null);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @DeleteMapping("/prof")
    public CalendarResponse deleteSlot(@RequestBody BookingSlot slot) {
        String profEmail = "jit_biswas@sutd.edu.sg";
        // String profEmail = authFacade.getAuthentication().getName();

        BookingSlot returnedSlot = bookingSlotService.deleteSlot(slot, profEmail);
        if (returnedSlot != null)
            return new CalendarResponse("DELETE_DONE", returnedSlot);
        else
            return new CalendarResponse("DELETE_FAIL", slot);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping("/prof")
    public CalendarResponse addSlot(@RequestBody BookingSlot slot) {
        String profEmail = "jit_biswas@sutd.edu.sg";
        // String profEmail = authFacade.getAuthentication().getName();

        BookingSlot returnedSlot = bookingSlotService.addSlot(slot, profEmail);
        if (returnedSlot != null)
            return new CalendarResponse("ADD_DONE", returnedSlot);
        else
            return new CalendarResponse("ADD_FAIL", slot);
    }

}
