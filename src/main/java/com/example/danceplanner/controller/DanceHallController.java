package com.example.danceplanner.controller;

import com.example.danceplanner.data.DanceHall;
import com.example.danceplanner.service.DanceHallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
public class DanceHallController {

    private final DanceHallService hallService;

    public DanceHallController(DanceHallService hallService) {
        this.hallService = hallService;
    }

    @GetMapping
    public List<DanceHall> getAll() {
        return hallService.getAllHalls();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DanceHall> getById(@PathVariable long id) {
        return hallService.getHallById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public DanceHall create(@RequestBody DanceHall hall) {
        return hallService.createHall(hall);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DanceHall> update(
            @PathVariable long id,
            @RequestBody DanceHall hall
    ) {
        return hallService.updateHall(id, hall)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        return hallService.deleteHall(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
