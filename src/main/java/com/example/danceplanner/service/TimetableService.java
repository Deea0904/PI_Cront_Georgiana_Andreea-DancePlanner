package com.example.danceplanner.service;

import com.example.danceplanner.dto.TimetableEntryDto;
import com.example.danceplanner.jdbc.JdbcCalendarEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    private final JdbcCalendarEventRepository calendarEventRepo;

    // Am pastrat doar injectarea necesara pentru calendarul unificat
    public TimetableService(JdbcCalendarEventRepository calendarEventRepo) {
        this.calendarEventRepo = calendarEventRepo;
    }

    /**
     * Returneaza programul complet (Grup + Privat) transformat in DTO-uri pentru interfata.
     */
    public List<TimetableEntryDto> getTimetable() {
        // Citim toate evenimentele (GROUP si PRIVATE) din tabelul unificat
        List<JdbcCalendarEventRepository.Event> events = calendarEventRepo.findAll();

        return events.stream().map(e -> {
                    // Determinam ce nume afisam in coloana "Detalii" (Grupa sau Dansatorul)
                    String detail = (e.eventType().equals("GROUP")) ? e.groupName() : e.dancerName();

                    return new TimetableEntryDto(
                            dayNameRo(e.dayOfWeek()), // Transformam indexul 1-7 in nume zi
                            e.startTime(),
                            e.endTime(),
                            e.hallName(),
                            e.coachName(),
                            detail
                    );
                })
                .sorted(Comparator.comparing(TimetableEntryDto::getDay)
                        .thenComparing(TimetableEntryDto::getStartTime))
                .collect(Collectors.toList());
    }

    /**
     * Helper pentru a transforma indexul zilei din baza de date in text.
     */
    private String dayNameRo(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "LUNI";
            case 2 -> "MARTI";
            case 3 -> "MIERCURI";
            case 4 -> "JOI";
            case 5 -> "VINERI";
            case 6 -> "SAMBATA";
            case 7 -> "DUMINICA";
            default -> "ZI " + dayOfWeek;
        };
    }

    // Adaptare pentru metoda apelata anterior in Controller
    public List<TimetableEntryDto> getTimetableThisWeek() {
        return getTimetable();
    }
}