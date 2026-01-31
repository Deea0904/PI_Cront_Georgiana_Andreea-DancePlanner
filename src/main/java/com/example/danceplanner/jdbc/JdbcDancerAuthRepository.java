package com.example.danceplanner.jdbc;

import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcDancerAuthRepository {

    private final long clubId = 1L; // ACELASI club ca restul aplicatiei

    // ===== DTO intern pentru auth =====
    public record AuthRow(long id, String username, String passwordHash) {
    }

    // ===== cautare dupa username =====
    public Optional<AuthRow> findByUsername(String username) {
        String sql = """
                    select id, username, password_hash
                    from dancer
                    where club_id = ? and username = ?
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setString(2, username);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

                return Optional.of(new AuthRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare findByUsername", e);
        }
    }

    // ===== cautare id (pentru schimbare parola) =====
    public Optional<AuthRow> findByIdForAuth(long dancerId) {
        String sql = """
                    select id, username, password_hash
                    from dancer
                    where club_id = ? and id = ?
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, dancerId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

                return Optional.of(new AuthRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare findByIdForAuth", e);
        }
    }

    // ===== actualizare parola =====
    public void updatePassword(long dancerId, String newHash) {
        String sql = """
                    update dancer
                    set password_hash = ?
                    where club_id = ? and id = ?
                """;

        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setLong(2, clubId);
            ps.setLong(3, dancerId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Eroare updatePassword", e);
        }
    }
}
