package com.example.danceplanner.controller;

import com.example.danceplanner.service.GreedyScheduleService;
import com.example.danceplanner.jdbc.JdbcCalendarEventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedule/greedy")
public class GreedyScheduleController {

    private final GreedyScheduleService greedyService;

    public GreedyScheduleController(GreedyScheduleService greedyService) {
        this.greedyService = greedyService;
    }

    @GetMapping("/generate")
    public List<JdbcCalendarEventRepository.Event> generate() {
        return greedyService.generateGreedySchedule();
    }
}