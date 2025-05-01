package hu.bme.aut.crypto_casino_backend.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private Long id;
    private String address;
    private String label;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
