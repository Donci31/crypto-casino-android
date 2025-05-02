package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.transaction.BlockchainTransactionDto;
import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BlockchainTransactionMapper {
    BlockchainTransaction toEntity(BlockchainTransactionDto dto);
    BlockchainTransactionDto toDto(BlockchainTransaction entity);
    List<BlockchainTransactionDto> toDtoList(List<BlockchainTransaction> entities);
}
