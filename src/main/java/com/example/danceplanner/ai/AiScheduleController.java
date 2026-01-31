package com.example.danceplanner.ai;

import com.example.danceplanner.jdbc.JdbcCalendarEventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/schedule")
public class AiScheduleController {

    private final AiScheduleService aiService;

    public AiScheduleController(AiScheduleService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/generate-weekly")
    public List<JdbcCalendarEventRepository.Event> generateWeekly() {
        return aiService.generateWeeklyPrivateSchedule();
    }
}