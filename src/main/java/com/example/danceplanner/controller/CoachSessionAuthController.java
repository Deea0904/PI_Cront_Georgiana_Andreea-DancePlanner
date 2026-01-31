package com.example.danceplanner.controller;

import com.example.danceplanner.service.CoachAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CoachSessionAuthController {

    private final CoachAuthService auth;

    public CoachSessionAuthController(CoachAuthService auth) {
        this.auth = auth;
    }

    public record LoginRequest(String username, String password) {}
    public record ChangePasswordRequest(String oldPassword, String newPassword) {}

    @PostMapping("/api/coach/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        var userIdOpt = auth.authenticate(req.username(), req.password());
        if (userIdOpt.isEmpty()) return ResponseEntity.status(401).body("Bad credentials");

        session.setAttribute("COACH_ID", userIdOpt.get());
        session.setAttribute("COACH_USERNAME", req.username());
        System.out.println("COACH LOGIN username=" + req.username());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/coach/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/coach/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object u = session.getAttribute("COACH_USERNAME");
        if (u == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(u);
    }

    @PostMapping("/api/coach/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req,
                                            HttpSession session) {
        Object idObj = session.getAttribute("COACH_ID");
        if (idObj == null) return ResponseEntity.status(401).build();

        long coachId = ((Number) idObj).longValue();

        boolean ok = auth.changePassword(
                coachId,
                req.oldPassword(),
                req.newPassword()
        );

        if (!ok) return ResponseEntity.status(400).body("Parola veche gresita");
        return ResponseEntity.ok().build();
    }
}
