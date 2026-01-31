package com.example.danceplanner.controller;

import com.example.danceplanner.data.Group;
import com.example.danceplanner.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }


    @GetMapping
    public List<Group> getAll() {
        return groupService.getAllGroups();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Group> getById(@PathVariable long id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping
    public Group create(@RequestBody Group group) {
        return groupService.createGroup(group);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Group> update(
            @PathVariable long id,
            @RequestBody Group group
    ) {
        return groupService.updateGroup(id, group)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable long id,
            @RequestParam(required = false) Long reassignToId,
            @RequestParam(defaultValue = "false") boolean deleteDancers
    ) {
        try {
            groupService.deleteGroup(id, reassignToId, deleteDancers);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la procesarea stergerii: " + e.getMessage());
        }
    }
}
