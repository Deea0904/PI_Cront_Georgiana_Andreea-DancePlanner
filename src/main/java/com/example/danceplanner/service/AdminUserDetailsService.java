package com.example.danceplanner.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbc;

    public AdminUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String sql = "select username, password_hash, role from admin_user where username = ? limit 1";

        return jdbc.query(sql, rs -> {
            if (!rs.next()) throw new UsernameNotFoundException("Admin not found");

            String u = rs.getString("username");
            String hash = rs.getString("password_hash");
            String role = rs.getString("role"); // ex: ADMIN

            // Spring Security vrea ROLE_ADMIN
            var auth = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

            return new org.springframework.security.core.userdetails.User(u, hash, auth);
        }, username);
    }
}
