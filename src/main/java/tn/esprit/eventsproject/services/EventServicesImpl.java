package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
        return participantRepository.save(participant);
    }

    @Override
    public Event addAffectEvenParticipant(Event event, int idParticipant) {
        Participant participant = requireParticipant(idParticipant);
        if(participant.getEvents() == null){
            Set<Event> events = new HashSet<>();
            events.add(event);
            participant.setEvents(events);
        }else {
            participant.getEvents().add(event);
        }
        return eventRepository.save(event);
    }

    @Override
    public Event addAffectEvenParticipant(Event event) {
        Set<Participant> participants = event.getParticipants();
        for(Participant aParticipant:participants){
            Participant participant = requireParticipant(aParticipant.getIdPart());
            if(participant.getEvents() == null){
                Set<Event> events = new HashSet<>();
                events.add(event);
                participant.setEvents(events);
            }else {
                participant.getEvents().add(event);
            }
        }
        return eventRepository.save(event);
    }

    @Override
    public Logistics addAffectLog(Logistics logistics, String descriptionEvent) {
      Event event = eventRepository.findByDescription(descriptionEvent);
      if (event == null) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
      }
      if(event.getLogistics() == null){
          Set<Logistics> logisticsSet = new HashSet<>();
          logisticsSet.add(logistics);
          event.setLogistics(logisticsSet);
          eventRepository.save(event);
      }
      else{
          event.getLogistics().add(logistics);
      }
        return logisticsRepository.save(logistics);
    }

    @Override
    public List<Logistics> getLogisticsDates(LocalDate date_debut, LocalDate date_fin) {
        List<Event> events = eventRepository.findByDateDebutBetween(date_debut, date_fin);

        List<Logistics> logisticsList = new ArrayList<>();
        for (Event event:events){
            if(event.getLogistics().isEmpty()){

                return null;
            }

            else {
                Set<Logistics> logisticsSet = event.getLogistics();
                for (Logistics logistics:logisticsSet){
                    if(logistics.isReserve())
                        logisticsList.add(logistics);
                }
            }
        }
        return logisticsList;
    }

    private Participant requireParticipant(int id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participant id must be positive");
        }
        return participantRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
    }

    @Scheduled(cron = "*/60 * * * * *")
    @Override
    public void calculCout() {
        List<Event> events = eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi","Ahmed", Tache.ORGANISATEUR);
    // eventRepository.findAll();
        float somme = 0f;
        for(Event event:events){
            log.info(event.getDescription());
            Set<Logistics> logisticsSet = event.getLogistics();
            for (Logistics logistics:logisticsSet){
                if(logistics.isReserve())
                    somme+=logistics.getPrixUnit()*logistics.getQuantite();
            }
            event.setCout(somme);
            eventRepository.save(event);
            log.info("Cout de l'Event "+event.getDescription()+" est "+ somme);

        }
    }

}
