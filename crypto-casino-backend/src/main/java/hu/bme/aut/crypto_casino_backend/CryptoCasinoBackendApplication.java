package hu.bme.aut.crypto_casino_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class CryptoCasinoBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(CryptoCasinoBackendApplication.class, args);
  }

}
