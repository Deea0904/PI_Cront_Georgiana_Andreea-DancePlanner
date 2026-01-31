package com.example.danceplanner.jdbc;

import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcCoachAuthRepository {

    private final long clubId = 1L;

    // record similar cu ce ai la dancer
    public record AuthRow(long id, String username, String passwordHash) {}

    public Optional<AuthRow> findByUsername(String username) {
        String sql = "select id, username, password_hash from coach where club_id=? and username=?";
        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setString(2, username);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new AuthRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare findByUsername la coach auth", e);
        }
    }

    public Optional<AuthRow> findByIdForAuth(long coachId) {
        String sql = "select id, username, password_hash from coach where club_id=? and id=?";
        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, coachId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new AuthRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare findByIdForAuth la coach auth", e);
        }
    }

    public void updatePassword(long coachId, String newHash) {
        String sql = "update coach set password_hash=? where club_id=? and id=?";
        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setLong(2, clubId);
            ps.setLong(3, coachId);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Eroare updatePassword la coach auth", e);
        }
    }
}
