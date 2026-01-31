package com.example.danceplanner.jdbc;

import com.example.danceplanner.data.Dancer;
import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDancerRepository {
    private final long clubId = 1L; // Deocamdata hardcodat

    public JdbcDancerRepository() {
    }

    private long ensureGroupLevel(String name) throws SQLException {
        String q = "select id from group_level where club_id=? and name=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(q)) {

            ps.setLong(1, clubId);
            ps.setString(2, name);

            try (var rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getLong(1);
            }
        }

        String ins = "insert into group_level(club_id, name) values (?, ?)";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, clubId);
            ps.setString(2, name);
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getLong(1);
                throw new SQLException("Nu s-a generat id pentru group_level");
            }
        }
    }

    private long resolveLevelId(Dancer d) throws SQLException {
        if (d.getLevelId() > 0)
            return d.getLevelId(); // 0 =
        String name = (d.getLevelName() != null && !d.getLevelName().isBlank())
                ? d.getLevelName()
                : "Beginners";
        return ensureGroupLevel(name);
    }

    public Dancer saveByLevelName(Dancer d) {
        try {
            long levelId = resolveLevelId(d);

            if (d.getId() == 0) {
                // INSERARE
                String sql = "insert into dancer(club_id, name, age, level_id) values (?, ?, ?, ?)";
                try (var c = DB.getConnection();
                        var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setLong(1, clubId);
                    ps.setString(2, d.getName());

                    if (d.getAge() == null)
                        ps.setNull(3, java.sql.Types.INTEGER);
                    else
                        ps.setInt(3, d.getAge());

                    ps.setLong(4, levelId);

                    ps.executeUpdate();
                    try (var rs = ps.getGeneratedKeys()) {
                        if (rs.next())
                            d.setId(rs.getLong(1));
                    }
                }

            } else {
                // ACTUALIZARE
                String sql = "update dancer set name=?, age=?, level_id=? where id=? and club_id=?";
                try (var c = DB.getConnection();
                        var ps = c.prepareStatement(sql)) {

                    ps.setString(1, d.getName());

                    if (d.getAge() == null)
                        ps.setNull(2, java.sql.Types.INTEGER);
                    else
                        ps.setInt(2, d.getAge());

                    ps.setLong(3, levelId);
                    ps.setLong(4, d.getId());
                    ps.setLong(5, clubId);

                    ps.executeUpdate();
                }

            }

            d.setLevelId(levelId);
            return d;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- citire / interogare ----------

    public List<Dancer> findAll() {
        String sql = """
                  select d.id, d.name, d.age, gl.id as gl_id, gl.name as level_name
                  from dancer d
                  join group_level gl on gl.id = d.level_id
                  where d.club_id = ? and gl.club_id = ?
                  order by d.id
                """;
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, clubId);

            try (var rs = ps.executeQuery()) {
                List<Dancer> out = new ArrayList<>();
                while (rs.next())
                    out.add(mapRow(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(long id) {
        String sql = "select 1 from dancer where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, id);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Dancer> findById(long id) {
        String sql = """
                  select d.id, d.name, d.age, gl.id as gl_id, gl.name as level_name
                  from dancer d
                  join group_level gl on gl.id = d.level_id
                  where d.club_id = ? and gl.club_id = ? and d.id = ?
                """;
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, clubId);
            ps.setLong(3, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(long id) {
        String sql = "delete from dancer where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dancer> findByLevelName(String levelName) {
        String sql = """
                  select d.id, d.name, d.age, gl.id as gl_id, gl.name as level_name
                  from dancer d
                  join group_level gl on gl.id = d.level_id
                  where d.club_id=? and gl.club_id=? and gl.name LIKE ?
                  order by d.id
                """;
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, clubId);
            ps.setString(3, levelName); // sau "%"+levelName+"%" pentru cautare partiala

            try (var rs = ps.executeQuery()) {
                List<Dancer> out = new ArrayList<>();
                while (rs.next())
                    out.add(mapRow(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dancer> searchByName(String q, int limit, int offset) {
        String sql = """
                  select d.id, d.name, d.age, gl.id as gl_id, gl.name as level_name
                  from dancer d
                  join group_level gl on gl.id = d.level_id
                  where d.club_id=? and gl.club_id=? and d.name LIKE ?
                  order by d.id
                  limit ? offset ?
                """;
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, clubId);
            ps.setString(3, "%" + q + "%");
            ps.setInt(4, limit);
            ps.setInt(5, offset);

            try (var rs = ps.executeQuery()) {
                List<Dancer> out = new ArrayList<>();
                while (rs.next())
                    out.add(mapRow(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long count() {
        String sql = "select count(*) from dancer where club_id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean rename(long id, String newName) {
        String sql = "update dancer set name=? where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setLong(2, clubId);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean transferToLevelName(long dancerId, String newLevelName) {
        try {
            long newLevelId = ensureGroupLevel(newLevelName);
            String sql = "update dancer set level_id=? where club_id=? and id=?";
            try (var c = DB.getConnection();
                    var ps = c.prepareStatement(sql)) {

                ps.setLong(1, newLevelId);
                ps.setLong(2, clubId);
                ps.setLong(3, dancerId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ------- helper: mapare ResultSet -> Dancer --------
    private Dancer mapRow(ResultSet rs) throws SQLException {
        Dancer d = new Dancer();

        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));

        int age = rs.getInt("age");
        d.setAge(rs.wasNull() ? null : age);

        d.setLevelId(rs.getLong("gl_id"));
        d.setLevelName(rs.getString("level_name"));
        return d;
    }

    public Optional<Dancer> findByUsername(String username) {
        String sql = "select id, username, password_hash from dancer where club_id=? and username=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setString(2, username);

            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();

                Dancer d = new Dancer();
                d.setId(rs.getLong("id"));
                d.setUsername(rs.getString("username"));
                d.setPasswordHash(rs.getString("password_hash"));

                return Optional.of(d);
            }
        } catch (Exception e) {
            throw new RuntimeException("Eroare findByUsername la dancer", e);
        }
    }

    public void setLogin(long dancerId, String username, String passwordHash) {
        String sql = "update dancer set username=?, password_hash=? where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setLong(3, clubId);
            ps.setLong(4, dancerId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Eroare setLogin", e);
        }
    }

}
