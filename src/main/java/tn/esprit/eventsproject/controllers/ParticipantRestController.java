package tn.esprit.eventsproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IParticipantServices;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("participant")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ParticipantRestController {

    private final IParticipantServices participantServices;

    @PostMapping
    public Participant add(@RequestBody Participant participant) {
        return participantServices.addParticipant(participant);
    }

    @GetMapping
    public List<Participant> getAll() {
        return participantServices.retrieveAllParticipants();
    }

    @GetMapping("/{id}")
    public Participant getById(@PathVariable int id) {
        return participantServices.retrieveParticipant(id);
    }

    @PutMapping
    public Participant update(@RequestBody Participant participant) {
        return participantServices.updateParticipant(participant);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        participantServices.removeParticipant(id);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}