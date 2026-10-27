package tn.esprit.eventsproject.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.services.IEventServices;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("event")
@RestController
public class EventRestController {
    private final IEventServices eventServices;

    @PostMapping("/addPart")
    public Participant addParticipant(@RequestBody Participant participant){
        return eventServices.addParticipant(participant);
    }
    @PostMapping("/addEvent/{id}")
    public Event addEventPart(@RequestBody Event event, @PathVariable("id") int idPart){
        return eventServices.addAffectEvenParticipant(event, idPart);
    }
    @PostMapping("/addEvent")
    public Event addEvent(@RequestBody Event event){
        return eventServices.addAffectEvenParticipant(event);
    }
    @PutMapping("/addAffectLog/{description}")
    public Logistics addAffectLog(@RequestBody Logistics logistics,@PathVariable("description") String descriptionEvent){
        return eventServices.addAffectLog(logistics,descriptionEvent);
    }
    @GetMapping("/getLogs/{d1}/{d2}")
    public List<Logistics> getLogistiquesDates (@PathVariable("d1") LocalDate date_debut, @PathVariable("d2") LocalDate date_fin){
        return eventServices.getLogisticsDates(date_debut,date_fin);
    }

    // ==========================================
    // Event CRUD REST Endpoints
    // ==========================================

    @PostMapping("/saveEvent")
    public Event saveEvent(@RequestBody Event event) {
        log.info("REST request to save Event: {}", event != null ? event.getDescription() : null);
        return eventServices.addEvent(event);
    }

    @GetMapping("/getAllEvents")
    public List<Event> getAllEvents() {
        log.info("REST request to get all Events");
        return eventServices.retrieveAllEvents();
    }

    @GetMapping("/getEvent/{id}")
    public Event getEventById(@PathVariable("id") int idEvent) {
        log.info("REST request to get Event by ID: {}", idEvent);
        return eventServices.retrieveEvent(idEvent);
    }

    @PutMapping("/updateEvent")
    public Event updateEvent(@RequestBody Event event) {
        log.info("REST request to update Event ID: {}", event != null ? event.getIdEvent() : null);
        return eventServices.updateEvent(event);
    }

    @DeleteMapping("/deleteEvent/{id}")
    public void deleteEvent(@PathVariable("id") int idEvent) {
        log.info("REST request to delete Event ID: {}", idEvent);
        eventServices.deleteEvent(idEvent);
    }
}
