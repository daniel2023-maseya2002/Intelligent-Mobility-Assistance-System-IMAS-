package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperatorDTO {
    private Long operatorId;
    private String operatorName;
    private String teamName;
    private String position;
}