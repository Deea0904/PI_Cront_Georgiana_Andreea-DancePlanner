package com.example.danceplanner.service;

import com.example.danceplanner.data.Coach;
import com.example.danceplanner.jdbc.JdbcCoachRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CoachService {

    private final JdbcCoachRepository coachRepository;

    public CoachService(JdbcCoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    public List<Coach> getAllCoaches() {
        return coachRepository.findAll();
    }

    public Optional<Coach> getCoachById(long id) {
        return coachRepository.findById(id);
    }

    /**
     * Creeaza coach si genereaza automat credentiale:
     * - username = numele normalizat
     * - parola initiala = username.id
     * - parola se salveaza ca BCrypt hash (nu se salveaza parola raw in DB)
     */
    public Coach createCoach(Coach req) {

        Coach c = new Coach();
        c.setId(0L);

        c.setName(req.getName());

        Coach saved = coachRepository.save(c);

        String username = normalize(saved.getName());

        String rawPassword = username + "_coach" + "." + saved.getId();

        String hash = new BCryptPasswordEncoder().encode(rawPassword);
        coachRepository.setLogin(saved.getId(), username, hash);

        saved.setUsername(username);

        System.out.println("LOGIN INIT pentru " + saved.getName() +
                " -> username=" + username + " parola=" + rawPassword);

        return saved;
    }

    public Optional<Coach> updateCoach(long id, Coach newData) {
        if (!coachRepository.exists(id)) {
            return Optional.empty();
        }

        newData.setId(id);
        coachRepository.update(newData);
        return coachRepository.findById(id);
    }

    // In CoachService.java

    public boolean deleteCoach(long id, Long replacementId) {
        // Verificam daca antrenorul exista inainte de a incerca stergerea
        if (!coachRepository.exists(id)) {
            return false;
        }

        // Trimitem ambii parametri catre Repository
        return coachRepository.delete(id, replacementId);
    }

    private String normalize(String name) {
        return name.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9]", "");
    }
}
