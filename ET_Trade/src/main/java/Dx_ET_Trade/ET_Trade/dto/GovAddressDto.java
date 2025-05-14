package Dx_ET_Trade.ET_Trade.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GovAddressDto {
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
