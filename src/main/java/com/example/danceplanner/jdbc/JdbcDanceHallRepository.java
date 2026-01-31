package com.example.danceplanner.jdbc;

import com.example.danceplanner.data.DanceHall;
import com.example.danceplanner.db.DB;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDanceHallRepository {

    private final long clubId = 1L;

    // CREARE
    public DanceHall save(DanceHall h) {
        String sql = "insert into dance_hall(club_id, name, capacity) values (?, ?, ?)";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, clubId);
            ps.setString(2, h.getName());
            ps.setInt(3, h.getCapacity());
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setId(rs.getLong(1));
                } else {
                    throw new SQLException("Nu s-a generat id pentru dance_hall");
                }
            }
            return h;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // CITIRE - toate
    public List<DanceHall> findAll() {
        String sql = "select id, name, capacity from dance_hall where club_id=? order by id";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);

            try (var rs = ps.executeQuery()) {
                List<DanceHall> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new DanceHall(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getInt("capacity")));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // CITIRE - dupa id
    public Optional<DanceHall> findById(long id) {
        String sql = "select id, name, capacity from dance_hall where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, id);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new DanceHall(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getInt("capacity")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // EXISTA
    public boolean exists(long id) {
        String sql = "select 1 from dance_hall where club_id=? and id=?";
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

    // ACTUALIZARE
    public void update(DanceHall h) {
        String sql = "update dance_hall set name=?, capacity=? where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setString(1, h.getName());
            ps.setInt(2, h.getCapacity());
            ps.setLong(3, clubId);
            ps.setLong(4, h.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // STERGERE
    public boolean delete(long id) {
        String sql = "delete from dance_hall where club_id=? and id=?";
        try (var c = DB.getConnection();
                var ps = c.prepareStatement(sql)) {

            ps.setLong(1, clubId);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
