package tn.esprit.eventsproject.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    int idLog;
    String description;
    boolean reserve;
    float prixUnit;
    int quantite;
}
