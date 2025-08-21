package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentTrendDTO {
    private Map<String, Integer> incidentsByDay;
    private Map<String, Integer> incidentsByWeek;
    private Map<String, Integer> incidentsByMonth;
    private Map<String, Integer> incidentsByLocation;
    private Map<String, Integer> incidentsByTrainType;
}
