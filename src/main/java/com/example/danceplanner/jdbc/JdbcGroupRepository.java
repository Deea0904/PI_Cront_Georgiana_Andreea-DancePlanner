package com.example.danceplanner.jdbc;

import com.example.danceplanner.data.Group;
import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcGroupRepository {

    private final long clubId;

    public JdbcGroupRepository() {
        this.clubId = 1L;
    }

    public JdbcGroupRepository(long clubId) {
        this.clubId = clubId;
    }

    // -------------------- CREARE --------------------

    public Group create(String name, String description) {
        String sql = "insert into group_level(club_id, name, description) values (?,?,?)";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, clubId);
            ps.setString(2, name);
            if (description == null || description.isBlank()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, description);
            }

            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new Group(id, name, description);
                } else {
                    throw new SQLException("Nu s-a generat id pentru group_level");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Group save(Group group) {
        return create(group.getName(), group.getDescription());
    }

    // -------------------- CITIRE --------------------

    public List<Group> findAll() {
        String sql = "select id, name, description from group_level where club_id=? order by id";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            try (var rs = ps.executeQuery()) {
                var out = new ArrayList<Group>();
                while (rs.next()) {
                    out.add(new Group(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description")));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Group> findById(long id) {
        String sql = "select id, name, description from group_level where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Group(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description")));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsByName(String name) {
        String sql = "select 1 from group_level where club_id=? and name=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setString(2, name);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(long id) {
        String sql = "select 1 from group_level where club_id=? and id=?";
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

    // -------------------- ACTUALIZARE --------------------

    public void rename(long id, String newName) {
        String sql = "update group_level set name=? where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setLong(2, clubId);
            ps.setLong(3, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Group not found: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDescription(long id, String newDesc) {
        String sql = "update group_level set description=? where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            if (newDesc == null || newDesc.isBlank()) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, newDesc);
            }
            ps.setLong(2, clubId);
            ps.setLong(3, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Group not found: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // folosita de GroupService: update complet (nume + descriere)
    public void update(Group group) {
        rename(group.getId(), group.getName());
        updateDescription(group.getId(), group.getDescription());
    }

    // -------------------- STERGERE --------------------

    public void deleteGroup(long groupId, Long reassignToId, boolean deleteDancers) throws SQLException {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String delTimetable = "DELETE FROM timetable_entry WHERE group_level_id = ?";
                try (var ps = conn.prepareStatement(delTimetable)) {
                    ps.setLong(1, groupId);
                    ps.executeUpdate();
                }

                if (deleteDancers) {
                    String sql = "DELETE FROM dancer WHERE level_id = ?";
                    try (var ps = conn.prepareStatement(sql)) {
                        ps.setLong(1, groupId);
                        ps.executeUpdate();
                    }
                } else if (reassignToId != null) {
                    String sql = "UPDATE dancer SET level_id = ? WHERE level_id = ?";
                    try (var ps = conn.prepareStatement(sql)) {
                        ps.setLong(1, reassignToId);
                        ps.setLong(2, groupId);
                        ps.executeUpdate();
                    }
                }

                String deleteGroupSql = "DELETE FROM group_level WHERE id = ?";
                try (var ps = conn.prepareStatement(deleteGroupSql)) {
                    ps.setLong(1, groupId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
