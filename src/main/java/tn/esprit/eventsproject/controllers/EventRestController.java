package tn.esprit.eventsproject.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventsproject.dto.EventDTO;
import tn.esprit.eventsproject.dto.EventMapper;
import tn.esprit.eventsproject.dto.LogisticsDTO;
import tn.esprit.eventsproject.dto.ParticipantDTO;
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
    public ParticipantDTO addParticipant(@RequestBody ParticipantDTO participantDTO){
        Participant participant = EventMapper.toParticipantEntity(participantDTO);
        Participant saved = eventServices.addParticipant(participant);
        return EventMapper.toParticipantDTO(saved);
    }

    @PostMapping("/addEvent/{id}")
    public EventDTO addEventPart(@RequestBody EventDTO eventDTO, @PathVariable("id") int idPart){
        Event event = EventMapper.toEventEntity(eventDTO);
        Event saved = eventServices.addAffectEvenParticipant(event, idPart);
        return EventMapper.toEventDTO(saved);
    }

    @PostMapping("/addEvent")
    public EventDTO addEvent(@RequestBody EventDTO eventDTO){
        Event event = EventMapper.toEventEntity(eventDTO);
        Event saved = eventServices.addAffectEvenParticipant(event);
        return EventMapper.toEventDTO(saved);
    }

    @PutMapping("/addAffectLog/{description}")
    public LogisticsDTO addAffectLog(@RequestBody LogisticsDTO logisticsDTO, @PathVariable("description") String descriptionEvent){
        Logistics logistics = EventMapper.toLogisticsEntity(logisticsDTO);
        Logistics saved = eventServices.addAffectLog(logistics, descriptionEvent);
        return EventMapper.toLogisticsDTO(saved);
    }

    @GetMapping("/getLogs/{d1}/{d2}")
    public List<LogisticsDTO> getLogistiquesDates (@PathVariable("d1") LocalDate date_debut, @PathVariable("d2") LocalDate date_fin){
        List<Logistics> logistics = eventServices.getLogisticsDates(date_debut, date_fin);
        return EventMapper.toLogisticsDTOList(logistics);
    }

    // ==========================================
    // Event CRUD REST Endpoints
    // ==========================================

    @PostMapping("/saveEvent")
    public EventDTO saveEvent(@RequestBody EventDTO eventDTO) {
        log.info("REST request to save Event: {}", eventDTO != null ? eventDTO.getDescription() : null);
        Event event = EventMapper.toEventEntity(eventDTO);
        Event saved = eventServices.addEvent(event);
        return EventMapper.toEventDTO(saved);
    }

    @GetMapping("/getAllEvents")
    public List<EventDTO> getAllEvents() {
        log.info("REST request to get all Events");
        List<Event> events = eventServices.retrieveAllEvents();
        return EventMapper.toEventDTOList(events);
    }

    @GetMapping("/getEvent/{id}")
    public EventDTO getEventById(@PathVariable("id") int idEvent) {
        log.info("REST request to get Event by ID: {}", idEvent);
        Event event = eventServices.retrieveEvent(idEvent);
        return EventMapper.toEventDTO(event);
    }

    @PutMapping("/updateEvent")
    public EventDTO updateEvent(@RequestBody EventDTO eventDTO) {
        log.info("REST request to update Event ID: {}", eventDTO != null ? eventDTO.getIdEvent() : null);
        Event event = EventMapper.toEventEntity(eventDTO);
        Event updated = eventServices.updateEvent(event);
        return EventMapper.toEventDTO(updated);
    }

    @DeleteMapping("/deleteEvent/{id}")
    public void deleteEvent(@PathVariable("id") int idEvent) {
        log.info("REST request to delete Event ID: {}", idEvent);
        eventServices.deleteEvent(idEvent);
    }
}
