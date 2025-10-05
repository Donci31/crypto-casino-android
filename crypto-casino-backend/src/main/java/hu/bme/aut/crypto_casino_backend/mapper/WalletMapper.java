package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.wallet.BalanceResponse;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletRequest;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletResponse;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.UserWallet;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", source = "user")
	@Mapping(target = "createdAt", ignore = true)
	UserWallet walletRequestToUserWallet(WalletRequest walletRequest, User user);

	WalletResponse userWalletToWalletResponse(UserWallet userWallet);

	List<WalletResponse> userWalletsToWalletResponses(List<UserWallet> userWallets);

	@Mapping(target = "address", source = "userWallet.address")
	@Mapping(target = "balance", source = "balance")
	BalanceResponse toBalanceResponse(UserWallet userWallet, BigDecimal balance);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "address", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	void updateUserWalletFromRequest(WalletRequest walletRequest, @MappingTarget UserWallet userWallet);

}
