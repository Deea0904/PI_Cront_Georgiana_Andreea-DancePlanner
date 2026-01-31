package com.example.danceplanner.service;

import com.example.danceplanner.data.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TimetableGenerator {

    // Inlocuitor pentru PrivateLesson in interiorul algoritmului
    public static class ScheduledSession {
        public Long dancerId;
        public Long coachId;
        public Long hallId;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
    }

    private static class TimeSlot {
        LocalDateTime start;
        LocalDateTime end;
        Long coachId;
        Long hallId;

        TimeSlot(LocalDateTime start, LocalDateTime end, Long coachId, Long hallId) {
            this.start = start;
            this.end = end;
            this.coachId = coachId;
            this.hallId = hallId;
        }
    }

    public static class Result {
        private final List<ScheduledSession> scheduled;
        private final List<PrivateLessonRequest> notFullyScheduled;

        public Result(List<ScheduledSession> scheduled,
                List<PrivateLessonRequest> notFullyScheduled) {
            this.scheduled = scheduled;
            this.notFullyScheduled = notFullyScheduled;
        }

        public List<ScheduledSession> getScheduled() {
            return scheduled;
        }

        public List<PrivateLessonRequest> getNotFullyScheduled() {
            return notFullyScheduled;
        }
    }

    private final int slotMinutes;

    public TimetableGenerator(int slotMinutes) {
        this.slotMinutes = slotMinutes;
    }

    public Result generate(
            LocalDate startDate,
            LocalDate endDate,
            List<DanceHall> halls,
            List<CoachWorkingInterval> coachWorkingIntervals,
            List<GroupLesson> groupLessons,
            List<PrivateLessonRequest> requests,
            List<ScheduledSession> existingLessons // Adaptat aici
    ) {
        List<TimeSlot> allSlots = buildAllSlots(startDate, endDate, halls, coachWorkingIntervals);

        filterBusyByGroupsAndExisting(allSlots, groupLessons, existingLessons);

        Map<Long, List<TimeSlot>> possibleSlotsPerRequest = computePossibleSlotsForRequests(requests, allSlots);

        List<PrivateLessonRequest> sortedRequests = new ArrayList<>(requests);
        sortedRequests.sort(Comparator.comparingInt(
                r -> possibleSlotsPerRequest.getOrDefault(r.getId(), List.of()).size()));

        List<ScheduledSession> scheduled = new ArrayList<>();
        List<PrivateLessonRequest> notFully = new ArrayList<>();

        Set<TimeSlot> usedSlots = new HashSet<>();
        Map<Long, List<TimeSlot>> dancerBusy = new HashMap<>();
        Map<Long, List<TimeSlot>> coachBusy = new HashMap<>();

        for (PrivateLessonRequest req : sortedRequests) {
            int assigned = 0;
            List<TimeSlot> candidates = new ArrayList<>(possibleSlotsPerRequest.getOrDefault(req.getId(), List.of()));
            candidates.sort(Comparator.comparing(ts -> ts.start));

            for (TimeSlot slot : candidates) {
                if (assigned >= req.getNumberOfLessons())
                    break;
                if (usedSlots.contains(slot))
                    continue;
                if (overlaps(dancerBusy.get(req.getDancerId()), slot))
                    continue;
                if (overlaps(coachBusy.get(req.getCoachId()), slot))
                    continue;

                // Folosim ScheduledSession în loc de PrivateLesson
                ScheduledSession session = new ScheduledSession();
                session.dancerId = req.getDancerId();
                session.coachId = req.getCoachId();
                session.hallId = slot.hallId;
                session.startTime = slot.start;
                session.endTime = slot.end;

                scheduled.add(session);
                usedSlots.add(slot);

                dancerBusy.computeIfAbsent(req.getDancerId(), k -> new ArrayList<>()).add(slot);
                coachBusy.computeIfAbsent(req.getCoachId(), k -> new ArrayList<>()).add(slot);

                assigned++;
            }

            if (assigned < req.getNumberOfLessons()) {
                notFully.add(req);
            }
        }

        return new Result(scheduled, notFully);
    }

    // Metodele helper raman identice, dar folosesc ScheduledSession in loc de
    // PrivateLesson
    private void filterBusyByGroupsAndExisting(
            List<TimeSlot> slots,
            List<GroupLesson> groupLessons,
            List<ScheduledSession> existingLessons) {
        slots.removeIf(slot -> {
            for (GroupLesson gl : groupLessons) {
                if (!Objects.equals(gl.getCoachId(), slot.coachId))
                    continue;
                if (gl.getDanceHallId() != null && !Objects.equals(gl.getDanceHallId(), slot.hallId))
                    continue;
                if (overlaps(slot.start, slot.end, gl.getStartTime(), gl.getEndTime()))
                    return true;
            }
            for (ScheduledSession pl : existingLessons) {
                if (!Objects.equals(pl.coachId, slot.coachId))
                    continue;
                if (!Objects.equals(pl.hallId, slot.hallId))
                    continue;
                if (overlaps(slot.start, slot.end, pl.startTime, pl.endTime))
                    return true;
            }
            return false;
        });
    }

    // ... Restul metodelor helper (overlaps, buildAllSlots,
    // isInsideAnyAvailability) raman neschimbate
    // (Pastreaza-le din codul tau original)

    private boolean overlaps(List<TimeSlot> list, TimeSlot candidate) {
        if (list == null)
            return false;
        for (TimeSlot ts : list) {
            boolean o = !(ts.end.isEqual(candidate.start) || ts.end.isBefore(candidate.start)
                    || candidate.end.isEqual(ts.start) || candidate.end.isBefore(ts.start));
            if (o)
                return true;
        }
        return false;
    }

    private List<TimeSlot> buildAllSlots(LocalDate startDate, LocalDate endDate, List<DanceHall> halls,
            List<CoachWorkingInterval> working) {
        List<TimeSlot> slots = new ArrayList<>();
        for (CoachWorkingInterval wi : working) {
            LocalDateTime start = wi.getStartTime();
            LocalDateTime end = wi.getEndTime();
            if (start.toLocalDate().isBefore(startDate))
                start = LocalDateTime.of(startDate, start.toLocalTime());
            if (end.toLocalDate().isAfter(endDate))
                end = LocalDateTime.of(endDate, end.toLocalTime());
            LocalDateTime cursor = start;
            while (cursor.plusMinutes(slotMinutes).isBefore(end) || cursor.plusMinutes(slotMinutes).isEqual(end)) {
                LocalDateTime slotEnd = cursor.plusMinutes(slotMinutes);
                for (DanceHall hall : halls) {
                    slots.add(new TimeSlot(cursor, slotEnd, wi.getCoachId(), hall.getId()));
                }
                cursor = cursor.plusMinutes(slotMinutes);
            }
        }
        return slots;
    }

    private Map<Long, List<TimeSlot>> computePossibleSlotsForRequests(List<PrivateLessonRequest> requests,
            List<TimeSlot> allSlots) {
        Map<Long, List<TimeSlot>> map = new HashMap<>();
        for (PrivateLessonRequest req : requests) {
            List<TimeSlot> possible = new ArrayList<>();
            Duration lessonDur = Duration.ofMinutes(req.getLessonDurationMinutes());
            for (TimeSlot slot : allSlots) {
                if (!Objects.equals(slot.coachId, req.getCoachId()))
                    continue;
                if (Duration.between(slot.start, slot.end).compareTo(lessonDur) < 0)
                    continue;
                if (isInsideAnyAvailability(slot, req.getAvailabilities()))
                    possible.add(slot);
            }
            map.put(req.getId(), possible);
        }
        return map;
    }

    private boolean isInsideAnyAvailability(TimeSlot slot, List<AvailabilityInterval> avs) {
        for (AvailabilityInterval av : avs) {
            if (!slot.start.isBefore(av.getStartTime()) && !slot.end.isAfter(av.getEndTime()))
                return true;
        }
        return false;
    }

    private boolean overlaps(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return !(e1.isEqual(s2) || e1.isBefore(s2) || e2.isEqual(s1) || e2.isBefore(s1));
    }
}