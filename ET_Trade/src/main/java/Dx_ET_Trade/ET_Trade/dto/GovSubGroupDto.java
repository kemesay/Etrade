package Dx_ET_Trade.ET_Trade.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GovSubGroupDto {
    @JsonProperty("Code")
    private Integer code;
    @JsonProperty("Description")
    private String description;
}
