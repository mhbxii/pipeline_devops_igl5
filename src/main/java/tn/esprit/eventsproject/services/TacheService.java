package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import javax.persistence.EntityNotFoundException;

/** Assigns existing enum roles; never creates or deletes participants. */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TacheService {
    private final ParticipantRepository participants;

    @Transactional(readOnly = true)
    public Tache getRole(int participantId) {
        log.debug("Reading role participantId={}", participantId);
        return findParticipant(participantId).getTache();
    }

    public Tache assignRole(int participantId, Tache role) {
        if (role == null) {
            log.warn("Role validation failed participantId={} reason=missing_role", participantId);
            throw new IllegalArgumentException("A role is required");
        }
        Participant participant = findParticipant(participantId);
        Tache previous = participant.getTache();
        participant.setTache(role);
        participants.save(participant);
        log.info("Role assigned participantId={} previous={} role={}", participantId, previous, role);
        return role;
    }

    public void clearRole(int participantId) {
        Participant participant = findParticipant(participantId);
        participant.setTache(null);
        participants.save(participant);
        log.info("Role cleared participantId={}", participantId);
    }

    private Participant findParticipant(int participantId) {
        if (participantId <= 0) {
            log.warn("Role validation failed participantId={} reason=invalid_id", participantId);
            throw new IllegalArgumentException("Participant id must be positive");
        }
        return participants.findById(participantId).orElseThrow(() -> {
            log.warn("Role operation rejected participantId={} reason=participant_not_found", participantId);
            return new EntityNotFoundException("Participant not found");
        });
    }
}
