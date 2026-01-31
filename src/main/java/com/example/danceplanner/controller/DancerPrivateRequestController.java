package com.example.danceplanner.controller;

import com.example.danceplanner.jdbc.JdbcDancerPrivateRequestRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dancer/private-request")
public class DancerPrivateRequestController {

    private final JdbcDancerPrivateRequestRepository repo;

    public DancerPrivateRequestController(JdbcDancerPrivateRequestRepository repo) {
        this.repo = repo;
    }

    // la fel ca in DancerController: verificam daca exista ceva in sesiune
    private boolean isLogged(HttpSession session) {
        return session.getAttribute("DANCER_ID") != null
                || session.getAttribute("DANCER_USERNAME") != null;
    }

    // luam dancerId din sesiune (trebuie sa existe, altfel aruncam)
    private long dancerId(HttpSession session) {
        Object id = session.getAttribute("DANCER_ID");
        if (id == null) throw new IllegalArgumentException("Not logged in");
        return (long) id;
    }

    // ------- DTO-uri pentru request-ul de la frontend -------
    // fiecare preferinta din body
    public record PrefReq(long coachId, int hours) {}

    // body-ul complet: totalHours + lista de preferinte
    public record SaveReq(int totalHours, List<PrefReq> preferences) {}

    /**
     * GET /api/dancer/private-request
     * Intoarce cererea curenta a dansatorului logat.
     */
    @GetMapping
    public ResponseEntity<?> getMyRequest(HttpSession session) {
        if (!isLogged(session)) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        var r = repo.getByDancer(dancerId(session));
        if (r == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(r);
    }

    /**
     * POST /api/dancer/private-request
     * Salveaza (create sau update) cererea dansatorului logat.
     */
    @PostMapping
    public ResponseEntity<?> saveMyRequest(@RequestBody SaveReq req, HttpSession session) {
        if (!isLogged(session)) {
            return ResponseEntity.status(401).body("Not logged in");
        }

        // convertim PrefReq -> repo.Preference
        var prefs = (req.preferences() == null)
                ? List.<JdbcDancerPrivateRequestRepository.Preference>of()
                : req.preferences().stream()
                .map(p -> new JdbcDancerPrivateRequestRepository.Preference(p.coachId(), p.hours()))
                .toList();

        try {
            var saved = repo.upsert(dancerId(session), req.totalHours(), prefs);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            // repo arunca IllegalArgumentException cand datele sunt invalide
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
