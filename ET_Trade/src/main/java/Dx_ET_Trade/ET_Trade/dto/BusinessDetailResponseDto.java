package Dx_ET_Trade.ET_Trade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDetailResponseDto {
    private SyncResponseDto syncResponse;
    private GovBusinessDetailDto businessDetail;
    private boolean isUpdated;
} 