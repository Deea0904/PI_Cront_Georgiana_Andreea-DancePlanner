package com.example.danceplanner.controller;

import com.example.danceplanner.jdbc.JdbcDancerAvailabilityRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dancer/availability")
public class DancerAvailabilityController {

    private final JdbcDancerAvailabilityRepository repo;

    public DancerAvailabilityController(JdbcDancerAvailabilityRepository repo) {
        this.repo = repo;
    }

    private boolean isLogged(HttpSession session) {
        return session.getAttribute("DANCER_ID") != null
                || session.getAttribute("DANCER_USERNAME") != null;
    }

    private long dancerId(HttpSession session) {
        Object id = session.getAttribute("DANCER_ID");
        if (id == null) throw new IllegalArgumentException("Not logged in");
        return (long) id;
    }

    public record AddReq(int dayOfWeek, String startTime, String endTime) {}

    // GET /api/dancer/availability
    @GetMapping
    public ResponseEntity<?> list(HttpSession session) {
        if (!isLogged(session)) return ResponseEntity.status(401).body("Not logged in");
        return ResponseEntity.ok(repo.findAllByDancer(dancerId(session)));
    }

    // POST /api/dancer/availability
    @PostMapping
    public ResponseEntity<?> add(@RequestBody AddReq req, HttpSession session) {
        if (!isLogged(session)) return ResponseEntity.status(401).body("Not logged in");

        try {
            var saved = repo.insert(dancerId(session), req.dayOfWeek(), req.startTime(), req.endTime());
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // DELETE /api/dancer/availability/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id, HttpSession session) {
        if (!isLogged(session)) return ResponseEntity.status(401).body("Not logged in");

        boolean ok = repo.delete(dancerId(session), id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
