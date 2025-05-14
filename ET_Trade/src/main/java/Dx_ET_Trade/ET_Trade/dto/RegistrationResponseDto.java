package Dx_ET_Trade.ET_Trade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDto {
    private SyncResponseDto syncResponse;
    private GovRegistrationInfoDto registrationInfo;
    private boolean isUpdated;
} 