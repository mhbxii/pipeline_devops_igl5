package tn.esprit.eventsproject.controllers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.services.TacheService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/taches")
public class TacheRestController {
    private final TacheService service;

    @GetMapping
    public Tache[] roles() { return Tache.values(); }

    @GetMapping("/participants/{id}")
    public RoleResponse get(@PathVariable int id) {
        return new RoleResponse(id, service.getRole(id));
    }

    @PutMapping("/participants/{id}")
    public RoleResponse assign(@PathVariable int id, @RequestBody RoleRequest request) {
        return new RoleResponse(id, service.assignRole(id, request.getTache()));
    }

    @DeleteMapping("/participants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable int id) { service.clearRole(id); }

    @Data
    public static class RoleRequest { private Tache tache; }

    @Data
    @RequiredArgsConstructor
    public static class RoleResponse {
        private final int participantId;
        private final Tache tache;
    }
}
