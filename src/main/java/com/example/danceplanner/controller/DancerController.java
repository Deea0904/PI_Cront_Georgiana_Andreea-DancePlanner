package com.example.danceplanner.controller;

import com.example.danceplanner.data.Dancer;
import com.example.danceplanner.dto.CreateDancerRequest;
import com.example.danceplanner.dto.UpdateDancerRequest;
import com.example.danceplanner.service.DancerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dancers") // Am grupat rutele aici pentru claritate
public class DancerController {

    private final DancerService dancerService;

    public DancerController(DancerService dancerService) {
        this.dancerService = dancerService;
    }

    @GetMapping
    public List<Dancer> getDancers() {
        return dancerService.getAllDancers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dancer> getDancerById(@PathVariable long id) {
        return dancerService.getDancerById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Dancer> createDancer(@RequestBody CreateDancerRequest req) {
        return ResponseEntity.ok(dancerService.createDancer(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dancer> updateDancer(@PathVariable long id, @RequestBody UpdateDancerRequest req) {
        return dancerService.updateDancer(id, req)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDancer(@PathVariable long id) {
        boolean removed = dancerService.deleteDancer(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}