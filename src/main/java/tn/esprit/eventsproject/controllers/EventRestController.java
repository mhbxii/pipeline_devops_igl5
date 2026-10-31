package tn.esprit.eventsproject.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.services.IEventServices;
import tn.esprit.eventsproject.controllers.dto.EventRequests.ParticipantCreateRequest;
import tn.esprit.eventsproject.controllers.dto.EventRequests.EventCreateRequest;
import tn.esprit.eventsproject.controllers.dto.EventRequests.LogisticsCreateRequest;
import tn.esprit.eventsproject.controllers.dto.EventResponses.ParticipantResponse;
import tn.esprit.eventsproject.controllers.dto.EventResponses.EventResponse;
import tn.esprit.eventsproject.controllers.dto.EventResponses.LogisticsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("event")
@RestController
public class EventRestController {
    private final IEventServices eventServices;

    @PostMapping("/addPart")
    public ParticipantResponse addParticipant(@RequestBody ParticipantCreateRequest participant){
        return ParticipantResponse.from(eventServices.addParticipant(participant.toEntity()));
    }
    @PostMapping("/addEvent/{id}")
    public EventResponse addEventPart(@RequestBody EventCreateRequest event, @PathVariable("id") int idPart){
        return EventResponse.from(eventServices.addAffectEvenParticipant(event.toEntity(), idPart));
    }
    @PostMapping("/addEvent")
    public EventResponse addEvent(@RequestBody EventCreateRequest event){
        return EventResponse.from(eventServices.addAffectEvenParticipant(event.toEntity()));
    }
    @PutMapping("/addAffectLog/{description}")
    public LogisticsResponse addAffectLog(@RequestBody LogisticsCreateRequest logistics,@PathVariable("description") String descriptionEvent){
        return LogisticsResponse.from(eventServices.addAffectLog(logistics.toEntity(),descriptionEvent));
    }
    @GetMapping("/getLogs/{d1}/{d2}")
    public List<LogisticsResponse> getLogistiquesDates (@PathVariable("d1") LocalDate date_debut, @PathVariable("d2") LocalDate date_fin){
        List<Logistics> items = eventServices.getLogisticsDates(date_debut,date_fin);
        return items == null ? null : items.stream().map(LogisticsResponse::from).collect(Collectors.toList());
    }
}
