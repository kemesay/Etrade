package Dx_ET_Trade.ET_Trade.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GovBusinessGroupDto {
    @JsonProperty("MainGuid")
    private String mainGuid;
    @JsonProperty("BusinessMainGuid")
    private String businessMainGuid;
    @JsonProperty("MajorDivision")
    private Integer majorDivision;
    @JsonProperty("Division")
    private Integer division;
    @JsonProperty("MajorGroup")
    private Integer majorGroup;
    @JsonProperty("BGroup")
    private Integer bGroup;
    @JsonProperty("SubGroup")
    private Integer subGroup;
    @JsonProperty("Tinc")
    private String tinc;
}
