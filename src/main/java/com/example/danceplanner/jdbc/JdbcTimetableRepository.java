package com.example.danceplanner.jdbc;

import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcTimetableRepository {

    private final long clubId = 1L;

    public record Entry(
            long id,
            int dayOfWeek,
            String startTime,
            String endTime,
            long hallId,
            long coachId,
            long groupLevelId) {
    }

    public List<Entry> findAll() {
        String sql = """
                    select id, day_of_week,
                           DATE_FORMAT(start_time, '%H:%i') as start_time,
                           DATE_FORMAT(end_time, '%H:%i') as end_time,
                           hall_id, coach_id, group_level_id
                    from timetable_entry
                    where club_id=?
                    order by day_of_week, start_time
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, clubId);
            try (var rs = ps.executeQuery()) {
                List<Entry> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Entry(
                            rs.getLong("id"),
                            rs.getInt("day_of_week"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getLong("hall_id"),
                            rs.getLong("coach_id"),
                            rs.getLong("group_level_id")));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Entry insert(int dayOfWeek, String start, String end, long hallId, long coachId, long groupLevelId) {
        if (hasOverlapInAny("hall_id", hallId, dayOfWeek, start, end))
            throw new IllegalArgumentException("Sala este deja ocupată în acel interval (curs de grup sau privat).");

        if (hasOverlapInAny("coach_id", coachId, dayOfWeek, start, end))
            throw new IllegalArgumentException("Antrenorul are deja o sesiune în acel interval.");

        if (checkOverlap(
                "SELECT 1 FROM timetable_entry WHERE day_of_week=? AND group_level_id=? AND ? < end_time AND ? > start_time",
                groupLevelId, dayOfWeek, start, end))
            throw new IllegalArgumentException("Grupa are deja un antrenament programat atunci.");

        String sql = """
                    INSERT INTO timetable_entry
                    (club_id, day_of_week, start_time, end_time, hall_id, coach_id, group_level_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, clubId);
            ps.setInt(2, dayOfWeek);
            ps.setTime(3, toSqlTime(start));
            ps.setTime(4, toSqlTime(end));
            ps.setLong(5, hallId);
            ps.setLong(6, coachId);
            ps.setLong(7, groupLevelId);

            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Entry(rs.getLong(1), dayOfWeek, start, end, hallId, coachId, groupLevelId);
                }
                throw new SQLException("Eșec la inserare orar, ID-ul nu a fost generat.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(long id) {
        String sql = "delete from timetable_entry where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {
            ps.setLong(1, clubId);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Logica Overlap: verifica in ambele tabele de orar
    private boolean hasOverlapInAny(String field, long entityId, int dayOfWeek, String start, String end) {
        String sqlGroup = "SELECT 1 FROM timetable_entry WHERE day_of_week=? AND %s=? AND ? < end_time AND ? > start_time"
                .formatted(field);
        String sqlPrivate = "SELECT 1 FROM timetable_private_entry WHERE day_of_week=? AND %s=? AND ? < end_time AND ? > start_time"
                .formatted(field);

        return checkOverlap(sqlGroup, entityId, dayOfWeek, start, end) ||
                checkOverlap(sqlPrivate, entityId, dayOfWeek, start, end);
    }

    private boolean checkOverlap(String sql, long entityId, int dayOfWeek, String start, String end) {
        try (var c = DB.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, dayOfWeek);
            ps.setLong(2, entityId);
            ps.setTime(3, toSqlTime(start));
            ps.setTime(4, toSqlTime(end));
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Time toSqlTime(String hhmm) {
        String s = hhmm.length() == 5 ? (hhmm + ":00") : hhmm;
        return Time.valueOf(s);
    }
}