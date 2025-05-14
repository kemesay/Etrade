package Dx_ET_Trade.ET_Trade.Controller;
import Dx_ET_Trade.ET_Trade.dto.*;
import Dx_ET_Trade.ET_Trade.model.CompanyBusiness;
import Dx_ET_Trade.ET_Trade.model.CompanyCustomer;
import Dx_ET_Trade.ET_Trade.repository.CompanyCustomerRepository;
import Dx_ET_Trade.ET_Trade.service.GovApiClientService;
import Dx_ET_Trade.ET_Trade.service.SyncService;
import Dx_ET_Trade.ET_Trade.exception.Syncexception;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private final SyncService syncService;

    @Autowired
    private final GovApiClientService govApiClientService;

    @Autowired
    private final CompanyCustomerRepository companyCustomerRepository;

    public BusinessController(SyncService syncService, GovApiClientService govApiClientService, CompanyCustomerRepository companyCustomerRepository) {
        this.syncService = syncService;
        this.govApiClientService = govApiClientService;
        this.companyCustomerRepository = companyCustomerRepository;
    }

    @GetMapping("/sync/{tin}")
    public ResponseEntity<?> syncBusinessByTin(@PathVariable String tin) {
        try {
            // First, try to get registration info from government
            GovRegistrationInfoDto govInfo = govApiClientService.getRegistrationInfoByTin(tin);
            if (govInfo == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Business not found in government database",
                        System.currentTimeMillis()
                    ));
            }

            // Check if customer exists in company database
            Optional<CompanyCustomer> existingCustomerOpt = companyCustomerRepository.findByTin(tin);
            boolean isUpdated = existingCustomerOpt.isPresent();

            // Then sync with our database
            SyncResponseDto syncResponse = syncService.syncCustomerData(tin);

            // Create combined response
            RegistrationResponseDto response = new RegistrationResponseDto(
                syncResponse,
                govInfo,
                isUpdated
            );

            return ResponseEntity.ok(response);


            
        } catch (Syncexception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage(),
                        System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to sync business data: " + e.getMessage(),
                        System.currentTimeMillis()
                    ));
        }
    }

    @GetMapping("/government/registration/{tin}")
    public ResponseEntity<?> getGovernmentRegistrationInfo(@PathVariable String tin) {
        try {
            GovRegistrationInfoDto info = govApiClientService.getRegistrationInfoByTin(tin);
            if (info == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if customer exists in company database
            Optional<CompanyCustomer> existingCustomerOpt = companyCustomerRepository.findByTin(tin);
            boolean isUpdated = existingCustomerOpt.isPresent();

            // Create response
            RegistrationResponseDto response = new RegistrationResponseDto(
                null, // No sync response for this endpoint
                info,
                isUpdated
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get registration info: " + e.getMessage(),
                        System.currentTimeMillis()
                    ));
        }
    }

    @GetMapping("/government/detail")
    public ResponseEntity<?> getGovernmentBusinessDetail(
            @RequestParam String licenseNo,
            @RequestParam String tin,
            @RequestParam(defaultValue = "en") String lang) {
        try {
            // First try to get from company database
            Optional<CompanyCustomer> existingCustomerOpt = companyCustomerRepository.findByTin(tin);
            if (existingCustomerOpt.isPresent()) {
                CompanyCustomer customer = existingCustomerOpt.get();
                Optional<CompanyBusiness> existingBusinessOpt = customer.getBusinesses().stream()
                        .filter(b -> licenseNo.equals(b.getLicenceNumber()))
                        .findFirst();

                if (existingBusinessOpt.isPresent()) {
                    // If business exists in company DB, update it with government data
                    SyncResponseDto syncResponse = syncService.updateBusinessDetails(licenseNo, tin, lang);
                    
                    // Get the updated business details
                    GovBusinessDetailDto businessDetail = govApiClientService.getBusinessByLicenseNo(licenseNo, tin, lang);
                    
                    // Create combined response
                    BusinessDetailResponseDto response = new BusinessDetailResponseDto(
                        syncResponse,
                        businessDetail,
                        true
                    );
                    
                    return ResponseEntity.ok(response);
                }
            }

            // If not found in company DB or update failed, get from government API
            GovBusinessDetailDto detail = govApiClientService.getBusinessByLicenseNo(licenseNo, tin, lang);
            if (detail == null) {
                return ResponseEntity.notFound().build();
            }

            // Create response with just the business details
            BusinessDetailResponseDto response = new BusinessDetailResponseDto(
                null,
                detail,
                false
            );
            
            return ResponseEntity.ok(response);
        } catch (Syncexception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage(),
                        System.currentTimeMillis()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Failed to get business details: " + e.getMessage(),
                        System.currentTimeMillis()
                    ));
        }
    }

    @GetMapping("/renewal-status")
    public ResponseEntity<List<BusinessResponseDto>> getBusinessesNeedingRenewal() {
        // This would need to be implemented in your service layer
        // Currently returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/renew/{licenseNo}")
    public ResponseEntity<BusinessResponseDto> renewBusinessLicense(
            @PathVariable String licenseNo,
            @RequestParam String renewedTo) {
        // This would need to be implemented in your service layer
        // Currently returning placeholder response
        BusinessResponseDto response = new BusinessResponseDto();
        response.setLicenceNumber(licenseNo);
        response.setRenewedTo(LocalDate.parse(renewedTo));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BusinessResponseDto>> searchBusinesses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tin,
            @RequestParam(required = false) String licenseNo) {
        // This would need to be implemented in your service layer
        // Currently returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }

    @ExceptionHandler(Syncexception.class)
    public ResponseEntity<ErrorResponse> handleSyncException(Syncexception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Data
    @AllArgsConstructor
    static class ErrorResponse {
        private int status;
        private String message;
        private long timestamp;
    }
}