package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.TransactionDto;
import hu.bme.aut.crypto_casino_backend.model.Transaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "type", expression = "java(transaction.getType().name())")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    TransactionDto toDto(Transaction transaction);

    @InheritInverseConfiguration
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "type", expression = "java(Transaction.TransactionType.valueOf(transactionDto.getType()))")
    @Mapping(target = "status", expression = "java(Transaction.TransactionStatus.valueOf(transactionDto.getStatus()))")
    Transaction toEntity(TransactionDto transactionDto);

    List<TransactionDto> toDtoList(List<Transaction> transactions);

    @AfterMapping
    default void setEnumValues(@MappingTarget Transaction transaction, TransactionDto dto) {
        if (dto.getType() != null) {
            transaction.setType(Transaction.TransactionType.valueOf(dto.getType()));
        }
        if (dto.getStatus() != null) {
            transaction.setStatus(Transaction.TransactionStatus.valueOf(dto.getStatus()));
        }
    }
}
