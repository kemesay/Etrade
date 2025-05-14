package Dx_ET_Trade.ET_Trade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GovRegistrationInfoDto {
    @JsonProperty("Tin")
    private String tin;
    
    @JsonProperty("LegalCondtion")
    private String legalCondtion;
    
    @JsonProperty("RegNo")
    private String regNo;
    
    @JsonProperty("RegDate")
    private String regDate;
    
    @JsonProperty("BusinessName")
    private String businessName;
    
    @JsonProperty("BusinessNameAmh")
    private String businessNameAmh;
    
    @JsonProperty("PaidUpCapital")
    private Double paidUpCapital;
    
    @JsonProperty("AssociateShortInfos")
    private List<GovAssociateDto> associateShortInfos;
    
    @JsonProperty("Businesses")
    private List<GovBusinessDto> businesses;
}
