package pe.fabiosalasm.uyhomefinder.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO: Make it immutable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SyncResultDTO {
    private Integer registriesAdded;
    private Integer registriesUpdated;
    private Integer registriesDeleted;
}
