package com.example.danceplanner.controller;

import com.example.danceplanner.jdbc.JdbcTimetableRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/timetable")
public class AdminTimetableController {

    private final JdbcTimetableRepository repo;

    public AdminTimetableController(JdbcTimetableRepository repo) {
        this.repo = repo;
    }

    public record AddReq(
            int dayOfWeek,
            String startTime,
            String endTime,
            long hallId,
            long coachId,
            long groupLevelId
    ) {}

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody AddReq req) {

        if (req.dayOfWeek() < 1 || req.dayOfWeek() > 7)
            return ResponseEntity.badRequest().body("Zi invalida (1..7)");
        if (req.startTime() == null || req.endTime() == null)
            return ResponseEntity.badRequest().body("Start/End lipsa");
        if (req.startTime().compareTo(req.endTime()) >= 0)
            return ResponseEntity.badRequest().body("Start trebuie < End");

        try {
            var saved = repo.insert(
                    req.dayOfWeek(), req.startTime(), req.endTime(),
                    req.hallId(), req.coachId(), req.groupLevelId()
            );
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        boolean ok = repo.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
