package tn.esprit.eventsproject.controllers.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Scalar response fields and identifier-only relationships avoid entity cycles. */
public final class EventResponses {
    private EventResponses() { }

    @Getter @RequiredArgsConstructor
    public static class ParticipantResponse {
        private final int idPart;
        private final String nom;
        private final String prenom;
        private final Tache tache;
        private final Set<EventReference> events;

        public static ParticipantResponse from(Participant participant) {
            return new ParticipantResponse(participant.getIdPart(), participant.getNom(),
                participant.getPrenom(), participant.getTache(),
                references(participant.getEvents(), event -> new EventReference(event.getIdEvent())));
        }
    }

    @Getter @RequiredArgsConstructor
    public static class EventResponse {
        private final int idEvent;
        private final String description;
        private final LocalDate dateDebut;
        private final LocalDate dateFin;
        private final float cout;
        private final Set<ParticipantReference> participants;
        private final Set<LogisticsReference> logistics;

        public static EventResponse from(Event event) {
            return new EventResponse(event.getIdEvent(), event.getDescription(), event.getDateDebut(),
                event.getDateFin(), event.getCout(),
                references(event.getParticipants(), participant -> new ParticipantReference(participant.getIdPart())),
                references(event.getLogistics(), item -> new LogisticsReference(item.getIdLog())));
        }
    }

    @Getter @RequiredArgsConstructor
    public static class LogisticsResponse {
        private final int idLog;
        private final String description;
        private final boolean reserve;
        private final float prixUnit;
        private final int quantite;

        public static LogisticsResponse from(Logistics item) {
            return new LogisticsResponse(item.getIdLog(), item.getDescription(), item.isReserve(),
                item.getPrixUnit(), item.getQuantite());
        }
    }

    @Getter @RequiredArgsConstructor
    public static class EventReference { private final int idEvent; }

    @Getter @RequiredArgsConstructor
    public static class ParticipantReference { private final int idPart; }

    @Getter @RequiredArgsConstructor
    public static class LogisticsReference { private final int idLog; }

    private static <T, R> Set<R> references(Set<T> entities, Function<T, R> mapper) {
        return entities == null ? null : entities.stream().map(mapper).collect(Collectors.toSet());
    }
}
