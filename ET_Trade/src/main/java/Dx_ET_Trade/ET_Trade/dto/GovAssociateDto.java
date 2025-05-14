package Dx_ET_Trade.ET_Trade.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GovAssociateDto {
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
