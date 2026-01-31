package com.example.danceplanner.controller;

import com.example.danceplanner.service.DancerAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DancerSessionAuthController {

    private final DancerAuthService auth;

    public DancerSessionAuthController(DancerAuthService auth) {
        this.auth = auth;
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/api/dancer/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        var userIdOpt = auth.authenticate(req.username(), req.password());
        if (userIdOpt.isEmpty()) return ResponseEntity.status(401).body("Bad credentials");

        session.setAttribute("DANCER_ID", userIdOpt.get());
        session.setAttribute("DANCER_USERNAME", req.username());
        System.out.println("LOGIN username=" + req.username());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/dancer/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

//    // ca sa stii daca esti logat cand dai refresh
    @GetMapping("/api/dancer/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object u = session.getAttribute("DANCER_USERNAME");
        if (u == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(u);
    }

    @PostMapping("/api/dancer/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req,
                                            HttpSession session) {
        Object idObj = session.getAttribute("DANCER_ID");
        if (idObj == null) return ResponseEntity.status(401).build();

        long dancerId = ((Number) idObj).longValue();

        boolean ok = auth.changePassword(
                dancerId,
                req.oldPassword(),
                req.newPassword()
        );

        if (!ok) return ResponseEntity.status(400).body("Parola veche gresita");

        return ResponseEntity.ok().build();
    }

    public record ChangePasswordRequest(String oldPassword, String newPassword) {}

}

