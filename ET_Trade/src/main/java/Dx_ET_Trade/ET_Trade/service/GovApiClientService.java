package Dx_ET_Trade.ET_Trade.service;
import Dx_ET_Trade.ET_Trade.dto.*;
import Dx_ET_Trade.ET_Trade.exception.Syncexception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.nio.channels.Pipe.SourceChannel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
public class GovApiClientService {
    private static final Logger logger = LoggerFactory.getLogger(GovApiClientService.class);
    
    @Value("${gov.api.base-url}")
    private String govApiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GovApiClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public GovRegistrationInfoDto getRegistrationInfoByTin(String tin) {
        try {
            String url = govApiBaseUrl + "/Registration/GetRegistrationInfoByTin/" + tin + "/en";
            logger.info("Fetching registration info for TIN: {} from URL: {}", tin, url);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (rawResponse.getStatusCode() == HttpStatus.OK && rawResponse.getBody() != null) {
                logger.debug("Raw API response: {}", rawResponse.getBody());
                
                try {
                    GovRegistrationInfoDto registrationInfo = objectMapper.readValue(
                        rawResponse.getBody(), GovRegistrationInfoDto.class);
                    
                    logger.info("Successfully retrieved registration info for TIN: {}", tin);
                    
                    validateRegistrationInfo(registrationInfo, tin);
                    return registrationInfo;
                } catch (JsonProcessingException e) {
                    logger.error("Failed to parse registration info response for TIN {}: {}", tin, e.getMessage());
                    throw new Syncexception("Failed to parse registration info response: " + e.getMessage());
                }
            } else {
                String errorMessage = String.format("No registration info found for TIN: %s - Status: %s", 
                    tin, rawResponse.getStatusCode());
                logger.warn(errorMessage);
                throw new Syncexception(errorMessage);
            }
        } catch (RestClientException e) {
            String errorMessage = String.format("Failed to connect to government API for TIN %s: %s", 
                tin, e.getMessage());
            logger.error(errorMessage, e);
            throw new Syncexception(errorMessage);
        } catch (Exception e) {
            String errorMessage = String.format("Error fetching registration info for TIN %s: %s", 
                tin, e.getMessage());
            logger.error(errorMessage, e);
            throw new Syncexception(errorMessage);
        }
    }

    public GovBusinessDetailDto getBusinessByLicenseNo(String licenseNo, String tin, String lang) {
        try {
            String url = govApiBaseUrl + "/BusinessMain/GetBusinessByLicenseNo";
            logger.info("Fetching business details for License: {} and TIN: {}", licenseNo, tin);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("LicenseNo", licenseNo)
                    .queryParam("Tin", tin)
                    .queryParam("Lang", lang);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET, entity, String.class);

            if (rawResponse.getStatusCode() == HttpStatus.OK && rawResponse.getBody() != null) {
                logger.debug("Raw business details response: {}", rawResponse.getBody());
                
                try {
                    GovBusinessDetailDto businessDetail = objectMapper.readValue(
                        rawResponse.getBody(), GovBusinessDetailDto.class);
                    
                    // Ensure license number is set from the request parameter
                    if (businessDetail != null && businessDetail.getLicenceNumber() == null) {
                        businessDetail.setLicenceNumber(licenseNo);
                    }
                    
                    logger.info("Successfully retrieved business details for License: {}", licenseNo);
                    
                    validateBusinessDetail(businessDetail, licenseNo);
                    return businessDetail;
                } catch (JsonProcessingException e) {
                    String errorMessage = String.format("Failed to parse business details response for License %s: %s", 
                        licenseNo, e.getMessage());
                    logger.error(errorMessage);
                    throw new Syncexception(errorMessage);
                }
            } else {
                String errorMessage = String.format("No business details found for License: %s - Status: %s", 
                    licenseNo, rawResponse.getStatusCode());
                logger.warn(errorMessage);
                throw new Syncexception(errorMessage);
            }
        } catch (RestClientException e) {
            String errorMessage = String.format("Failed to connect to government API for License %s: %s", 
                licenseNo, e.getMessage());
            logger.error(errorMessage, e);
            throw new Syncexception(errorMessage);
        } catch (Syncexception e) {
            // Re-throw Syncexception as is
            throw e;
        } catch (Exception e) {
            String errorMessage = String.format("Error fetching business details for License %s: %s", 
                licenseNo, e.getMessage());
            logger.error(errorMessage, e);
            throw new Syncexception(errorMessage);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Referer", "https://etrade.gov.et");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private void validateRegistrationInfo(GovRegistrationInfoDto info, String tin) {
        if (info == null) {
            throw new Syncexception("Null registration info received for TIN: " + tin);
        }

        StringBuilder missingFields = new StringBuilder();
        
        if (info.getBusinessName() == null) {
            missingFields.append("BusinessName, ");
        }
        if (info.getRegNo() == null) {
            missingFields.append("RegNo, ");
        }
        if (info.getBusinesses() == null || info.getBusinesses().isEmpty()) {
            missingFields.append("Businesses, ");
        }

        if (missingFields.length() > 0) {
            String missing = missingFields.substring(0, missingFields.length() - 2); // Remove trailing comma and space
            String errorMessage = String.format("Invalid registration info: missing required fields (%s) for TIN: %s", 
                missing, tin);
            logger.warn(errorMessage);
            throw new Syncexception(errorMessage);
        }
    }

