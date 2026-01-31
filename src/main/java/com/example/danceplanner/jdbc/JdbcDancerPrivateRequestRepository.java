package com.example.danceplanner.jdbc;

import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcDancerPrivateRequestRepository {

    // club-ul curent (la tine e hardcodat in mai multe locuri)
    private final long clubId = 1L;

    // reprezinta o preferinta: "coachId + cate ore vreau cu el"
    public record Preference(long coachId, int hours) {}

    // reprezinta cererea completa pentru un dansator
    public record Request(
            long id,
            long dancerId,
            int totalHours,
            List<Preference> preferences
    ) {}

    /**
     * Returneaza cererea existenta pentru dancer (daca exista), altfel null.
     */
    public Request getByDancer(long dancerId) {
        String q = """
            select id, total_hours
            from dancer_private_request
            where club_id=? and dancer_id=?
            limit 1
        """;

        try (var c = DB.getConnection();
             var ps = c.prepareStatement(q)) {

            // ?1 = club_id
            ps.setLong(1, clubId);
            // ?2 = dancer_id
            ps.setLong(2, dancerId);

            try (var rs = ps.executeQuery()) {
                // daca nu exista rand -> null
                if (!rs.next()) return null;

                long id = rs.getLong("id");
                int total = rs.getInt("total_hours");

                // luam preferintele din tabelul copil
                List<Preference> prefs = findPreferences(id);

                return new Request(id, dancerId, total, prefs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creeaza sau actualizeaza cererea dansatorului.
     * - daca exista deja, o updateaza (si rescrie preferintele)
     * - daca nu exista, o insereaza
     */
    public Request upsert(long dancerId, int totalHours, List<Preference> prefs) {
        // validari simple
        if (totalHours <= 0) throw new IllegalArgumentException("totalHours trebuie > 0");
        if (prefs == null) prefs = List.of();

        // suma preferintelor trebuie sa fie egala cu totalHours
        int sum = prefs.stream().mapToInt(Preference::hours).sum();
        if (sum != totalHours) {
            throw new IllegalArgumentException("Suma preferintelor trebuie sa fie egala cu totalHours.");
        }

        // optional: nu permite acelasi coach de doua ori
        long distinct = prefs.stream().map(Preference::coachId).distinct().count();
        if (distinct != prefs.size()) {
            throw new IllegalArgumentException("Nu pune acelasi coach de doua ori in preferinte.");
        }

        try {
            // cautam daca exista deja request pentru (clubId, dancerId)
            Long existingId = findRequestId(dancerId);

            if (existingId == null) {
                // nu exista -> insert
                long reqId = insertRequest(dancerId, totalHours);
                insertPreferences(reqId, prefs);
                return new Request(reqId, dancerId, totalHours, prefs);
            } else {
                // exista -> update
                updateRequest(existingId, totalHours);

                // rescriem preferintele: delete + insert
                deletePreferences(existingId);
                insertPreferences(existingId, prefs);

                return new Request(existingId, dancerId, totalHours, prefs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------- Helpers private -----------------

    /**
     * Returneaza id-ul cererii daca exista, altfel null.
     */
    private Long findRequestId(long dancerId) throws SQLException {
        String q = """
            select id
            from dancer_private_request
            where club_id=? and dancer_id=?
            limit 1
        """;

        try (var c = DB.getConnection();
             var ps = c.prepareStatement(q)) {

            ps.setLong(1, clubId);
            ps.setLong(2, dancerId);

            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    /**
     * Insereaza randul din tabela parinte (dancer_private_request) si returneaza id-ul generat.
     */
    private long insertRequest(long dancerId, int totalHours) throws SQLException {
        String sql = """
            insert into dancer_private_request (club_id, dancer_id, total_hours)
            values (?, ?, ?)
        """;

        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, clubId);
            ps.setLong(2, dancerId);
            ps.setInt(3, totalHours);

            ps.executeUpdate();

            // luam cheia generata (id)
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /**
     * Update doar pentru total_hours (randul parinte).
     */
    private void updateRequest(long requestId, int totalHours) throws SQLException {
        String sql = "update dancer_private_request set total_hours=? where club_id=? and id=?";

        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, totalHours);
            ps.setLong(2, clubId);
            ps.setLong(3, requestId);

            ps.executeUpdate();
        }
    }

    /**
     * Sterge toate preferintele pentru un request (inainte sa le rescriem).
     */
    private void deletePreferences(long requestId) throws SQLException {
        String sql = "delete from dancer_private_request_preference where request_id=?";

        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setLong(1, requestId);
            ps.executeUpdate();
        }
    }

    /**
     * Insereaza toate preferintele (batch) pentru request.
     */
    private void insertPreferences(long requestId, List<Preference> prefs) throws SQLException {
        String sql = """
            insert into dancer_private_request_preference (request_id, coach_id, hours)
            values (?, ?, ?)
        """;

        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql)) {

            for (Preference p : prefs) {
                if (p.hours() <= 0) throw new IllegalArgumentException("hours trebuie > 0");

                ps.setLong(1, requestId);
                ps.setLong(2, p.coachId());
                ps.setInt(3, p.hours());

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    /**
     * Citeste preferintele existente pentru un request.
     */
    private List<Preference> findPreferences(long requestId) throws SQLException {
        String sql = """
            select coach_id, hours
            from dancer_private_request_preference
            where request_id=?
            order by coach_id
        """;

        try (var c = DB.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setLong(1, requestId);

            try (var rs = ps.executeQuery()) {
                List<Preference> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Preference(
                            rs.getLong("coach_id"),
                            rs.getInt("hours")
                    ));
                }
                return out;
            }
        }
    }
}
