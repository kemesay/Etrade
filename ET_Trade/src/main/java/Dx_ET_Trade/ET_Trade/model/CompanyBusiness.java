package Dx_ET_Trade.ET_Trade.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "company_businesses")
public class CompanyBusiness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mainGuid;
    private String licenceNumber;
    private LocalDate dateRegistered;
    private String tradeNameAmh;
    private String tradesName;
    private LocalDate renewalDate;
    private LocalDate renewedFrom;
    private LocalDate renewedTo;
    private Integer status;
    private Double capital;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CompanyAddress address;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "business_id")
    private List<BusinessSubGroup> subGroups = new ArrayList<>();
}