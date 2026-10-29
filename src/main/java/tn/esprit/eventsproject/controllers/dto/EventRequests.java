package tn.esprit.eventsproject.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Explicit create-field allowlists; nested relationships accept identifiers only. */
public final class EventRequests {
    private EventRequests() { }

    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParticipantCreateRequest {
        private String nom;
        private String prenom;
        private Tache tache;
        private Set<EventReference> events;

        public Participant toEntity() {
            Participant participant = new Participant();
            participant.setNom(nom);
            participant.setPrenom(prenom);
            participant.setTache(tache);
            participant.setEvents(mapReferences(events, reference -> {
                Event event = new Event();
                event.setIdEvent(positiveId(reference.getIdEvent()));
                return event;
            }));
            return participant;
        }
    }

    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventCreateRequest {
        private String description;
        private LocalDate dateDebut;
        private LocalDate dateFin;
        private Set<ParticipantReference> participants;
        private Set<LogisticsReference> logistics;

        public Event toEntity() {
            Event event = new Event();
            event.setDescription(description);
            event.setDateDebut(dateDebut);
            event.setDateFin(dateFin);
            event.setParticipants(mapReferences(participants, reference -> {
                Participant participant = new Participant();
                participant.setIdPart(positiveId(reference.getIdPart()));
                return participant;
            }));
            event.setLogistics(mapReferences(logistics, reference -> {
                Logistics item = new Logistics();
                item.setIdLog(positiveId(reference.getIdLog()));
                return item;
            }));
            return event;
        }
    }

    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogisticsCreateRequest {
        private String description;
        private boolean reserve;
        private float prixUnit;
        private int quantite;

        public Logistics toEntity() {
            Logistics item = new Logistics();
            item.setDescription(description);
            item.setReserve(reserve);
            item.setPrixUnit(prixUnit);
            item.setQuantite(quantite);
            return item;
        }
    }

    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventReference { private int idEvent; }

    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParticipantReference { private int idPart; }

    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogisticsReference { private int idLog; }

    private static int positiveId(int id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Relationship identifiers must be positive");
        }
        return id;
    }

    private static <T, R> Set<R> mapReferences(Set<T> references, Function<T, R> mapper) {
        if (references == null) { return new HashSet<>(); }
        return references.stream().map(reference -> {
            if (reference == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Relationship references cannot be null");
            }
            return mapper.apply(reference);
        }).collect(Collectors.toSet());
    }
}
