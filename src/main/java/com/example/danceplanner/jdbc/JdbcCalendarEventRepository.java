package com.example.danceplanner.jdbc;

import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcCalendarEventRepository {

    private final long clubId = 1L;

    public record Event(
            long id,
            String eventType,     // GROUP / PRIVATE
            int dayOfWeek,
            String startTime,     // HH:mm
            String endTime,       // HH:mm

            long hallId,
            String hallName,

            long coachId,
            String coachName,

            Long groupLevelId,    // null pt PRIVATE
            String groupName,     // null pt PRIVATE

            Long dancerId,        // null pt GROUP
            String dancerName     // null pt GROUP
    ) {}

    // -------------------- READ ALL --------------------
    public List<Event> findAll() {
        String sql = """
            select
                ce.id,
                ce.event_type,
                ce.day_of_week,
                DATE_FORMAT(ce.start_time, '%H:%i') as start_time,
                DATE_FORMAT(ce.end_time, '%H:%i') as end_time,

                ce.hall_id,
                h.name as hall_name,

                ce.coach_id,
                c.name as coach_name,

                ce.group_level_id,
                gl.name as group_name,

                ce.dancer_id,
                d.name as dancer_name

            from calendar_event ce
            join coach c on c.id = ce.coach_id
            join dance_hall h on h.id = ce.hall_id
            left join group_level gl on gl.id = ce.group_level_id
            left join dancer d on d.id = ce.dancer_id

            where ce.club_id=?
            order by ce.day_of_week, ce.start_time, ce.id
        """;

        try (var conn = DB.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);

            try (var rs = ps.executeQuery()) {
                List<Event> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- FILTER BY GROUPS --------------------
    /**
     * Returneaza:
     * - GROUP events: group_level_id IN (groupIds)
     * - PRIVATE events: dancer-ul apartine uneia din grupele selectate (dancer.group_level_id IN groupIds)
     *
     * includeGroup/includePrivate controleaza ce tipuri se includ.
     */
    public List<Event> findByGroupIds(List<Long> groupIds, boolean includeGroup, boolean includePrivate) {
        if (groupIds == null || groupIds.isEmpty()) return findAll();
        if (!includeGroup && !includePrivate) return List.of();

        String in = groupIds.stream().map(x -> "?").reduce((a, b) -> a + "," + b).orElse("?");

        StringBuilder sql = new StringBuilder();
        sql.append("""
            select
                x.id,
                x.event_type,
                x.day_of_week,
                DATE_FORMAT(x.start_time, '%H:%i') as start_time,
                DATE_FORMAT(x.end_time, '%H:%i') as end_time,

                x.hall_id,
                h.name as hall_name,

                x.coach_id,
                c.name as coach_name,

                x.group_level_id,
                gl.name as group_name,

                x.dancer_id,
                d.name as dancer_name

            from (
        """);

        boolean first = true;

        if (includeGroup) {
            sql.append("""
                select ce.*
                from calendar_event ce
                where ce.club_id=? and ce.event_type='GROUP'
                  and ce.group_level_id in (""").append(in).append(")\n");
            first = false;
        }

        if (includePrivate) {
            if (!first) sql.append(" union all\n");
            sql.append("""
                select ce.*
                from calendar_event ce
                join dancer dd on dd.id = ce.dancer_id
                where ce.club_id=? and ce.event_type='PRIVATE'
                  and dd.level_id in (""").append(in).append(")\n");
        }

        sql.append("""
            ) x
            join coach c on c.id = x.coach_id
            join dance_hall h on h.id = x.hall_id
            left join group_level gl on gl.id = x.group_level_id
            left join dancer d on d.id = x.dancer_id
            order by x.day_of_week, x.start_time, x.id
        """);

        try (var conn = DB.getConnection();
             var ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;

            if (includeGroup) {
                ps.setLong(idx++, clubId);
                for (Long gid : groupIds) ps.setLong(idx++, gid);
            }

            if (includePrivate) {
                ps.setLong(idx++, clubId);
                for (Long gid : groupIds) ps.setLong(idx++, gid);
            }

            try (var rs = ps.executeQuery()) {
                List<Event> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- DELETE BY TYPE --------------------
    public int deleteByType(String type) {
        String sql = "delete from calendar_event where club_id=? and event_type=?";
        try (var conn = DB.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setString(2, type);

            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- IMPORT GROUPS FROM TIMETABLE --------------------
    /**
     * Importa orele de grup din timetable_entry ca evenimente GROUP in calendar_event.
     * Recomandat: inainte, deleteByType("GROUP") ca sa nu dublezi.
     */
    public int importGroupsFromTimetable() {
        String sql = """
            insert into calendar_event
              (club_id, event_type, day_of_week, start_time, end_time, hall_id, coach_id, group_level_id, dancer_id)
            select
              t.club_id, 'GROUP', t.day_of_week, t.start_time, t.end_time, t.hall_id, t.coach_id, t.group_level_id, null
            from timetable_entry t
            where t.club_id=?
        """;

        try (var conn = DB.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------- Helpers --------------------
    private Event mapRow(java.sql.ResultSet rs) throws Exception {
        // BIGINT nullable safe
        Object glObj = rs.getObject("group_level_id");
        Long groupLevelId = (glObj == null) ? null : ((Number) glObj).longValue();

        Object dObj = rs.getObject("dancer_id");
        Long dancerId = (dObj == null) ? null : ((Number) dObj).longValue();

        return new Event(
                rs.getLong("id"),
                rs.getString("event_type"),
                rs.getInt("day_of_week"),
                rs.getString("start_time"),
                rs.getString("end_time"),

                rs.getLong("hall_id"),
                rs.getString("hall_name"),

                rs.getLong("coach_id"),
                rs.getString("coach_name"),

                groupLevelId,
                rs.getString("group_name"),

                dancerId,
                rs.getString("dancer_name")
        );
    }
    public void savePrivateEvent(Long dancerId, long coachId, long hallId, int dayOfWeek, String startTime, String endTime) {
        String sql = """
        insert into calendar_event
          (club_id, event_type, day_of_week, start_time, end_time, hall_id, coach_id, dancer_id, group_level_id)
        values (?, 'PRIVATE', ?, ?, ?, ?, ?, ?, null)
    """;

        try (var conn = DB.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setInt(2, dayOfWeek);
            // Parametrii primiti de la AI sunt String (HH:mm)
            ps.setString(3, startTime);
            ps.setString(4, endTime);
            ps.setLong(5, hallId);
            ps.setLong(6, coachId);
            ps.setLong(7, dancerId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea evenimentului privat: " + e.getMessage(), e);
        }
    }

    public boolean deleteEvent(long id) {
        String sql = "delete from calendar_event where club_id=? and id=?";
        try (var conn = DB.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clubId);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public int importPrivatesFromPrivateTimetable() {
        String sql = """
        insert into calendar_event
          (club_id, event_type, day_of_week, start_time, end_time, hall_id, coach_id, group_level_id, dancer_id)
        select
          p.club_id, 'PRIVATE', p.day_of_week, p.start_time, p.end_time, p.hall_id, p.coach_id, null, p.dancer_id
        from timetable_private_entry p
        where p.club_id=?
    """;

        try (var conn = DB.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
