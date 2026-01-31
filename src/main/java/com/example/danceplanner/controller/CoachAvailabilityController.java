package com.example.danceplanner.controller;

import com.example.danceplanner.jdbc.JdbcCoachAvailabilityRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coach/availability")
public class CoachAvailabilityController {

    private final JdbcCoachAvailabilityRepository repo;

    public CoachAvailabilityController(JdbcCoachAvailabilityRepository repo) {
        this.repo = repo;
    }

    private boolean isLogged(HttpSession session) {
        return session.getAttribute("COACH_ID") != null
                || session.getAttribute("COACH_USERNAME") != null;
    }

    private long coachId(HttpSession session) {
        Object id = session.getAttribute("COACH_ID");
        if (id == null) throw new IllegalArgumentException("Not logged in");
        return (long) id;
    }

    public record AddReq(int dayOfWeek, String startTime, String endTime) {}

    // GET /api/coach/availability
    @GetMapping
    public ResponseEntity<?> list(HttpSession session) {
        if (!isLogged(session)) return ResponseEntity.status(401).body("Not logged in");
        return ResponseEntity.ok(repo.findAllByCoach(coachId(session)));
    }

    // POST /api/coach/availability
    @PostMapping
    public ResponseEntity<?> add(@RequestBody AddReq req, HttpSession session) {
        if (!isLogged(session)) return ResponseEntity.status(401).body("Not logged in");

        try {
            var saved = repo.insert(coachId(session), req.dayOfWeek(), req.startTime(), req.endTime());
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // DELETE /api/coach/availability/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id, HttpSession session) {
        if (!isLogged(session)) return ResponseEntity.status(401).body("Not logged in");

        boolean ok = repo.delete(coachId(session), id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
