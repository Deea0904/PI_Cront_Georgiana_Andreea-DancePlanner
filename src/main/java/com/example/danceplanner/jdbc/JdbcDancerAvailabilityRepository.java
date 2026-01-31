package com.example.danceplanner.jdbc;

import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcDancerAvailabilityRepository {

    private final long clubId = 1L;

    public record Slot(
            long id,
            int dayOfWeek,
            String startTime,
            String endTime) {
    }

    public List<Slot> findAllByDancer(long dancerId) {
        String sql = """
                    select id, day_of_week,
                           DATE_FORMAT(start_time, '%H:%i') as start_time,
                           DATE_FORMAT(end_time, '%H:%i') as end_time
                    from dancer_availability
                    where club_id=? and dancer_id=?
                    order by day_of_week, start_time
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, dancerId);

            try (var rs = ps.executeQuery()) {
                List<Slot> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Slot(
                            rs.getLong("id"),
                            rs.getInt("day_of_week"),
                            rs.getString("start_time"),
                            rs.getString("end_time")));
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Slot insert(long dancerId, int dayOfWeek, String start, String end) {
        if (dayOfWeek < 1 || dayOfWeek > 7)
            throw new IllegalArgumentException("Zi invalida (1..7)");
        if (start == null || end == null)
            throw new IllegalArgumentException("Start/End lipsa");
        if (start.compareTo(end) >= 0)
            throw new IllegalArgumentException("Start trebuie < End");

        // nu permitem intervale suprapuse pentru acelasi dancer in aceeasi zi
        if (hasOverlap(dancerId, dayOfWeek, start, end)) {
            throw new IllegalArgumentException("Ai deja un interval suprapus in acea zi.");
        }

        String sql = """
                    insert into dancer_availability (club_id, dancer_id, day_of_week, start_time, end_time)
                    values (?, ?, ?, ?, ?)
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, clubId);
            ps.setLong(2, dancerId);
            ps.setInt(3, dayOfWeek);
            ps.setTime(4, toSqlTime(start));
            ps.setTime(5, toSqlTime(end));

            ps.executeUpdate();

            long id;
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getLong(1);
            }

            return new Slot(id, dayOfWeek, start, end);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(long dancerId, long id) {
        String sql = "delete from dancer_availability where club_id=? and dancer_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, dancerId);
            ps.setLong(3, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Suprapunere: noul inceput < sfarsitul existent SI noul sfarsit > inceputul
    // existent
    private boolean hasOverlap(long dancerId, int dayOfWeek, String start, String end) {
        String sql = """
                    select 1
                    from dancer_availability
                    where club_id=? and dancer_id=? and day_of_week=?
                      and ? < end_time
                      and ? > start_time
                    limit 1
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, dancerId);
            ps.setInt(3, dayOfWeek);

            ps.setTime(4, toSqlTime(start));
            ps.setTime(5, toSqlTime(end));

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
