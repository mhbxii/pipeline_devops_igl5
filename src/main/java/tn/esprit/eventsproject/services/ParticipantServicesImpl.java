package tn.esprit.eventsproject.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipantServicesImpl implements IParticipantServices {

    private final ParticipantRepository participantRepository;

    @Override
    public Participant addParticipant(Participant participant) {
        log.info("Adding participant {} {}", participant.getNom(), participant.getPrenom());
        Participant saved = participantRepository.save(participant);
        log.debug("Participant saved with id {}", saved.getIdPart());
        return saved;
    }

    @Override
    public List<Participant> retrieveAllParticipants() {
        log.info("Retrieving all participants");
        List<Participant> participants = new ArrayList<>();
        participantRepository.findAll().forEach(participants::add);
        log.debug("{} participants found", participants.size());
        return participants;
    }

    @Override
    public Participant retrieveParticipant(int idPart) {
        log.info("Retrieving participant {}", idPart);
        return participantRepository.findById(idPart).orElseThrow(() -> {
            log.error("Participant {} not found", idPart);
            return new EntityNotFoundException("Participant " + idPart + " not found");
        });
    }

    @Override
    public Participant updateParticipant(Participant participant) {
        log.info("Updating participant {}", participant.getIdPart());
        if (!participantRepository.existsById(participant.getIdPart())) {
            log.error("Cannot update: participant {} not found", participant.getIdPart());
            throw new EntityNotFoundException("Participant " + participant.getIdPart() + " not found");
        }
        return participantRepository.save(participant);
    }

    @Override
    public void removeParticipant(int idPart) {
        log.info("Removing participant {}", idPart);
        if (!participantRepository.existsById(idPart)) {
            log.warn("Cannot remove: participant {} not found", idPart);
            throw new EntityNotFoundException("Participant " + idPart + " not found");
        }
        participantRepository.deleteById(idPart);
        log.debug("Participant {} removed", idPart);
    }
}