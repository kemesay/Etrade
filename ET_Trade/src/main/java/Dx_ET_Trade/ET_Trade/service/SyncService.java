package Dx_ET_Trade.ET_Trade.service;
import Dx_ET_Trade.ET_Trade.dto.*;
import Dx_ET_Trade.ET_Trade.exception.Syncexception;
import Dx_ET_Trade.ET_Trade.model.*;
import Dx_ET_Trade.ET_Trade.repository.CompanyCustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private final CompanyCustomerRepository companyCustomerRepository;
    @Autowired
    private final GovApiClientService govApiClientService;

    public SyncService(CompanyCustomerRepository companyCustomerRepository,
                       GovApiClientService govApiClientService) {
        this.companyCustomerRepository = companyCustomerRepository;
        this.govApiClientService = govApiClientService;
    }

    @Transactional
    public SyncResponseDto syncCustomerData(String tin) {
        // Rule 1: Check if customer exists in company database
        Optional<CompanyCustomer> existingCustomerOpt = companyCustomerRepository.findByTin(tin);

        if (existingCustomerOpt.isPresent()) {
            CompanyCustomer existingCustomer = existingCustomerOpt.get();
            
            // Check if any business license is expired by comparing RenewedTo with current date
            boolean hasExpiredLicense = existingCustomer.getBusinesses().stream()
                    .anyMatch(business -> business.getRenewedTo() == null ||
                            business.getRenewedTo().isBefore(LocalDate.now()));

            if (!hasExpiredLicense) {
                // Rule 1: All licenses are valid, return existing data
                return createResponse(existingCustomer, "Data exists with valid licenses", false);
            } else {
                // Rule 2: Some licenses expired, check government DB
                return handleExpiredLicenses(existingCustomer, tin);
            }
        } else {
            // Rule 3: Customer doesn't exist in company DB, fetch from government DB
            return handleNewCustomer(tin);
        }
    }

    private SyncResponseDto handleNewCustomer(String tin) {
        List<String> failedLicenses = new ArrayList<>();
        
        try {
            // Step 1: Fetch initial customer data from first API
            GovRegistrationInfoDto govInfo = govApiClientService.getRegistrationInfoByTin(tin);

            if (govInfo == null) {
                throw new Syncexception("Customer not found in government database");
            }

            // Create new customer from government data
            CompanyCustomer newCustomer = new CompanyCustomer();
            newCustomer.setTin(tin);
            newCustomer.setLegalCondition(govInfo.getLegalCondtion());
            newCustomer.setRegNo(govInfo.getRegNo());
            newCustomer.setRegDate(GovApiClientService.parseDate(govInfo.getRegDate()));
            newCustomer.setBusinessName(govInfo.getBusinessName());
            newCustomer.setBusinessNameAmh(govInfo.getBusinessNameAmh());
            newCustomer.setPaidUpCapital(govInfo.getPaidUpCapital());

            // Initialize lists
            newCustomer.setAssociates(new ArrayList<>());
            newCustomer.setBusinesses(new ArrayList<>());

            // Map associates
            if (govInfo.getAssociateShortInfos() != null) {
                govInfo.getAssociateShortInfos().stream()
                        .map(this::createAssociateFromGovData)
                        .forEach(newCustomer.getAssociates()::add);
            }

            // Step 2: For each business, try to get detailed info from second API
            if (govInfo.getBusinesses() != null) {
                for (GovBusinessDto govBusiness : govInfo.getBusinesses()) {
                    try {
                        String licenseNo = govBusiness.getLicenceNumber();
                        if (licenseNo == null || licenseNo.trim().isEmpty()) {
                            logger.warn("Skipping business with null or empty license number for TIN: {}", tin);
                            continue;
                        }

                        logger.info("Processing business with license: {}", licenseNo);
                        
                        // First try to get detailed business info from second API
                        GovBusinessDetailDto detail = null;
                        try {
                            detail = govApiClientService.getBusinessByLicenseNo(licenseNo, tin, "en");
                            logger.info("Successfully fetched detailed info for business with license: {}", licenseNo);
                        } catch (Exception e) {
                            logger.warn("Failed to fetch detailed info for business {}: {}. Will use basic info instead.", 
                                licenseNo, e.getMessage());
                        }

                        // Create business from either detailed or basic info
                        CompanyBusiness business;
                        if (detail != null) {
                            business = createBusinessFromGovData(detail);
                            logger.info("Created business from detailed info for license: {}", licenseNo);
                        } else {
                            business = createBusinessFromBasicInfo(govBusiness);
                            logger.info("Created business from basic info for license: {}", licenseNo);
                        }
                        
                        newCustomer.getBusinesses().add(business);
                    } catch (Exception e) {
                        String licenseNo = govBusiness.getLicenceNumber();
                        failedLicenses.add(licenseNo);
                        logger.error("Failed to process business {}: {}", licenseNo, e.getMessage());
                    }
                }
                
                // If all licenses failed, throw exception
                if (failedLicenses.size() == govInfo.getBusinesses().size()) {
                    throw new Syncexception("Failed to process all businesses: " + 
                        String.join(", ", failedLicenses));
                }
                
                // If some licenses failed, log warning
                if (!failedLicenses.isEmpty()) {
                    logger.warn("Failed to process some businesses: {}", 
                        String.join(", ", failedLicenses));
                }
            }

            // Save new customer
            companyCustomerRepository.save(newCustomer);

            // Check if all licenses are renewable
            boolean allRenewable = newCustomer.getBusinesses().stream()
                    .allMatch(business -> business.getRenewedTo() != null &&
                            business.getRenewedTo().isAfter(LocalDate.now()));

            String message = failedLicenses.isEmpty() ? 
                "Successfully added new customer" :
                "Added new customer with some businesses using basic info";

            return createResponse(newCustomer, message, allRenewable);
        } catch (Exception e) {
            throw new Syncexception("Failed to create new customer: " + e.getMessage());
        }
    }

    private CompanyBusiness createBusinessFromBasicInfo(GovBusinessDto govBusiness) {
        CompanyBusiness business = new CompanyBusiness();
        
        // Set basic info with null checks
        business.setMainGuid(govBusiness.getMainGuid());
        business.setLicenceNumber(govBusiness.getLicenceNumber());
        business.setDateRegistered(GovApiClientService.parseDate(govBusiness.getDateRegistered()));
        business.setTradeNameAmh(govBusiness.getTradeNameAmh());
        business.setTradesName(govBusiness.getTradesName());
        business.setRenewalDate(GovApiClientService.parseDate(govBusiness.getRenewalDate()));
        business.setRenewedFrom(GovApiClientService.parseDate(govBusiness.getRenewedFrom()));
        business.setRenewedTo(GovApiClientService.parseDate(govBusiness.getRenewedTo()));
        
        // Set default values for missing fields
        business.setStatus(1); // Active status
        business.setCapital(0.0); // Default capital

        // Map subgroups if available
        if (govBusiness.getSubGroups() != null && !govBusiness.getSubGroups().isEmpty()) {
            List<BusinessSubGroup> subGroups = govBusiness.getSubGroups().stream()
                    .map(this::createSubGroupFromSimpleDto)
                    .collect(Collectors.toList());
            business.setSubGroups(subGroups);
        } else {
            business.setSubGroups(new ArrayList<>());
        }

        // Create empty address
        business.setAddress(new CompanyAddress());

        return business;
    }

    private SyncResponseDto handleExpiredLicenses(CompanyCustomer existingCustomer, String tin) {
        try {
            // Step 1: Fetch initial customer data from first API
            GovRegistrationInfoDto govInfo = govApiClientService.getRegistrationInfoByTin(tin);

            if (govInfo == null) {
                throw new Syncexception("Customer not found in government database");
            }

            // Update businesses with government data
            for (GovBusinessDto govBusiness : govInfo.getBusinesses()) {
                String govLicenseNo = govBusiness.getLicenceNumber();
                
                if (govLicenseNo == null || govLicenseNo.trim().isEmpty()) {
                    logger.warn("Skipping business with null or empty license number for TIN: {}", tin);
                    continue;
                }

                try {
                    // Step 2: Get detailed business info from second API
                    GovBusinessDetailDto businessDetail = govApiClientService.getBusinessByLicenseNo(govLicenseNo, tin, "en");
                    
                    // Find matching business in company DB or create new
                    Optional<CompanyBusiness> existingBusinessOpt = existingCustomer.getBusinesses().stream()
                            .filter(b -> {
                                String existingLicense = b.getLicenceNumber();
                                return existingLicense != null && existingLicense.equals(govLicenseNo);
                            })
                            .findFirst();

                    if (existingBusinessOpt.isPresent()) {
                        // Update existing business
                        CompanyBusiness existingBusiness = existingBusinessOpt.get();
                        logger.info("Updating existing business with license: {}", govLicenseNo);
                        
                        if (businessDetail != null) {
                            updateBusinessFromGovData(existingBusiness, businessDetail);
                        } else {
                            // If detailed info not available, update with basic info
                            updateBusinessFromBasicInfo(existingBusiness, govBusiness);
                        }
                    } else {
                        // Add new business
                        logger.info("Adding new business with license: {}", govLicenseNo);
                        CompanyBusiness newBusiness;
                        if (businessDetail != null) {
                            newBusiness = createBusinessFromGovData(businessDetail);
                        } else {
                            newBusiness = createBusinessFromBasicInfo(govBusiness);
                        }
                        existingCustomer.getBusinesses().add(newBusiness);
                    }
                } catch (Exception e) {
                    logger.error("Failed to process business with license {}: {}", govLicenseNo, e.getMessage());
                    // Continue with next business instead of failing the entire sync
                }
            }

            // Save updated customer
            companyCustomerRepository.save(existingCustomer);

            // Check if any license is still expired after update
            boolean hasExpiredLicense = existingCustomer.getBusinesses().stream()
                    .anyMatch(business -> business.getRenewedTo() == null ||
                            business.getRenewedTo().isBefore(LocalDate.now()));

            String statusMessage = hasExpiredLicense ?
                    "Updated but some licenses are expired or not renewable" :
                    "Successfully updated with renewable licenses";

            return createResponse(existingCustomer, statusMessage, !hasExpiredLicense);
        } catch (Exception e) {
            throw new Syncexception("Failed to update expired licenses: " + e.getMessage());
        }
    }

    private void updateBusinessFromBasicInfo(CompanyBusiness existingBusiness, GovBusinessDto govBusiness) {
        existingBusiness.setMainGuid(govBusiness.getMainGuid());
        existingBusiness.setLicenceNumber(govBusiness.getLicenceNumber());
        existingBusiness.setDateRegistered(GovApiClientService.parseDate(govBusiness.getDateRegistered()));
        existingBusiness.setTradeNameAmh(govBusiness.getTradeNameAmh());
        existingBusiness.setTradesName(govBusiness.getTradesName());
        existingBusiness.setRenewalDate(GovApiClientService.parseDate(govBusiness.getRenewalDate()));
        existingBusiness.setRenewedFrom(GovApiClientService.parseDate(govBusiness.getRenewedFrom()));
        existingBusiness.setRenewedTo(GovApiClientService.parseDate(govBusiness.getRenewedTo()));

        // Update subgroups if available
        if (govBusiness.getSubGroups() != null && !govBusiness.getSubGroups().isEmpty()) {
            List<BusinessSubGroup> newSubGroups = govBusiness.getSubGroups().stream()
                    .map(this::createSubGroupFromSimpleDto)
                    .collect(Collectors.toList());
            
            existingBusiness.getSubGroups().clear();
            existingBusiness.getSubGroups().addAll(newSubGroups);
        }
    }

    private CompanyAssociate createAssociateFromGovData(GovAssociateDto govAssociate) {
        CompanyAssociate associate = new CompanyAssociate();
        associate.setPosition(govAssociate.getPosition());
        associate.setManagerName(govAssociate.getManagerName());
        associate.setManagerNameEng(govAssociate.getManagerNameEng());
        associate.setPhoto(govAssociate.getPhoto());
        associate.setMobilePhone(govAssociate.getMobilePhone());
        associate.setRegularPhone(govAssociate.getRegularPhone());
        return associate;
    }

    private CompanyBusiness createBusinessFromGovData(GovBusinessDetailDto govBusiness) {
        CompanyBusiness business = new CompanyBusiness();
        
        // Set basic info with null checks
        business.setMainGuid(govBusiness.getMainGuid());
        business.setLicenceNumber(govBusiness.getLicenceNumber());
        business.setDateRegistered(GovApiClientService.parseDate(govBusiness.getDateRegistered()));
        business.setTradeNameAmh(govBusiness.getTradeName());
        business.setTradesName(govBusiness.getTradeName());
        business.setRenewalDate(GovApiClientService.parseDate(govBusiness.getRenewalDate()));
        business.setRenewedFrom(GovApiClientService.parseDate(govBusiness.getRenewedFrom()));
        business.setRenewedTo(GovApiClientService.parseDate(govBusiness.getRenewedTo()));
        business.setStatus(govBusiness.getStatus());
        business.setCapital(govBusiness.getCapital());

        // Map address if available
        if (govBusiness.getAddressInfo() != null) {
            try {
                business.setAddress(createAddressFromGovData(govBusiness.getAddressInfo()));
            } catch (Exception e) {
                logger.warn("Failed to map address for business {}: {}", 
                    govBusiness.getLicenceNumber(), e.getMessage());
                // Create empty address
                business.setAddress(new CompanyAddress());
            }
        } else {
            // Create empty address
            business.setAddress(new CompanyAddress());
        }

        // Map business licensing groups if available
        if (govBusiness.getBusinessLicensingGroupMain() != null && !govBusiness.getBusinessLicensingGroupMain().isEmpty()) {
            try {
                List<BusinessSubGroup> subGroups = govBusiness.getBusinessLicensingGroupMain().stream()
                        .map(this::createSubGroupFromGovData)
                        .collect(Collectors.toList());
                business.setSubGroups(subGroups);
            } catch (Exception e) {
                logger.warn("Failed to map business groups for business {}: {}", 
                    govBusiness.getLicenceNumber(), e.getMessage());
                business.setSubGroups(new ArrayList<>());
            }
        } else if (govBusiness.getSubGroups() != null && !govBusiness.getSubGroups().isEmpty()) {
            // If BusinessLicensingGroupMain is not available, try to use SubGroups
            try {
                List<BusinessSubGroup> subGroups = govBusiness.getSubGroups().stream()
                        .map(this::createSubGroupFromSimpleDto)
                        .collect(Collectors.toList());
                business.setSubGroups(subGroups);
            } catch (Exception e) {
                logger.warn("Failed to map simple subgroups for business {}: {}", 
                    govBusiness.getLicenceNumber(), e.getMessage());
                business.setSubGroups(new ArrayList<>());
            }
        } else {
            business.setSubGroups(new ArrayList<>());
        }

        return business;
    }

    private BusinessSubGroup createSubGroupFromSimpleDto(GovSubGroupDto govSubGroup) {
        BusinessSubGroup subGroup = new BusinessSubGroup();
        subGroup.setCode(govSubGroup.getCode());
        subGroup.setDescription(govSubGroup.getDescription());
        return subGroup;
    }

    private BusinessSubGroup createSubGroupFromGovData(GovBusinessGroupDto govGroup) {
        BusinessSubGroup subGroup = new BusinessSubGroup();
        subGroup.setCode(govGroup.getSubGroup());
        
        // Create a detailed description from all group levels
        StringBuilder description = new StringBuilder();
        description.append("(").append(govGroup.getSubGroup()).append(") ");
        description.append("Division ").append(govGroup.getMajorDivision()).append("/");
        description.append(govGroup.getDivision()).append(" - ");
        description.append("Group ").append(govGroup.getMajorGroup()).append("/");
        description.append(govGroup.getBGroup()).append("/");
        description.append(govGroup.getSubGroup());
        
        subGroup.setDescription(description.toString());
        return subGroup;
    }

    private CompanyAddress createAddressFromGovData(GovAddressDto govAddress) {
        CompanyAddress address = new CompanyAddress();
        
        // Set all fields with null checks
        address.setRegion(govAddress.getRegion() != null ? govAddress.getRegion() : "N/A");
        address.setZone(govAddress.getZone() != null ? govAddress.getZone() : "N/A");
        address.setWoreda(govAddress.getWoreda() != null ? govAddress.getWoreda() : "N/A");
        address.setKebele(govAddress.getKebele() != null ? govAddress.getKebele() : "N/A");
        address.setHouseNo(govAddress.getHouseNo() != null ? govAddress.getHouseNo() : "N/A");
        address.setMobilePhone(govAddress.getMobilePhone() != null ? govAddress.getMobilePhone() : "N/A");
        address.setRegularPhone(govAddress.getRegularPhone() != null ? govAddress.getRegularPhone() : "N/A");
        
        return address;
    }

    private void updateBusinessFromGovData(CompanyBusiness existingBusiness, GovBusinessDetailDto govBusiness) {
        existingBusiness.setDateRegistered(GovApiClientService.parseDate(govBusiness.getDateRegistered()));
        existingBusiness.setTradeNameAmh(govBusiness.getTradeName());
        existingBusiness.setTradesName(govBusiness.getTradeName());
        existingBusiness.setRenewalDate(GovApiClientService.parseDate(govBusiness.getRenewalDate()));
        existingBusiness.setRenewedFrom(GovApiClientService.parseDate(govBusiness.getRenewedFrom()));
        existingBusiness.setRenewedTo(GovApiClientService.parseDate(govBusiness.getRenewedTo()));
        existingBusiness.setStatus(govBusiness.getStatus());
        existingBusiness.setCapital(govBusiness.getCapital());

        // Update address
        if (govBusiness.getAddressInfo() != null) {
            if (existingBusiness.getAddress() == null) {
                existingBusiness.setAddress(createAddressFromGovData(govBusiness.getAddressInfo()));
            } else {
                updateAddressFromGovData(existingBusiness.getAddress(), govBusiness.getAddressInfo());
            }
        }

        // Update business licensing groups
        if (govBusiness.getBusinessLicensingGroupMain() != null && !govBusiness.getBusinessLicensingGroupMain().isEmpty()) {
            List<BusinessSubGroup> newSubGroups = govBusiness.getBusinessLicensingGroupMain().stream()
                    .map(this::createSubGroupFromGovData)
                    .collect(Collectors.toList());
            
            // Clear existing subgroups and add new ones
            existingBusiness.getSubGroups().clear();
            existingBusiness.getSubGroups().addAll(newSubGroups);
        } else if (govBusiness.getSubGroups() != null && !govBusiness.getSubGroups().isEmpty()) {
            // If BusinessLicensingGroupMain is not available, try to use SubGroups
            List<BusinessSubGroup> newSubGroups = govBusiness.getSubGroups().stream()
                    .map(this::createSubGroupFromSimpleDto)
                    .collect(Collectors.toList());
            
            // Clear existing subgroups and add new ones
            existingBusiness.getSubGroups().clear();
            existingBusiness.getSubGroups().addAll(newSubGroups);
        }

        // Log the update
        logger.info("Updated business details for license: {}", existingBusiness.getLicenceNumber());
    }

    private void updateAddressFromGovData(CompanyAddress existingAddress, GovAddressDto govAddress) {
        existingAddress.setRegion(govAddress.getRegion());
        existingAddress.setZone(govAddress.getZone());
        existingAddress.setWoreda(govAddress.getWoreda());
        existingAddress.setKebele(govAddress.getKebele());
        existingAddress.setHouseNo(govAddress.getHouseNo());
        existingAddress.setMobilePhone(govAddress.getMobilePhone());
        existingAddress.setRegularPhone(govAddress.getRegularPhone());
    }

    @Transactional
    public SyncResponseDto updateBusinessDetails(String licenseNo, String tin, String lang) {
        try {
            // First check if customer exists in company database
            Optional<CompanyCustomer> existingCustomerOpt = companyCustomerRepository.findByTin(tin);
            if (!existingCustomerOpt.isPresent()) {
                throw new Syncexception("Customer not found in company database for TIN: " + tin);
            }

            CompanyCustomer customer = existingCustomerOpt.get();
            
            // Find the business in the customer's businesses list
            Optional<CompanyBusiness> existingBusinessOpt = customer.getBusinesses().stream()
                    .filter(b -> licenseNo.equals(b.getLicenceNumber()))
                    .findFirst();

            if (!existingBusinessOpt.isPresent()) {
                throw new Syncexception("Business not found in company database for license: " + licenseNo);
            }

            CompanyBusiness existingBusiness = existingBusinessOpt.get();

            // Get detailed business info from government API
            GovBusinessDetailDto govBusinessDetail = govApiClientService.getBusinessByLicenseNo(licenseNo, tin, lang);
            if (govBusinessDetail == null) {
                throw new Syncexception("Failed to fetch business details from government database");
            }

            // Update business with government data
            updateBusinessFromGovData(existingBusiness, govBusinessDetail);

            // Save the updated customer
            companyCustomerRepository.save(customer);

            // Check if the license is renewable
            boolean isRenewable = existingBusiness.getRenewedTo() != null && 
                                existingBusiness.getRenewedTo().isAfter(LocalDate.now());

            String message = String.format("Successfully updated business details for license: %s", licenseNo);
            return createResponse(customer, message, isRenewable);

        } catch (Exception e) {
            throw new Syncexception("Failed to update business details: " + e.getMessage());
        }
    }

    private SyncResponseDto createResponse(CompanyCustomer customer, String message, boolean renewable) {
        SyncResponseDto response = new SyncResponseDto();
        response.setTin(customer.getTin());
        response.setBusinessName(customer.getBusinessName());
        response.setStatus(message);

        // Find the latest renewedTo date among all businesses
        LocalDate latestRenewedTo = customer.getBusinesses().stream()
                .map(CompanyBusiness::getRenewedTo)
                .filter(date -> date != null)
                .max(LocalDate::compareTo)
                .orElse(null);

        response.setRenewedTo(latestRenewedTo);
        response.setRenewable(renewable);
        response.setMessage(message);

        return response;
    }
}
