package Dx_ET_Trade.ET_Trade.repository;

import Dx_ET_Trade.ET_Trade.model.CompanyCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyCustomerRepository extends JpaRepository<CompanyCustomer, Long> {
    Optional<CompanyCustomer> findByTin(String Tin);
    boolean existsByTin(String Tin);
}
