package Dx_ET_Trade.ET_Trade.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "company_addresses")
public class CompanyAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String region;
    private String zone;
    private String woreda;
    private String kebele;
    private String houseNo;
    private String mobilePhone;
    private String regularPhone;

    // Getters and Setters
}
