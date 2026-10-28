package tn.esprit.eventsproject.dto;

import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventMapper {

    private EventMapper() {
        // Utility class
    }

    // Participant mapping
    public static Participant toParticipantEntity(ParticipantDTO dto) {
        if (dto == null) {
            return null;
        }
        Participant participant = new Participant();
        participant.setIdPart(dto.getIdPart());
        participant.setNom(dto.getNom());
        participant.setPrenom(dto.getPrenom());
        participant.setTache(dto.getTache());
        return participant;
    }

    public static ParticipantDTO toParticipantDTO(Participant entity) {
        if (entity == null) {
            return null;
        }
        return ParticipantDTO.builder()
                .idPart(entity.getIdPart())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .tache(entity.getTache())
                .build();
    }

    // Logistics mapping
    public static Logistics toLogisticsEntity(LogisticsDTO dto) {
        if (dto == null) {
            return null;
        }
        Logistics logistics = new Logistics();
        logistics.setIdLog(dto.getIdLog());
        logistics.setDescription(dto.getDescription());
        logistics.setReserve(dto.isReserve());
        logistics.setPrixUnit(dto.getPrixUnit());
        logistics.setQuantite(dto.getQuantite());
        return logistics;
    }

    public static LogisticsDTO toLogisticsDTO(Logistics entity) {
        if (entity == null) {
            return null;
        }
        return LogisticsDTO.builder()
                .idLog(entity.getIdLog())
                .description(entity.getDescription())
                .reserve(entity.isReserve())
                .prixUnit(entity.getPrixUnit())
                .quantite(entity.getQuantite())
                .build();
    }

    public static List<LogisticsDTO> toLogisticsDTOList(List<Logistics> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(EventMapper::toLogisticsDTO).collect(Collectors.toList());
    }

    // Event mapping
    public static Event toEventEntity(EventDTO dto) {
        if (dto == null) {
            return null;
        }
        Event event = new Event();
        event.setIdEvent(dto.getIdEvent());
        event.setDescription(dto.getDescription());
        event.setDateDebut(dto.getDateDebut());
        event.setDateFin(dto.getDateFin());
        event.setCout(dto.getCout());
        if (dto.getParticipants() != null) {
            Set<Participant> participants = dto.getParticipants().stream()
                    .map(EventMapper::toParticipantEntity)
                    .collect(Collectors.toSet());
            event.setParticipants(participants);
        }
        if (dto.getLogistics() != null) {
            Set<Logistics> logistics = dto.getLogistics().stream()
                    .map(EventMapper::toLogisticsEntity)
                    .collect(Collectors.toSet());
            event.setLogistics(logistics);
        }
        return event;
    }

    public static EventDTO toEventDTO(Event entity) {
        if (entity == null) {
            return null;
        }
        Set<ParticipantDTO> participants = null;
        if (entity.getParticipants() != null) {
            participants = entity.getParticipants().stream()
                    .map(EventMapper::toParticipantDTO)
                    .collect(Collectors.toSet());
        }
        Set<LogisticsDTO> logistics = null;
        if (entity.getLogistics() != null) {
            logistics = entity.getLogistics().stream()
                    .map(EventMapper::toLogisticsDTO)
                    .collect(Collectors.toSet());
        }
        return EventDTO.builder()
                .idEvent(entity.getIdEvent())
                .description(entity.getDescription())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .cout(entity.getCout())
                .participants(participants)
                .logistics(logistics)
                .build();
    }

    public static List<EventDTO> toEventDTOList(List<Event> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(EventMapper::toEventDTO).collect(Collectors.toList());
    }
}
