package com.example.danceplanner.controller;

import com.example.danceplanner.jdbc.JdbcCalendarEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class CalendarController {

    private final JdbcCalendarEventRepository repo;

    public CalendarController(JdbcCalendarEventRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/api/admin/calendar/approve-full-schedule")
    public ResponseEntity<?> approveFullSchedule(
            @RequestBody List<JdbcCalendarEventRepository.Event> privateSuggestions) {
        try {
            repo.deleteByType("GROUP");
            repo.deleteByType("PRIVATE");
            repo.importGroupsFromTimetable();

            for (JdbcCalendarEventRepository.Event s : privateSuggestions) {
                repo.savePrivateEvent(
                        s.dancerId(),
                        s.coachId(),
                        s.hallId(),
                        s.dayOfWeek(),
                        s.startTime(),
                        s.endTime());
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Eroare la salvarea orarului: " + e.getMessage());
        }
    }

    @GetMapping("/api/calendar")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String groupIds,
            @RequestParam(defaultValue = "true") boolean includeGroup,
            @RequestParam(defaultValue = "true") boolean includePrivate) {
        if (groupIds == null || groupIds.isBlank()) {
            return ResponseEntity.ok(repo.findAll());
        }

        try {
            List<Long> ids = Arrays.stream(groupIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();

            return ResponseEntity.ok(repo.findByGroupIds(ids, includeGroup, includePrivate));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Format invalid pentru ID-urile grupurilor.");
        }
    }

    @DeleteMapping("/api/admin/calendar/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable long id) {
        boolean deleted = repo.deleteEvent(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}