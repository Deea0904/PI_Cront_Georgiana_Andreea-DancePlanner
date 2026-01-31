package com.example.danceplanner.service;

import com.example.danceplanner.jdbc.JdbcCoachAuthRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CoachAuthService {

    private final JdbcCoachAuthRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CoachAuthService(JdbcCoachAuthRepository repo) {
        this.repo = repo;
    }

    public Optional<Long> authenticate(String username, String rawPassword) {
        try {
            var accOpt = repo.findByUsername(username);
            if (accOpt.isEmpty()) return Optional.empty();

            var acc = accOpt.get();
            if (!encoder.matches(rawPassword, acc.passwordHash())) return Optional.empty();

            return Optional.of(acc.id());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean changePassword(long coachId, String oldPass, String newPass) {
        var opt = repo.findByIdForAuth(coachId);
        if (opt.isEmpty()) return false;

        var row = opt.get();
        if (!encoder.matches(oldPass, row.passwordHash())) return false;

        String newHash = encoder.encode(newPass);
        repo.updatePassword(coachId, newHash);
        return true;
    }
}
