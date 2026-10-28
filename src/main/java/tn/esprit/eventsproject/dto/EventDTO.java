package tn.esprit.eventsproject.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    int idEvent;
    String description;
    LocalDate dateDebut;
    LocalDate dateFin;
    float cout;
    Set<ParticipantDTO> participants;
    Set<LogisticsDTO> logistics;
}
