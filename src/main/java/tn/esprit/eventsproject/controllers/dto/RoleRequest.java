package tn.esprit.eventsproject.controllers.dto;

import lombok.Getter;
import lombok.Setter;
import tn.esprit.eventsproject.entities.Tache;

/** Only the existing participant role is assignable through this endpoint. */
@Getter
@Setter
public class RoleRequest {
    private Tache tache;
}
