package com.example.danceplanner.controller;

import com.example.danceplanner.data.Coach;
import com.example.danceplanner.service.CoachService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping
    public List<Coach> getAll() {
        return coachService.getAllCoaches();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coach> getById(@PathVariable long id) {
        return coachService.getCoachById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Coach create(@RequestBody Coach coach) {
        return coachService.createCoach(coach);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coach> update(
            @PathVariable long id,
            @RequestBody Coach coach) {
        return coachService.updateCoach(id, coach)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoach(
            @PathVariable long id,
            @RequestParam(required = false) Long replacementId) {
        boolean success = coachService.deleteCoach(id, replacementId);

        if (success)
            return ResponseEntity.ok().build();
        return ResponseEntity.notFound().build();
    }
}
