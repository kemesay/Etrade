package Dx_ET_Trade.ET_Trade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponseDto {
    @JsonProperty("MainGuid")
    private String mainGuid;
    @JsonProperty("Tin")
    private String tin;
    @JsonProperty("LicenceNumber")
    private String licenceNumber;
    @JsonProperty("BusinessName")
    private String businessName;
    @JsonProperty("BusinessNameAmh")
    private String businessNameAmh;
    @JsonProperty("TradeName")
    private String tradeName;
    @JsonProperty("TradeNameAmh")
    private String tradeNameAmh;
    @JsonProperty("DateRegistered")
    private LocalDate dateRegistered;
    @JsonProperty("RenewalDate")
    private LocalDate renewalDate;
    @JsonProperty("RenewedFrom")
    private LocalDate renewedFrom;
    @JsonProperty("RenewedTo")
    private LocalDate renewedTo;
    @JsonProperty("Status")
    private Integer status;
    @JsonProperty("Capital")
    private Double capital;
    @JsonProperty("LegalCondtion")
    private String legalCondtion;
    @JsonProperty("RegNo")
    private String regNo;
    @JsonProperty("RegisteredDate")
    private LocalDate registeredDate;
    @JsonProperty("PaidUpCapital")
    private Double paidUpCapital;
    @JsonProperty("Renewable")
    private boolean renewable;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("Associates")
    private List<BusinessAssociateDto> associates;
    @JsonProperty("Address")
    private BusinessAddressDto address;
    @JsonProperty("SubGroups")
    private List<BusinessSubGroupDto> subGroups;

    // Constructor for basic business info
    public BusinessResponseDto(String mainGuid, String tin, String licenceNumber, String businessName) {
        this.mainGuid = mainGuid;
        this.tin = tin;
        this.licenceNumber = licenceNumber;
        this.businessName = businessName;
    }

    // Constructor for sync response
    public BusinessResponseDto(String tin, String businessName, String status, LocalDate renewedTo,
                             boolean renewable, String message) {
        this.tin = tin;
        this.businessName = businessName;
        this.status = status != null ? Integer.parseInt(status) : null;
        this.renewedTo = renewedTo;
        this.renewable = renewable;
        this.message = message;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor

class BusinessAssociateDto {
    @JsonProperty("Position")
    private String position;
    @JsonProperty("ManagerName")
    private String managerName;
    @JsonProperty("ManagerNameEng")
    private String managerNameEng;
    @JsonProperty("Photo")
    private String photo;
    @JsonProperty("MobilePhone")
    private String mobilePhone;
    @JsonProperty("RegularPhone")
    private String regularPhone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class BusinessAddressDto {
    @JsonProperty("Region")
    private String region;
    @JsonProperty("Zone")
    private String zone;
    @JsonProperty("Woreda")
    private String woreda;
    @JsonProperty("Kebele")
    private String kebele;
    @JsonProperty("HouseNo")
    private String houseNo;
    @JsonProperty("MobilePhone")
    private String mobilePhone;
    @JsonProperty("RegularPhone")
    private String regularPhone;
}

@Data
    @NoArgsConstructor
    @AllArgsConstructor
class BusinessSubGroupDto {
    @JsonProperty("Code")
    private Integer code;
    @JsonProperty("Description")
    private String description;
}