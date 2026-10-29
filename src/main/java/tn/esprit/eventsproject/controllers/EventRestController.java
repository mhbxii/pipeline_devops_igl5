package tn.esprit.eventsproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IEventServices;
import tn.esprit.eventsproject.controllers.dto.EventRequests.ParticipantCreateRequest;
import tn.esprit.eventsproject.controllers.dto.EventRequests.EventCreateRequest;
import tn.esprit.eventsproject.controllers.dto.EventRequests.LogisticsCreateRequest;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("event")
@RestController
public class EventRestController {
    private final IEventServices eventServices;

    @PostMapping("/addPart")
    public Participant addParticipant(@RequestBody ParticipantCreateRequest participant){
        return eventServices.addParticipant(participant.toEntity());
    }
    @PostMapping("/addEvent/{id}")
    public Event addEventPart(@RequestBody EventCreateRequest event, @PathVariable("id") int idPart){
        return eventServices.addAffectEvenParticipant(event.toEntity(), idPart);
    }
    @PostMapping("/addEvent")
    public Event addEvent(@RequestBody EventCreateRequest event){
        return eventServices.addAffectEvenParticipant(event.toEntity());
    }
    @PutMapping("/addAffectLog/{description}")
    public Logistics addAffectLog(@RequestBody LogisticsCreateRequest logistics,@PathVariable("description") String descriptionEvent){
        return eventServices.addAffectLog(logistics.toEntity(),descriptionEvent);
    }
    @GetMapping("/getLogs/{d1}/{d2}")
    public List<Logistics> getLogistiquesDates (@PathVariable("d1") LocalDate date_debut, @PathVariable("d2") LocalDate date_fin){
        return eventServices.getLogisticsDates(date_debut,date_fin);
    }
}
