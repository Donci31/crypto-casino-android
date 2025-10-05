package hu.bme.aut.crypto_casino_backend.mapper;

import hu.bme.aut.crypto_casino_backend.dto.game.GameHistoryResponse;
import hu.bme.aut.crypto_casino_backend.model.GameSession;
import java.util.List;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameMapper {

	@Mapping(target = "spinId", source = "blockchainSpinId")
	@Mapping(target = "isWin", expression = "java(gameSession.getWinAmount().compareTo(java.math.BigDecimal.ZERO) > 0)")
	@Mapping(target = "timestamp", source = "resolvedAt")
	@Mapping(target = "reels", ignore = true) // Will be set manually from gameResult
	GameHistoryResponse gameSessionToHistoryResponse(GameSession gameSession);

	List<GameHistoryResponse> gameSessionsToHistoryResponses(List<GameSession> gameSessions);

}
