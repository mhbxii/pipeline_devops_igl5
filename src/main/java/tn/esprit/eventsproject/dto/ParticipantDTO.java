package tn.esprit.eventsproject.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tn.esprit.eventsproject.entities.Tache;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipantDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    int idPart;
    String nom;
    String prenom;
    Tache tache;
}
