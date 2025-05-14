package Dx_ET_Trade.ET_Trade.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

//# application.properties
//gov.api.base-url=https://etrade.gov.et/api
//spring.datasource.url=jdbc:mysql://localhost:3306/company_db
//spring.datasource.username=root
//spring.datasource.password=password
//spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
//spring.jpa.hibernate.ddl-auto=update
//spring.jpa.show-sql=true
