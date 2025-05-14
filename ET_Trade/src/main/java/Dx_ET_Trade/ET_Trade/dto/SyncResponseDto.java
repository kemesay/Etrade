package Dx_ET_Trade.ET_Trade.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponseDto {
    @JsonProperty("Tin")
    private String Tin;
    @JsonProperty("BusinessName")
    private String BusinessName;
    @JsonProperty("Status")
    private String Status;
    @JsonProperty("RenewedTo")
    private LocalDate RenewedTo;
    @JsonProperty("Renewable")
    private boolean Renewable;
    @JsonProperty("Message")
    private String Message;
} 