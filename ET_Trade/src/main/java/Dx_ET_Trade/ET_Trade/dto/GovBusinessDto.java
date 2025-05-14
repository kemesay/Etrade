package Dx_ET_Trade.ET_Trade.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class GovBusinessDto {
    @JsonProperty("MainGuid")
    private String mainGuid;
    @JsonProperty("OwnerTIN")
    private String ownerTIN;
    @JsonProperty("DateRegistered")
    private String dateRegistered;
    @JsonProperty("TradeNameAmh")
    private String tradeNameAmh;
    @JsonProperty("TradesName")
    private String tradesName;
    @JsonProperty("LicenceNumber")
    private String licenceNumber;
    @JsonProperty("RenewalDate")
    private String renewalDate;
    @JsonProperty("RenewedFrom")
    private String renewedFrom;
    @JsonProperty("RenewedTo")
    private String renewedTo;
    @JsonProperty("SubGroups")
    private List<GovSubGroupDto> subGroups;
}
