package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.WalletDto;
import hu.bme.aut.crypto_casino_backend.dto.WalletSummaryDto;
import hu.bme.aut.crypto_casino_backend.model.Wallet;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TransactionMapper.class})
public interface WalletMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "recentTransactions", source = "transactions")
    WalletDto toDto(Wallet wallet);

    @InheritInverseConfiguration
    @Mapping(target = "user", ignore = true)
    Wallet toEntity(WalletDto walletDto);

    @Named("toWalletSummary")
    @Mapping(target = "transactionCount", expression = "java(wallet.getTransactions().size())")
    WalletSummaryDto toSummaryDto(Wallet wallet);

    @Named("walletSummaryToWallet")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    Wallet fromSummaryDto(WalletSummaryDto summaryDto);

    List<WalletDto> toDtoList(List<Wallet> wallets);
    List<WalletSummaryDto> toSummaryDtoList(List<Wallet> wallets);
}
