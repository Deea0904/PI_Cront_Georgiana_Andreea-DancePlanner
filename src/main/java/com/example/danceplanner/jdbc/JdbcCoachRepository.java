package com.example.danceplanner.jdbc;

import com.example.danceplanner.data.Coach;
import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCoachRepository {

    private final long clubId = 1L;

    // CREARE
    public Coach save(Coach c) {
        String sql = "insert into coach(club_id, name) values (?,?)";
        try (var conn = DB.getConnection();
                var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, clubId);
            ps.setString(2, c.getName());
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getLong(1));
                }
            }
            return c;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // CITIRE - toate
    public List<Coach> findAll() {
        String sql = "select id, name from coach where club_id=? order by id";
        try (var conn = DB.getConnection();
                var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);

            try (var rs = ps.executeQuery()) {
                List<Coach> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // CITIRE - dupa id
    public Optional<Coach> findById(long id) {
        String sql = "select id, name from coach where club_id=? and id=?";
        try (var conn = DB.getConnection();
                var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(long id) {
        String sql = "select 1 from coach where club_id=? and id=?";
        try (var conn = DB.getConnection();
                var ps = conn.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, id);

            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ACTUALIZARE
    public void update(Coach c) {
        String sql = "update coach set name=? where club_id=? and id=?";
        try (var conn = DB.getConnection();
                var ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getName());
            ps.setLong(2, clubId);
            ps.setLong(3, c.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // STERGERE
    public boolean delete(long id, Long replacementCoachId) {
        String delPrivateTimetable = "DELETE FROM timetable_private_entry WHERE coach_id=?";
        String delPrefs = "DELETE FROM dancer_private_request_preference WHERE coach_id=?";
        String delCalendar = "DELETE FROM calendar_event WHERE coach_id=?";
        String delPrivate = "DELETE FROM private_lesson WHERE coach_id=?";

        String updateTimetable = "UPDATE timetable_entry SET coach_id = ? WHERE coach_id = ?";
        String nullTimetable = "UPDATE timetable_entry SET coach_id = NULL WHERE coach_id = ?";

        String delCoach = "DELETE FROM coach WHERE id=?";

        try (var conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Orarul de Grup
                if (replacementCoachId != null && replacementCoachId > 0) {
                    try (var ps = conn.prepareStatement(updateTimetable)) {
                        ps.setLong(1, replacementCoachId);
                        ps.setLong(2, id);
                        ps.executeUpdate();
                    }
                } else {
                    try (var ps = conn.prepareStatement(nullTimetable)) {
                        ps.setLong(1, id);
                        ps.executeUpdate();
                    }
                }

                // Stergem din Orarul Privat
                try (var ps = conn.prepareStatement(delPrivateTimetable)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }

                // Stergem restul dependentelor
                try (var ps = conn.prepareStatement(delPrefs)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                try (var ps = conn.prepareStatement(delCalendar)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                try (var ps = conn.prepareStatement(delPrivate)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }

                // Stergem antrenorul
                try (var ps = conn.prepareStatement(delCoach)) {
                    ps.setLong(1, id);
                    int affected = ps.executeUpdate();
                    conn.commit();
                    return affected > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Eroare la stergere coach: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // helper
    private Coach mapRow(ResultSet rs) throws SQLException {
        return new Coach(
                rs.getLong("id"),
                rs.getString("name"));
    }

    public Optional<Coach> findByUsername(String username) {
        String sql = "select id, username, password_hash, name from coach where club_id=? and username=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setString(2, username);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

                Coach coach = new Coach(
                        rs.getLong("id"),
                        rs.getString("name"));

                coach.setUsername(rs.getString("username"));
                coach.setPasswordHash(rs.getString("password_hash"));

                return Optional.of(coach);
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare findByUsername la coach", e);
        }
    }

    public void setLogin(long coachId, String username, String passwordHash) {
        String sql = "update coach set username=?, password_hash=? where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setLong(3, clubId);
            ps.setLong(4, coachId);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Eroare setLogin la coach", e);
        }
    }

}