    private void validateBusinessDetail(GovBusinessDetailDto detail, String licenseNo) {
        if (detail == null) {
            throw new Syncexception("Null business detail received for License: " + licenseNo);
        }

        // Log the received detail for debugging
        logger.debug("Validating business detail for license {}: {}", licenseNo, detail);

        // Check for essential fields
        StringBuilder missingFields = new StringBuilder();
        
        // Generate MainGuid if missing
        if (detail.getMainGuid() == null) {
            // Generate a MainGuid using license number and timestamp
            String generatedGuid = "GUID_" + licenseNo.replaceAll("[^A-Za-z0-9]", "_") + "_" + System.currentTimeMillis();
            detail.setMainGuid(generatedGuid);
            logger.info("Generated MainGuid for license {}: {}", licenseNo, generatedGuid);
        }

        // Only LicenceNumber is truly required now
        if (detail.getLicenceNumber() == null) {
            missingFields.append("LicenceNumber, ");
        }

        // If any required fields are missing, throw exception with details
        if (missingFields.length() > 0) {
            String missing = missingFields.substring(0, missingFields.length() - 2); // Remove trailing comma and space
            String errorMessage = String.format("Invalid business detail: missing required fields (%s) for License: %s", 
                missing, licenseNo);
            logger.warn(errorMessage);
            throw new Syncexception(errorMessage);
        }

        // For optional fields, set defaults if missing
        if (detail.getTradeName() == null) {
            detail.setTradeName("N/A");
            logger.debug("Setting default TradeName for license {}", licenseNo);
        }
        
        // if (detail.getRenewedTo() == null) {
        //     // Set to current date + 1 year if missing
        //     detail.setRenewedTo(LocalDate.now().plusYears(1));
        //     logger.debug("Setting default RenewedTo date for license {}", licenseNo);
        // }

        // Validate dates if both are present
        // if (detail.getRenewedTo() != null && detail.getRenewedFrom() != null) {
        //     if (detail.getRenewedTo().isBefore(detail.getRenewedFrom())) {
        //         logger.warn("Invalid date range for license {}: RenewedTo ({}) is before RenewedFrom ({}), adjusting dates", 
        //             licenseNo, detail.getRenewedTo(), detail.getRenewedFrom());
        //         // Swap the dates if they're in wrong order
        //         LocalDate temp = detail.getRenewedFrom();
        //         detail.setRenewedFrom(detail.getRenewedTo());
        //         detail.setRenewedTo(temp);
        //     }
        // }
    }

    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            logger.error("Error parsing date: {} - {}", dateString, e.getMessage());
            return null;
        }
    }
}