package com.example.danceplanner.controller;

import com.example.danceplanner.jdbc.JdbcCalendarEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/calendar")
public class AdminCalendarController {

    private final JdbcCalendarEventRepository repo;

    public AdminCalendarController(JdbcCalendarEventRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/generate-all")
    public ResponseEntity<?> generateAll() {

        int delGroup = repo.deleteByType("GROUP");
        int delPriv  = repo.deleteByType("PRIVATE");

        int insGroup = repo.importGroupsFromTimetable();
        int insPriv  = repo.importPrivatesFromPrivateTimetable();

        return ResponseEntity.ok(
                "OK. Calendar regenerated. " +
                        "deleted GROUP=" + delGroup +
                        ", deleted PRIVATE=" + delPriv +
                        ", inserted GROUP=" + insGroup +
                        ", inserted PRIVATE=" + insPriv
        );
    }
}
