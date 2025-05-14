package Dx_ET_Trade.ET_Trade.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class GovBusinessDetailDto {
    @JsonProperty("MainGuid")
    private String mainGuid;
    
    @JsonProperty("OwnerTIN")
    private String ownerTIN;
    
    @JsonProperty("DateRegistered")
    private String dateRegistered;
    
    @JsonProperty("TradeName")
    private String tradeName;
    
    @JsonProperty("LicenceNumber")
    private String licenceNumber;
    
    @JsonProperty("Status")
    private Integer status;
    
    @JsonProperty("Capital")
    private Double capital;
    
    @JsonProperty("AssociateShortInfos")
    private List<GovAssociateDto> associateShortInfos;
    
    @JsonProperty("AddressInfo")
    private GovAddressDto addressInfo;
    
    @JsonProperty("BusinessLicensingGroupMain")
    private List<GovBusinessGroupDto> businessLicensingGroupMain;
    
    @JsonProperty("RenewedTo")
    private String renewedTo;
    
    @JsonProperty("RenewedToDateString")
    private String renewedToDateString;
    
    @JsonProperty("RenewalDate")
    private String renewalDate;
    
    @JsonProperty("RenewedFrom")
    private String renewedFrom;
    
    @JsonProperty("SubGroups")
    private List<GovSubGroupDto> subGroups;
}

