package Dx_ET_Trade.ET_Trade.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "company_customers")
public class CompanyCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tin;

    private String legalCondition;
    private String regNo;
    private LocalDate regDate;
    private String businessName;
    private String businessNameAmh;
    private Double paidUpCapital;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id")
    private List<CompanyAssociate> associates = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id")
    private List<CompanyBusiness> businesses = new ArrayList<>();

    // Ensure TIN is never null
    public void setTin(String tin) {
        if (tin == null || tin.trim().isEmpty()) {
            throw new IllegalArgumentException("TIN cannot be null or empty");
        }
        this.tin = tin.trim();
    }
}