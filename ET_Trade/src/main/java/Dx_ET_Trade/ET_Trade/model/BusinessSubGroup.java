package Dx_ET_Trade.ET_Trade.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "business_sub_groups")
public class BusinessSubGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer code;
    private String description;

    // Getters and Setters
}
