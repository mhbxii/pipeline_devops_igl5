package tn.esprit.eventsproject.controllers.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tn.esprit.eventsproject.entities.Tache;

@Getter
@RequiredArgsConstructor
public class RoleResponse {
    private final int participantId;
    private final Tache tache;
}
