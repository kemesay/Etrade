package Dx_ET_Trade.ET_Trade.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "company_associates")
public class CompanyAssociate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String position;
    private String managerName;
    private String managerNameEng;
    @Lob
    private String photo;
    private String mobilePhone;
    private String regularPhone;

    // Getters and Setters
}
