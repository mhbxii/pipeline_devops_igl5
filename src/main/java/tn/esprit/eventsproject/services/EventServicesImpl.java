package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Slf4j
@RequiredArgsConstructor
@Service
public class EventServicesImpl implements IEventServices{

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final LogisticsRepository logisticsRepository;

    @Override
    public Participant addParticipant(Participant participant) {
        log.info("Adding participant: {} {}", participant != null ? participant.getNom() : null, participant != null ? participant.getPrenom() : null);
        log.debug("Participant payload: {}", participant);
        return participantRepository.save(participant);
    }

    @Override
    public Event addAffectEvenParticipant(Event event, int idParticipant) {
        log.info("Affecting participant ID {} to event: {}", idParticipant, event != null ? event.getDescription() : null);
        Participant participant = participantRepository.findById(idParticipant).orElse(null);
        if (participant == null) {
            log.error("Participant not found with ID: {}", idParticipant);
            return null;
        }
        if (participant.getEvents() == null) {
            Set<Event> events = new HashSet<>();
            events.add(event);
            participant.setEvents(events);
        } else {
            participant.getEvents().add(event);
        }
        log.debug("Event before save: {}", event);
        return eventRepository.save(event);
    }

    @Override
    public Event addAffectEvenParticipant(Event event) {
        log.info("Affecting multiple participants to event: {}", event != null ? event.getDescription() : null);
        Set<Participant> participants = event != null ? event.getParticipants() : null;
        if (participants != null) {
            for (Participant aParticipant : participants) {
                Participant participant = participantRepository.findById(aParticipant.getIdPart()).orElse(null);
                if (participant != null) {
                    if (participant.getEvents() == null) {
                        Set<Event> events = new HashSet<>();
                        events.add(event);
                        participant.setEvents(events);
                    } else {
                        participant.getEvents().add(event);
                    }
                } else {
                    log.warn("Participant with ID {} does not exist in DB", aParticipant.getIdPart());
                }
            }
        }
        return eventRepository.save(event);
    }

    @Override
    public Logistics addAffectLog(Logistics logistics, String descriptionEvent) {
        log.info("Affecting logistics to event with description: {}", descriptionEvent);
        Event event = eventRepository.findByDescription(descriptionEvent);
        if (event == null) {
            log.error("Event not found with description: {}", descriptionEvent);
            return null;
        }
        if (event.getLogistics() == null) {
            Set<Logistics> logisticsSet = new HashSet<>();
            logisticsSet.add(logistics);
            event.setLogistics(logisticsSet);
            eventRepository.save(event);
        } else {
            event.getLogistics().add(logistics);
        }
        log.debug("Logistics to save: {}", logistics);
        return logisticsRepository.save(logistics);
    }

    @Override
    public List<Logistics> getLogisticsDates(LocalDate date_debut, LocalDate date_fin) {
        log.info("Retrieving logistics between dates: {} and {}", date_debut, date_fin);
        List<Event> events = eventRepository.findByDateDebutBetween(date_debut, date_fin);

        List<Logistics> logisticsList = new ArrayList<>();
        for (Event event : events) {
            if (event.getLogistics() == null || event.getLogistics().isEmpty()) {
                log.debug("No logistics found for event: {}", event.getDescription());
                return null;
            } else {
                Set<Logistics> logisticsSet = event.getLogistics();
                for (Logistics logistics : logisticsSet) {
                    if (logistics.isReserve()) {
                        logisticsList.add(logistics);
                    }
                }
            }
        }
        log.debug("Found {} reserved logistics", logisticsList.size());
        return logisticsList;
    }

    @Scheduled(cron = "*/60 * * * * *")
    @Override
    public void calculCout() {
        log.info("Running scheduled calculCout task");
        List<Event> events = eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed", Tache.ORGANISATEUR);
        float somme = 0f;
        for (Event event : events) {
            log.debug("Calculating cost for event: {}", event.getDescription());
            Set<Logistics> logisticsSet = event.getLogistics();
            if (logisticsSet != null) {
                for (Logistics logistics : logisticsSet) {
                    if (logistics.isReserve()) {
                        somme += logistics.getPrixUnit() * logistics.getQuantite();
                    }
                }
            }
            event.setCout(somme);
            eventRepository.save(event);
            log.info("Cout de l'Event {} est {}", event.getDescription(), somme);
        }
    }

    // ==========================================
    // Event CRUD Operations with Logging
    // ==========================================

    @Override
    public Event addEvent(Event event) {
        log.info("Executing addEvent for event: {}", event != null ? event.getDescription() : "null");
        if (event == null) {
            log.error("Attempted to add a null Event!");
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (event.getDateDebut() != null && event.getDateFin() != null && event.getDateDebut().isAfter(event.getDateFin())) {
            log.warn("Event dateDebut ({}) is after dateFin ({})", event.getDateDebut(), event.getDateFin());
        }
        log.debug("Event details before save: {}", event);
        Event saved = eventRepository.save(event);
        log.info("Event successfully created with ID: {}", saved.getIdEvent());
        return saved;
    }

    @Override
    public Event updateEvent(Event event) {
        log.info("Executing updateEvent for event ID: {}", event != null ? event.getIdEvent() : "null");
        if (event == null) {
            log.error("Attempted to update a null Event!");
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (!eventRepository.existsById(event.getIdEvent())) {
            log.warn("Event with ID {} does not exist in DB, will be inserted or fail", event.getIdEvent());
        }
        log.debug("Event details for update: {}", event);
        Event updated = eventRepository.save(event);
        log.info("Event with ID {} updated successfully", updated.getIdEvent());
        return updated;
    }

    @Override
    public Event retrieveEvent(int idEvent) {
        log.info("Executing retrieveEvent for ID: {}", idEvent);
        Event event = eventRepository.findById(idEvent).orElse(null);
        if (event == null) {
            log.warn("Event with ID {} not found in database", idEvent);
        } else {
            log.debug("Retrieved event details: {}", event);
        }
        return event;
    }

    @Override
    public List<Event> retrieveAllEvents() {
        log.info("Executing retrieveAllEvents");
        List<Event> events = eventRepository.findAll();
        log.debug("Total events retrieved: {}", events.size());
        return events;
    }

    @Override
    public void deleteEvent(int idEvent) {
        log.info("Executing deleteEvent for ID: {}", idEvent);
        if (!eventRepository.existsById(idEvent)) {
            log.warn("Cannot delete: Event with ID {} not found", idEvent);
        } else {
            eventRepository.deleteById(idEvent);
            log.info("Event with ID {} deleted successfully", idEvent);
        }
    }

}
