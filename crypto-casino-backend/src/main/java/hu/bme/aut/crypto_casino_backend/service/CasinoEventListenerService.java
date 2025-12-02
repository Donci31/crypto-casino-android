package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import hu.bme.aut.crypto_casino_backend.repository.BlockchainTransactionRepository;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.casinotoken.CasinoToken;
import org.web3j.casinovault.CasinoVault;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.events.Log;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class CasinoEventListenerService {

  private final Web3j web3j;

  private final BlockchainTransactionRepository transactionRepository;

  private final Map<String, EventConfig> eventConfigs = new HashMap<>();

  @Value("${web3j.contract.casino-token}")
  private String tokenAddress;

  @Value("${web3j.contract.casino-vault}")
  private String vaultAddress;

  private Disposable eventSubscription;

  public CasinoEventListenerService(@Qualifier("wsWeb3j") Web3j web3j,
      BlockchainTransactionRepository transactionRepository) {
    this.web3j = web3j;
    this.transactionRepository = transactionRepository;
  }

  @PostConstruct
  public void init() {
    try {
      initializeEventConfigs();
      subscribeToEvents();
      log.info("Casino Event Listener initialized successfully");
    } catch (Exception e) {
      log.error("Failed to initialize CasinoEventListenerService", e);
    }
  }

  private void initializeEventConfigs() {
    registerEvent(CasinoToken.TOKENSPURCHASED_EVENT, BlockchainTransaction.TransactionType.TOKEN_PURCHASED,
        EventStructure.SIMPLE);
    registerEvent(CasinoToken.TOKENSEXCHANGED_EVENT, BlockchainTransaction.TransactionType.TOKEN_EXCHANGED,
        EventStructure.SIMPLE);
    registerEvent(CasinoVault.DEPOSIT_EVENT, BlockchainTransaction.TransactionType.DEPOSIT,
        EventStructure.WITH_BALANCE);
    registerEvent(CasinoVault.WITHDRAWAL_EVENT, BlockchainTransaction.TransactionType.WITHDRAWAL,
        EventStructure.WITH_BALANCE);
    registerEvent(CasinoVault.BETPLACED_EVENT, BlockchainTransaction.TransactionType.BET,
        EventStructure.WITH_BALANCE_AND_GAME);
    registerEvent(CasinoVault.WINPAID_EVENT, BlockchainTransaction.TransactionType.WIN,
        EventStructure.WITH_BALANCE_AND_GAME);

    log.info("Registered {} event configurations", eventConfigs.size());
  }

  private void registerEvent(Event event, BlockchainTransaction.TransactionType type, EventStructure structure) {
    String signature = EventEncoder.encode(event);
    eventConfigs.put(signature, new EventConfig(event, type, structure));
  }

  private void subscribeToEvents() {
    eventSubscription = web3j.logsNotifications(Arrays.asList(tokenAddress, vaultAddress), Collections.emptyList())
        .subscribe(event -> processEvent(event.getParams().getResult()),
            error -> log.error("Error in event subscription: {}", error.getMessage(), error));

    log.info("Subscribed to events for contracts: {} and {}", tokenAddress, vaultAddress);
  }

  @PreDestroy
  public void cleanup() {
    if (eventSubscription != null && !eventSubscription.isDisposed()) {
      eventSubscription.dispose();
      log.info("Event subscription disposed");
    }
  }

  @SuppressWarnings("rawtypes")
  private void processEvent(Log eventLog) {
    try {
      String eventSignature = eventLog.getTopics().getFirst();
      EventConfig config = eventConfigs.get(eventSignature);

      if (config == null) {
        log.debug("Ignoring unknown event: {}", eventSignature);
        return;
      }

      List<Type> indexedParams = extractIndexedParameters(config.event, eventLog);
      List<Type> nonIndexedParams = extractNonIndexedParameters(config.event, eventLog);

      BlockchainTransaction transaction = buildTransaction(eventLog, config, indexedParams, nonIndexedParams);
      transactionRepository.save(transaction);

      log.info("Saved {} transaction: {} tokens for {}", config.type, transaction.getAmount(),
          transaction.getUserAddress());
    } catch (Exception e) {
      log.error("Error processing event: {}", eventLog, e);
    }
  }

  @SuppressWarnings("rawtypes")
  private BlockchainTransaction buildTransaction(Log eventLog, EventConfig config, List<Type> indexedParams,
      List<Type> nonIndexedParams) {
    String userAddress = ((Address) indexedParams.getFirst()).getValue().toLowerCase();
    EventData eventData = extractEventData(config.structure, indexedParams, nonIndexedParams);

    BlockchainTransaction.BlockchainTransactionBuilder builder = BlockchainTransaction.builder()
        .txHash(eventLog.getTransactionHash())
        .blockNumber(Numeric.toBigInt(eventLog.getBlockNumber()).longValue())
        .logIndex(Numeric.toBigInt(eventLog.getLogIndex()).intValue())
        .userAddress(userAddress)
        .eventType(config.type)
        .amount(eventData.amount)
        .timestamp(eventData.timestamp);

    if (eventData.newBalance != null) {
      builder.newBalance(eventData.newBalance);
    }

    if (eventData.gameAddress != null) {
      builder.gameAddress(eventData.gameAddress);
    }

    return builder.build();
  }

  @SuppressWarnings("rawtypes")
  private EventData extractEventData(EventStructure structure, List<Type> indexedParams,
      List<Type> nonIndexedParams) {
    return switch (structure) {
      case SIMPLE -> extractSimpleEventData(nonIndexedParams);
      case WITH_BALANCE -> extractEventDataWithBalance(nonIndexedParams);
      case WITH_BALANCE_AND_GAME -> extractEventDataWithBalanceAndGame(indexedParams, nonIndexedParams);
    };
  }

  @SuppressWarnings("rawtypes")
  private EventData extractSimpleEventData(List<Type> nonIndexedParams) {
    BigInteger amount = ((Uint256) nonIndexedParams.get(0)).getValue();
    BigInteger timestamp = ((Uint256) nonIndexedParams.get(1)).getValue();

    return new EventData(convertToEther(amount), null, null, convertToLocalDateTime(timestamp));
  }

  @SuppressWarnings("rawtypes")
  private EventData extractEventDataWithBalance(List<Type> nonIndexedParams) {
    BigInteger amount = ((Uint256) nonIndexedParams.get(0)).getValue();
    BigInteger newBalance = ((Uint256) nonIndexedParams.get(1)).getValue();
    BigInteger timestamp = ((Uint256) nonIndexedParams.get(2)).getValue();

    return new EventData(convertToEther(amount), convertToEther(newBalance), null,
        convertToLocalDateTime(timestamp));
  }

  @SuppressWarnings("rawtypes")
  private EventData extractEventDataWithBalanceAndGame(List<Type> indexedParams, List<Type> nonIndexedParams) {
    String gameAddress = ((Address) indexedParams.get(1)).getValue().toLowerCase();
    BigInteger amount = ((Uint256) nonIndexedParams.get(0)).getValue();
    BigInteger newBalance = ((Uint256) nonIndexedParams.get(1)).getValue();
    BigInteger timestamp = ((Uint256) nonIndexedParams.get(2)).getValue();

    return new EventData(convertToEther(amount), convertToEther(newBalance), gameAddress,
        convertToLocalDateTime(timestamp));
  }

  @SuppressWarnings("rawtypes")
  private List<Type> extractIndexedParameters(Event event, Log eventLog) {
    List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
    List<String> topics = eventLog.getTopics();

    List<Type> result = new ArrayList<>();
    for (int i = 0; i < indexedParameters.size() && i + 1 < topics.size(); i++) {
      String topic = topics.get(i + 1);
      Type param = FunctionReturnDecoder.decodeIndexedValue(topic, indexedParameters.get(i));
      result.add(param);
    }

    return result;
  }

  @SuppressWarnings("rawtypes")
  private List<Type> extractNonIndexedParameters(Event event, Log eventLog) {
    return FunctionReturnDecoder.decode(eventLog.getData(), event.getNonIndexedParameters());
  }

  private BigDecimal convertToEther(BigInteger wei) {
    return Convert.fromWei(new BigDecimal(wei), Convert.Unit.ETHER);
  }

  private LocalDateTime convertToLocalDateTime(BigInteger timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()), ZoneId.systemDefault());
  }

  private enum EventStructure {

    SIMPLE, WITH_BALANCE, WITH_BALANCE_AND_GAME

  }

  private record EventConfig(Event event, BlockchainTransaction.TransactionType type, EventStructure structure) {
  }

  private record EventData(BigDecimal amount, BigDecimal newBalance, String gameAddress, LocalDateTime timestamp) {
  }

}
