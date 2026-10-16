package tn.esprit.eventsproject.services;

import tn.esprit.eventsproject.entities.Participant;
import java.util.List;

public interface IParticipantServices {
    Participant addParticipant(Participant participant);
    List<Participant> retrieveAllParticipants();
    Participant retrieveParticipant(int idPart);
    Participant updateParticipant(Participant participant);
    void removeParticipant(int idPart);
}