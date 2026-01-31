package com.example.danceplanner.controller;

import com.example.danceplanner.dto.TimetableEntryDto;
import com.example.danceplanner.service.TimetableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping
    public List<TimetableEntryDto> getTimetable() {
        return timetableService.getTimetable();
    }
}