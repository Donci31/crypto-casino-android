package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.transaction.BlockchainTransactionDto;
import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BlockchainTransactionMapper {

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "id", ignore = true)
  BlockchainTransaction toEntity(BlockchainTransactionDto dto);

  BlockchainTransactionDto toDto(BlockchainTransaction entity);

  List<BlockchainTransactionDto> toDtoList(List<BlockchainTransaction> entities);

}
