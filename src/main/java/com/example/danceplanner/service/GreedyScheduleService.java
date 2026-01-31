package com.example.danceplanner.service;

import com.example.danceplanner.data.*;
import com.example.danceplanner.jdbc.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GreedyScheduleService {

    private final JdbcTimetableRepository groupRepo;
    private final JdbcCoachRepository coachRepo;
    private final JdbcDancerRepository dancerRepo;
    private final JdbcDanceHallRepository hallRepo;
    private final JdbcCoachAvailabilityRepository coachAvailRepo;
    private final JdbcDancerAvailabilityRepository dancerAvailRepo;
    private final JdbcDancerPrivateRequestRepository requestRepo;

    public GreedyScheduleService(JdbcTimetableRepository groupRepo, JdbcCoachRepository coachRepo,
                                 JdbcDancerRepository dancerRepo, JdbcDanceHallRepository hallRepo,
                                 JdbcCoachAvailabilityRepository coachAvailRepo,
                                 JdbcDancerAvailabilityRepository dancerAvailRepo,
                                 JdbcDancerPrivateRequestRepository requestRepo) {
        this.groupRepo = groupRepo;
        this.coachRepo = coachRepo;
        this.dancerRepo = dancerRepo;
        this.hallRepo = hallRepo;
        this.coachAvailRepo = coachAvailRepo;
        this.dancerAvailRepo = dancerAvailRepo;
        this.requestRepo = requestRepo;
    }

    public List<JdbcCalendarEventRepository.Event> generateGreedySchedule() {
        List<JdbcCalendarEventRepository.Event> finalSchedule = new ArrayList<>();
        List<Coach> coaches = coachRepo.findAll();
        List<Dancer> dancers = dancerRepo.findAll();
        List<DanceHall> halls = hallRepo.findAll();

        // REPARARE: Folosim tipul Entry returnat de JdbcTimetableRepository
        List<JdbcTimetableRepository.Entry> groups = groupRepo.findAll();

        var cAvails = coaches.stream()
                .collect(Collectors.toMap(Coach::getId, c -> coachAvailRepo.findAllByCoach(c.getId())));
        var dAvails = dancers.stream()
                .collect(Collectors.toMap(Dancer::getId, d -> dancerAvailRepo.findAllByDancer(d.getId())));

        for (Dancer dancer : dancers) {
            var req = requestRepo.getByDancer(dancer.getId());
            if (req == null) continue;

            for (var pref : req.preferences()) {
                int scheduled = 0;
                for (int day = 1; day <= 7 && scheduled < pref.hours(); day++) {
                    if (isAlreadyOnDay(finalSchedule, dancer.getId(), day)) continue;

                    for (int h = 10; h <= 21 && scheduled < pref.hours(); h++) {
                        String startStr = String.format("%02d:00", h);
                        String endStr = String.format("%02d:00", h + 1);

                        if (canPlace(dancer, pref.coachId(), day, startStr, groups, finalSchedule, cAvails, dAvails)) {
                            DanceHall hall = findHall(halls, day, startStr, groups, finalSchedule);
                            if (hall != null) {
                                String cName = coaches.stream()
                                        .filter(c -> c.getId() == pref.coachId())
                                        .map(Coach::getName).findFirst().orElse("");

                                finalSchedule.add(new JdbcCalendarEventRepository.Event(
                                        0L, "PRIVATE", day, startStr, endStr,
                                        hall.getId(), hall.getName(),
                                        pref.coachId(), cName, null, null,
                                        dancer.getId(), dancer.getName()));
                                scheduled++;
                            }
                        }
                    }
                }
            }
        }
        return finalSchedule;
    }

    private boolean canPlace(Dancer d, long cId, int day, String time,
                             List<JdbcTimetableRepository.Entry> groups,
                             List<JdbcCalendarEventRepository.Event> current, Map ca, Map da) {

        boolean dGroup = groups.stream().anyMatch(g -> g.groupLevelId() == d.getLevelId() &&
                g.dayOfWeek() == day && g.startTime().startsWith(time));
        boolean cGroup = groups.stream().anyMatch(g -> g.coachId() == cId &&
                g.dayOfWeek() == day && g.startTime().startsWith(time));

        if (dGroup || cGroup) return false;

        return current.stream().noneMatch(p -> p.dayOfWeek() == day &&
                p.startTime().equals(time) &&
                (p.dancerId() == d.getId() || p.coachId() == cId));
    }

    private DanceHall findHall(List<DanceHall> halls, int day, String time,
                               List<JdbcTimetableRepository.Entry> groups,
                               List<JdbcCalendarEventRepository.Event> current) {
        for (DanceHall h : halls) {
            boolean hallHasGroup = groups.stream().anyMatch(g -> g.hallId() == h.getId() &&
                    g.dayOfWeek() == day && g.startTime().startsWith(time));
            if (hallHasGroup) continue;

            long count = current.stream().filter(p -> p.hallId() == h.getId() &&
                    p.dayOfWeek() == day && p.startTime().equals(time)).count();

            if ((count * 2) + 2 <= h.getCapacity()) return h;
        }
        return null;
    }

    private boolean isAlreadyOnDay(List<JdbcCalendarEventRepository.Event> list, long dId, int day) {
        return list.stream().anyMatch(e -> e.dancerId() == dId && e.dayOfWeek() == day);
    }
}