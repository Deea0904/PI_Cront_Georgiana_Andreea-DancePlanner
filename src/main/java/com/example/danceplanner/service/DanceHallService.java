package com.example.danceplanner.service;

import com.example.danceplanner.data.DanceHall;
import com.example.danceplanner.jdbc.JdbcDanceHallRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DanceHallService {

    private final JdbcDanceHallRepository hallRepository;

    public DanceHallService(JdbcDanceHallRepository hallRepository) {
        this.hallRepository = hallRepository;
    }

    // ia toate salile
    public List<DanceHall> getAllHalls() {
        return hallRepository.findAll();
    }

    // ia o sala dupa id
    public Optional<DanceHall> getHallById(long id) {
        return hallRepository.findById(id);
    }

    // creează o sală nouă
    public DanceHall createHall(DanceHall hall) {
        return hallRepository.save(hall);
    }

    // update la o sala existenta
    public Optional<DanceHall> updateHall(long id, DanceHall newData) {
        if (!hallRepository.exists(id)) {
            return Optional.empty();
        }

        newData.setId(id); // ne asiguram ca updatam sala cu id-ul corect
        hallRepository.update(newData);

        // citim din nou din DB varianta actualizata
        return hallRepository.findById(id);
    }

    // sterge o sala
    public boolean deleteHall(long id) {
        return hallRepository.delete(id);
    }
}
