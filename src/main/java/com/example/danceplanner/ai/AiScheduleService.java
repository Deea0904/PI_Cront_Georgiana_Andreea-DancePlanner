package com.example.danceplanner.ai;

import com.example.danceplanner.data.Coach;
import com.example.danceplanner.data.DanceHall;
import com.example.danceplanner.data.Dancer;
import com.example.danceplanner.jdbc.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiScheduleService {

    private final GeminiClient gemini;
    private final ObjectMapper mapper;

    private final JdbcTimetableRepository groupRepo;
    private final JdbcCoachRepository coachRepo;
    private final JdbcDancerRepository dancerRepo;
    private final JdbcDanceHallRepository hallRepo;
    private final JdbcCoachAvailabilityRepository coachAvailRepo;
    private final JdbcDancerAvailabilityRepository dancerAvailRepo;
    private final JdbcDancerPrivateRequestRepository requestRepo;

    public AiScheduleService(
            GeminiClient gemini,
            JdbcTimetableRepository groupRepo,
            JdbcCoachRepository coachRepo,
            JdbcDancerRepository dancerRepo,
            JdbcDanceHallRepository hallRepo,
            JdbcCoachAvailabilityRepository coachAvailRepo,
            JdbcDancerAvailabilityRepository dancerAvailRepo,
            JdbcDancerPrivateRequestRepository requestRepo,
            ObjectMapper mapper
    ) {
        this.gemini = gemini;
        this.groupRepo = groupRepo;
        this.coachRepo = coachRepo;
        this.dancerRepo = dancerRepo;
        this.hallRepo = hallRepo;
        this.coachAvailRepo = coachAvailRepo;
        this.dancerAvailRepo = dancerAvailRepo;
        this.requestRepo = requestRepo;
        this.mapper = mapper;
    }

    public List<JdbcCalendarEventRepository.Event> generateWeeklyPrivateSchedule() {
        List<Coach> allCoaches = coachRepo.findAll();
        List<Dancer> allDancers = dancerRepo.findAll();
        List<DanceHall> allHalls = hallRepo.findAll();
        var allGroupLessons = groupRepo.findAll();

        String validHallIds = allHalls.stream()
                .map(h -> String.valueOf(h.getId()))
                .collect(Collectors.joining(", "));

        // Map rapid: hallId -> hallName (ca sa nu fac stream de fiecare data)
        Map<Long, String> hallNames = allHalls.stream()
                .collect(Collectors.toMap(DanceHall::getId, DanceHall::getName, (a, b) -> a));

        Map<Long, String> coachNames = allCoaches.stream()
                .collect(Collectors.toMap(Coach::getId, Coach::getName, (a, b) -> a));

        Map<Long, String> dancerNames = allDancers.stream()
                .collect(Collectors.toMap(Dancer::getId, Dancer::getName, (a, b) -> a));

        StringBuilder context = new StringBuilder();
        context.append("### AVAILABLE HALLS: ");
        allHalls.forEach(h -> context.append(h.getId()).append(" (").append(h.getName()).append("), "));
        context.append("\n");

        for (Coach c : allCoaches) {
            var slots = coachAvailRepo.findAllByCoach(c.getId());
            if (!slots.isEmpty()) {
                context.append("- Coach ").append(c.getId()).append(": ");
                slots.forEach(s -> context.append(String.format("Day %d[%s-%s] ", s.dayOfWeek(), s.startTime(), s.endTime())));
                context.append("\n");
            }
        }

        for (Dancer d : allDancers) {
            var req = requestRepo.getByDancer(d.getId());
            if (req != null && req.preferences() != null && !req.preferences().isEmpty()) {
                context.append(String.format("- Dancer %d (%s): ", d.getId(), d.getName()));

                var myGroupLessons = allGroupLessons.stream()
                        .filter(g -> g.groupLevelId() == d.getLevelId())
                        .collect(Collectors.toList());

                context.append("BLOCKED BY GROUP: ");
                myGroupLessons.forEach(gl ->
                        context.append(String.format("Day %d[%s-%s] ", gl.dayOfWeek(), gl.startTime(), gl.endTime()))
                );

                context.append(" | NEEDS: ");
                req.preferences().forEach(p ->
                        context.append(String.format("Coach %d (%d hours) ", p.coachId(), p.hours()))
                );

                context.append(" | Avail: ");
                dancerAvailRepo.findAllByDancer(d.getId()).forEach(s ->
                        context.append(String.format("Day %d[%s-%s] ", s.dayOfWeek(), s.startTime(), s.endTime()))
                );

                context.append("\n");
            }
        }

        String prompt = """
                You are a PROFESSIONAL DANCE SCHOOL MASTER SCHEDULER.

TASK:
Generate a COMPLETE weekly schedule for PRIVATE dance lessons as a JSON ARRAY.

STRICT OUTPUT (MUST):
- Output ONLY a raw JSON array.
- No markdown, no backticks, no explanations.
- First char must be '[' and last char must be ']'.
- Every array element must be a complete JSON object.
- Do NOT output any other objects (no "error" objects).

FORMAT (STRICT):
Each object MUST have EXACTLY these keys:
- dancerId  (number)
- coachId   (number)
- hallId    (number)
- dayOfWeek (number 1..7 where 1=Monday, 7=Sunday)
- startTime (string "HH:mm")
- endTime   (string "HH:mm")

No extra keys.


HARD RULES:
1) DURATION:
   - Each lesson must be exactly 60 minutes.
   - endTime = startTime + 60 minutes.

2) DAILY LIMIT:
   - Max 1 private lesson per dancer per day.

3) NO OVERLAPS (same day):
   - Coach cannot overlap with another private.
   - Hall cannot overlap with another private.
   - Dancer cannot overlap with another private.

4) BLOCKED BY GROUP:
   - Do NOT schedule any private lesson overlapping a dancer's "BLOCKED BY GROUP" intervals.

5) AVAILABILITY:
   - A private lesson must be fully inside BOTH:
     a) coach availability interval
     b) dancer availability interval

6) IDs:
   - Use ONLY numeric IDs that appear in the context.
   - Do NOT invent IDs.
   - Do NOT output names or placeholders.

7) HALLS:
   - Use ONLY hall IDs from this list: %s
   - Spread lessons across multiple halls when possible.


COMPLETENESS (VERY IMPORTANT):
For each dancer, the context contains:
  NEEDS: Coach X (N hours)
You MUST generate exactly N separate lessons (each 60 minutes) for that coach+dancer pair.
Example:
  NEEDS: Coach 8 (2 hours)
=> output exactly 2 objects with dancerId=..., coachId=8

Do NOT generate extra lessons.
Do NOT miss required lessons.
If you cannot schedule all required lessons due to constraints,
return the BEST possible schedule that respects all hard rules
(even if incomplete). Still return a valid JSON array.

UNIFORM DISTRIBUTION (VERY IMPORTANT):
- If a dancer has multiple lessons in the week, spread them on DIFFERENT days.
- Never place 2 lessons for same dancer in same day (already strict).


CONTEXT:
%s
""".formatted(validHallIds, context.toString());

        String aiText = gemini.generate(prompt);

        return parseResponse(aiText, coachNames, dancerNames, hallNames);
    }

    private List<JdbcCalendarEventRepository.Event> parseResponse(
            String resp,
            Map<Long, String> coachNames,
            Map<Long, String> dancerNames,
            Map<Long, String> hallNames
    ) {
        try {
            String cleaned = resp
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            int start = cleaned.indexOf("[");
            int end = cleaned.lastIndexOf("]");
            if (start < 0 || end < 0 || end <= start) {
                throw new RuntimeException("AI nu a returnat un JSON array valid. Răspuns=" + resp);
            }

            String json = cleaned.substring(start, end + 1);
            JsonNode root = mapper.readTree(json);

            if (!root.isArray()) {
                throw new RuntimeException("AI nu a returnat array JSON.");
            }

            List<JdbcCalendarEventRepository.Event> out = new ArrayList<>();

            for (JsonNode node : root) {
                long dId = requireLong(node, "dancerId", "dancer_id");
                long cId = requireLong(node, "coachId", "coach_id");
                long hId = requireLong(node, "hallId", "hall_id");

                int dayNum = parseDayOfWeek(node);
                String startTime = requireText(node, "startTime", "start_time");
                String endTime = requireText(node, "endTime", "end_time");

                String hName = hallNames.getOrDefault(hId, "Sala " + hId);
                String cName = coachNames.getOrDefault(cId, "Coach " + cId);
                String dName = dancerNames.getOrDefault(dId, "Dancer " + dId);

                out.add(new JdbcCalendarEventRepository.Event(
                        0L, "PRIVATE", dayNum, startTime, endTime,
                        hId, hName,
                        cId, cName,
                        null, null,
                        dId, dName
                ));
            }

            return out;

        } catch (Exception e) {
            throw new RuntimeException("Eroare parsare: " + e.getMessage(), e);
        }
    }

    private long requireLong(JsonNode node, String k1, String k2) {
        JsonNode n = node.hasNonNull(k1) ? node.get(k1) : (node.hasNonNull(k2) ? node.get(k2) : null);
        if (n == null) throw new RuntimeException("Lipsește câmp numeric: " + k1 + "/" + k2);

        if (n.isNumber()) return n.asLong();

        String s = n.asText("").replaceAll("[^0-9]", "");
        if (s.isBlank()) throw new RuntimeException("Câmp numeric invalid: " + k1 + "/" + k2);
        return Long.parseLong(s);
    }

    private String requireText(JsonNode node, String k1, String k2) {
        JsonNode n = node.hasNonNull(k1) ? node.get(k1) : (node.hasNonNull(k2) ? node.get(k2) : null);
        if (n == null) throw new RuntimeException("Lipsește câmp text: " + k1 + "/" + k2);

        String v = n.asText("").trim();
        if (v.isBlank()) throw new RuntimeException("Câmp text invalid: " + k1 + "/" + k2);
        return v;
    }

    private int parseDayOfWeek(JsonNode node) {
        if (node.hasNonNull("dayOfWeek")) {
            JsonNode d = node.get("dayOfWeek");
            if (d.isNumber()) return d.asInt();
            String s = d.asText("");
            return parseDayStringToInt(s);
        }
        if (node.hasNonNull("day")) {
            String s = node.get("day").asText("");
            return parseDayStringToInt(s);
        }
        throw new RuntimeException("Lipsește dayOfWeek.");
    }

    private int parseDayStringToInt(String raw) {
        String digits = raw.replaceAll("[^0-9]", "");
        if (!digits.isBlank()) return Integer.parseInt(digits);
        throw new RuntimeException("dayOfWeek invalid: " + raw);
    }
}
