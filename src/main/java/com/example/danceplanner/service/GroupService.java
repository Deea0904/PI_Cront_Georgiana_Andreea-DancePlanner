package com.example.danceplanner.service;

import com.example.danceplanner.data.Group;
import com.example.danceplanner.jdbc.JdbcGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    private final JdbcGroupRepository groupRepository;

    public GroupService(JdbcGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    // ia toate grupele
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // ia o grupa dupa id
    public Optional<Group> getGroupById(long id) {
        return groupRepository.findById(id);
    }

    // creează o grupă nouă
    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    // update la o grupa existenta
    public Optional<Group> updateGroup(long id, Group newData) {
        if (!groupRepository.exists(id)) {
            return Optional.empty();
        }

        // ne asiguram ca updatam grupa cu id-ul corect
        newData.setId(id);
        groupRepository.update(newData);

        // citim din nou din DB grupa actualizata
        return groupRepository.findById(id);
    }

    // sterge o grupa
    // Sterge o grupa cu optiuni pentru dansatori
    public boolean deleteGroup(long id, Long reassignToId, boolean deleteDancers) {
        if (!groupRepository.exists(id)) {
            return false;
        }

        try {
            // Apelam metoda complexa din Repository care gestioneaza tranzactia
            groupRepository.deleteGroup(id, reassignToId, deleteDancers);
            return true;
        } catch (Exception e) {
            // Logam eroarea si returnam false daca stergerea a esuat (ex: FK constraint)
            throw new RuntimeException("Nu s-a putut șterge grupa: " + e.getMessage());
        }
    }
}
