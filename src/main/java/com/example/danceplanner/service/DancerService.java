package com.example.danceplanner.service;

import com.example.danceplanner.data.Dancer;
import com.example.danceplanner.jdbc.JdbcDancerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
import com.example.danceplanner.dto.CreateDancerRequest;
import com.example.danceplanner.dto.UpdateDancerRequest;

@Service
public class DancerService {

    private final JdbcDancerRepository dancerRepository;

    public DancerService(JdbcDancerRepository dancerRepository) {
        this.dancerRepository = dancerRepository;
    }

    public List<Dancer> getAllDancers() {
        return dancerRepository.findAll();
    }

    public Optional<Dancer> getDancerById(long id) {
        return dancerRepository.findById(id);
    }

    public Dancer createDancer(CreateDancerRequest req) {
        Dancer d = new Dancer();
        d.setId(0L);
        d.setName(req.getName());
        d.setLevelId(0L);
        d.setLevelName(req.getLevelName());
        d.setAge(req.getAge());

        Dancer saved = dancerRepository.saveByLevelName(d);

        String username = normalize(saved.getName());

        String rawPassword = username + "." + saved.getId();

        String hash = new BCryptPasswordEncoder().encode(rawPassword);
        dancerRepository.setLogin(saved.getId(), username, hash);

        saved.setUsername(username);

        System.out.println("LOGIN INIT pentru " + saved.getName() +
                " -> username=" + username + " parola=" + rawPassword);

        return saved;
    }

    public boolean deleteDancer(long id) {
        return dancerRepository.delete(id);
    }

    public Optional<Dancer> updateDancer(long id, UpdateDancerRequest req) {

        return dancerRepository.findById(id).map(d -> {

            if (req.getName() != null && !req.getName().isBlank()) {
                dancerRepository.rename(id, req.getName());
                d.setName(req.getName());
            }

            if (req.getLevelName() != null && !req.getLevelName().isBlank()) {
                dancerRepository.transferToLevelName(id, req.getLevelName());
                d.setLevelName(req.getLevelName());
            }

            return d;
        });
    }

    private String normalize(String name) {
        return name.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9]", "");
    }

}
