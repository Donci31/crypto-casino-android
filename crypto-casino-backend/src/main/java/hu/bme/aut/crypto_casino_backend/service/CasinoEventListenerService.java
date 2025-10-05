package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import hu.bme.aut.crypto_casino_backend.repository.BlockchainTransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@Slf4j
public class CasinoEventListenerService {

	@Value("${web3j.contract.casino-token}")
	private String tokenAddress;

	@Value("${web3j.contract.casino-vault}")
	private String vaultAddress;

	private final Web3j web3j;

	private final BlockchainTransactionRepository transactionRepository;

	// Map event signatures to event types for faster lookup
	private final Map<String, BlockchainTransaction.TransactionType> eventSignatureToType = new HashMap<>();

	private final Map<String, Event> eventSignatureToEvent = new HashMap<>();

	@Autowired
	public CasinoEventListenerService(@Qualifier("wsWeb3j") Web3j web3j,
			BlockchainTransactionRepository transactionRepository) {
		this.web3j = web3j;
		this.transactionRepository = transactionRepository;
	}

	@PostConstruct
	public void init() {
		try {
			// Initialize event signature maps
			initializeEventMaps();

			// Subscribe to events
			subscribeToEvents();

			log.info("Casino Event Listener initialized successfully");
		}
		catch (Exception e) {
			log.error("Failed to initialize CasinoEventListenerService", e);
		}
	}

	private void initializeEventMaps() {
		// Token events
		eventSignatureToType.put(EventEncoder.encode(CasinoToken.TOKENSPURCHASED_EVENT),
				BlockchainTransaction.TransactionType.TOKEN_PURCHASED);
		eventSignatureToEvent.put(EventEncoder.encode(CasinoToken.TOKENSPURCHASED_EVENT),
				CasinoToken.TOKENSPURCHASED_EVENT);

		eventSignatureToType.put(EventEncoder.encode(CasinoToken.TOKENSEXCHANGED_EVENT),
				BlockchainTransaction.TransactionType.TOKEN_EXCHANGED);
		eventSignatureToEvent.put(EventEncoder.encode(CasinoToken.TOKENSEXCHANGED_EVENT),
				CasinoToken.TOKENSEXCHANGED_EVENT);

		// Vault events
		eventSignatureToType.put(EventEncoder.encode(CasinoVault.DEPOSIT_EVENT),
				BlockchainTransaction.TransactionType.DEPOSIT);
		eventSignatureToEvent.put(EventEncoder.encode(CasinoVault.DEPOSIT_EVENT), CasinoVault.DEPOSIT_EVENT);

		eventSignatureToType.put(EventEncoder.encode(CasinoVault.WITHDRAWAL_EVENT),
				BlockchainTransaction.TransactionType.WITHDRAWAL);
		eventSignatureToEvent.put(EventEncoder.encode(CasinoVault.WITHDRAWAL_EVENT), CasinoVault.WITHDRAWAL_EVENT);

		eventSignatureToType.put(EventEncoder.encode(CasinoVault.BETPLACED_EVENT),
				BlockchainTransaction.TransactionType.BET);
		eventSignatureToEvent.put(EventEncoder.encode(CasinoVault.BETPLACED_EVENT), CasinoVault.BETPLACED_EVENT);

		eventSignatureToType.put(EventEncoder.encode(CasinoVault.WINPAID_EVENT),
				BlockchainTransaction.TransactionType.WIN);
		eventSignatureToEvent.put(EventEncoder.encode(CasinoVault.WINPAID_EVENT), CasinoVault.WINPAID_EVENT);

		log.info("Event signature mappings initialized");
	}

	private void subscribeToEvents() {
		web3j.logsNotifications(Arrays.asList(tokenAddress, vaultAddress), Collections.emptyList())
			.subscribe(event -> processEvent(event.getParams().getResult()),
					error -> log.error("Error in event subscription: {}", error.getMessage(), error));

		log.info("Subscribed to events for contracts: {} and {}", tokenAddress, vaultAddress);
	}

	private void processEvent(Log eventLog) {
		try {
			String eventSignature = eventLog.getTopics().get(0);
			BlockchainTransaction.TransactionType eventType = eventSignatureToType.get(eventSignature);

			if (eventType == null) {
				log.info("Ignoring unknown event: {}", eventSignature);
				return;
			}

			Event event = eventSignatureToEvent.get(eventSignature);
			if (event == null) {
				log.warn("Event definition not found for signature: {}", eventSignature);
				return;
			}

			List<Type> indexedParameters = extractIndexedParameters(event, eventLog);
			List<Type> nonIndexedParameters = extractNonIndexedParameters(event, eventLog);

			switch (eventType) {
				case TOKEN_PURCHASED -> processTokenPurchasedEvent(eventLog, indexedParameters, nonIndexedParameters);
				case TOKEN_EXCHANGED -> processTokenExchangedEvent(eventLog, indexedParameters, nonIndexedParameters);
				case DEPOSIT -> processDepositEvent(eventLog, indexedParameters, nonIndexedParameters);
				case WITHDRAWAL -> processWithdrawalEvent(eventLog, indexedParameters, nonIndexedParameters);
				case BET -> processBetPlacedEvent(eventLog, indexedParameters, nonIndexedParameters);
				case WIN -> processWinPaidEvent(eventLog, indexedParameters, nonIndexedParameters);
				default -> log.warn("Unhandled event type: {}", eventType);
			}
		}
		catch (Exception e) {
			log.error("Error processing event: {}", eventLog, e);
		}
	}

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

	private List<Type> extractNonIndexedParameters(Event event, Log eventLog) {
		List<TypeReference<Type>> nonIndexedParameters = event.getNonIndexedParameters();
		String data = eventLog.getData();

		return FunctionReturnDecoder.decode(data, nonIndexedParameters);
	}

	private void processTokenPurchasedEvent(Log eventLog, List<Type> indexedParams, List<Type> nonIndexedParams) {
		try {
			String userAddress = ((Address) indexedParams.get(0)).getValue().toLowerCase();
			BigInteger tokenAmount = ((Uint256) nonIndexedParams.get(0)).getValue();
			BigInteger timestamp = ((Uint256) nonIndexedParams.get(1)).getValue();

			BigDecimal amount = Convert.fromWei(new BigDecimal(tokenAmount), Convert.Unit.ETHER);
			LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()),
					ZoneId.systemDefault());

			BlockchainTransaction transaction = BlockchainTransaction.builder()
				.txHash(eventLog.getTransactionHash())
				.blockNumber(Numeric.toBigInt(eventLog.getBlockNumber()).longValue())
				.logIndex(Numeric.toBigInt(eventLog.getLogIndex()).intValue())
				.userAddress(userAddress)
				.eventType(BlockchainTransaction.TransactionType.TOKEN_PURCHASED)
				.amount(amount)
				.timestamp(eventTime)
				.build();

			transactionRepository.save(transaction);
			log.info("Saved TOKEN_PURCHASED transaction: {} tokens for {}", amount, userAddress);
		}
		catch (Exception e) {
			log.error("Error processing TokensPurchased event", e);
		}
	}

	private void processTokenExchangedEvent(Log eventLog, List<Type> indexedParams, List<Type> nonIndexedParams) {
		try {
			String userAddress = ((Address) indexedParams.get(0)).getValue().toLowerCase();
			BigInteger tokenAmount = ((Uint256) nonIndexedParams.get(0)).getValue();
			BigInteger timestamp = ((Uint256) nonIndexedParams.get(1)).getValue();

			BigDecimal amount = Convert.fromWei(new BigDecimal(tokenAmount), Convert.Unit.ETHER);
			LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()),
					ZoneId.systemDefault());

			BlockchainTransaction transaction = BlockchainTransaction.builder()
				.txHash(eventLog.getTransactionHash())
				.blockNumber(Numeric.toBigInt(eventLog.getBlockNumber()).longValue())
				.logIndex(Numeric.toBigInt(eventLog.getLogIndex()).intValue())
				.userAddress(userAddress)
				.eventType(BlockchainTransaction.TransactionType.TOKEN_EXCHANGED)
				.amount(amount)
				.timestamp(eventTime)
				.build();

			transactionRepository.save(transaction);
			log.info("Saved TOKEN_EXCHANGED transaction: {} tokens for {}", amount, userAddress);
		}
		catch (Exception e) {
			log.error("Error processing TokensExchanged event", e);
		}
	}

	private void processDepositEvent(Log eventLog, List<Type> indexedParams, List<Type> nonIndexedParams) {
		try {
			String userAddress = ((Address) indexedParams.get(0)).getValue().toLowerCase();
			BigInteger amount = ((Uint256) nonIndexedParams.get(0)).getValue();
			BigInteger newBalance = ((Uint256) nonIndexedParams.get(1)).getValue();
			BigInteger timestamp = ((Uint256) nonIndexedParams.get(2)).getValue();

			BigDecimal amountEth = Convert.fromWei(new BigDecimal(amount), Convert.Unit.ETHER);
			BigDecimal newBalanceEth = Convert.fromWei(new BigDecimal(newBalance), Convert.Unit.ETHER);
			LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()),
					ZoneId.systemDefault());

			BlockchainTransaction transaction = BlockchainTransaction.builder()
				.txHash(eventLog.getTransactionHash())
				.blockNumber(Numeric.toBigInt(eventLog.getBlockNumber()).longValue())
				.logIndex(Numeric.toBigInt(eventLog.getLogIndex()).intValue())
				.userAddress(userAddress)
				.eventType(BlockchainTransaction.TransactionType.DEPOSIT)
				.amount(amountEth)
				.newBalance(newBalanceEth)
				.timestamp(eventTime)
				.build();

			transactionRepository.save(transaction);
			log.info("Saved DEPOSIT transaction: {} tokens for {}", amountEth, userAddress);
		}
		catch (Exception e) {
			log.error("Error processing Deposit event", e);
		}
	}

	private void processWithdrawalEvent(Log eventLog, List<Type> indexedParams, List<Type> nonIndexedParams) {
		try {
			String userAddress = ((Address) indexedParams.get(0)).getValue().toLowerCase();
			BigInteger amount = ((Uint256) nonIndexedParams.get(0)).getValue();
			BigInteger newBalance = ((Uint256) nonIndexedParams.get(1)).getValue();
			BigInteger timestamp = ((Uint256) nonIndexedParams.get(2)).getValue();

			BigDecimal amountEth = Convert.fromWei(new BigDecimal(amount), Convert.Unit.ETHER);
			BigDecimal newBalanceEth = Convert.fromWei(new BigDecimal(newBalance), Convert.Unit.ETHER);
			LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()),
					ZoneId.systemDefault());

			BlockchainTransaction transaction = BlockchainTransaction.builder()
				.txHash(eventLog.getTransactionHash())
				.blockNumber(Numeric.toBigInt(eventLog.getBlockNumber()).longValue())
				.logIndex(Numeric.toBigInt(eventLog.getLogIndex()).intValue())
				.userAddress(userAddress)
				.eventType(BlockchainTransaction.TransactionType.WITHDRAWAL)
				.amount(amountEth)
				.newBalance(newBalanceEth)
				.timestamp(eventTime)
				.build();

			transactionRepository.save(transaction);
			log.info("Saved WITHDRAWAL transaction: {} tokens for {}", amountEth, userAddress);
		}
		catch (Exception e) {
			log.error("Error processing Withdrawal event", e);
		}
	}

	private void processBetPlacedEvent(Log eventLog, List<Type> indexedParams, List<Type> nonIndexedParams) {
		try {
			String userAddress = ((Address) indexedParams.get(0)).getValue().toLowerCase();
			String gameAddress = ((Address) indexedParams.get(1)).getValue().toLowerCase();
			BigInteger amount = ((Uint256) nonIndexedParams.get(0)).getValue();
			BigInteger newBalance = ((Uint256) nonIndexedParams.get(1)).getValue();
			BigInteger timestamp = ((Uint256) nonIndexedParams.get(2)).getValue();

			BigDecimal amountEth = Convert.fromWei(new BigDecimal(amount), Convert.Unit.ETHER);
			BigDecimal newBalanceEth = Convert.fromWei(new BigDecimal(newBalance), Convert.Unit.ETHER);
			LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()),
					ZoneId.systemDefault());

			BlockchainTransaction transaction = BlockchainTransaction.builder()
				.txHash(eventLog.getTransactionHash())
				.blockNumber(Numeric.toBigInt(eventLog.getBlockNumber()).longValue())
				.logIndex(Numeric.toBigInt(eventLog.getLogIndex()).intValue())
				.userAddress(userAddress)
				.eventType(BlockchainTransaction.TransactionType.BET)
				.amount(amountEth)
				.newBalance(newBalanceEth)
				.gameAddress(gameAddress)
				.timestamp(eventTime)
				.build();

			transactionRepository.save(transaction);
			log.info("Saved BET transaction: {} tokens for {} on game {}", amountEth, userAddress, gameAddress);
		}
		catch (Exception e) {
			log.error("Error processing BetPlaced event", e);
		}
	}

	private void processWinPaidEvent(Log eventLog, List<Type> indexedParams, List<Type> nonIndexedParams) {
		try {
			String userAddress = ((Address) indexedParams.get(0)).getValue().toLowerCase();
			String gameAddress = ((Address) indexedParams.get(1)).getValue().toLowerCase();
			BigInteger amount = ((Uint256) nonIndexedParams.get(0)).getValue();
			BigInteger newBalance = ((Uint256) nonIndexedParams.get(1)).getValue();
			BigInteger timestamp = ((Uint256) nonIndexedParams.get(2)).getValue();

			BigDecimal amountEth = Convert.fromWei(new BigDecimal(amount), Convert.Unit.ETHER);
			BigDecimal newBalanceEth = Convert.fromWei(new BigDecimal(newBalance), Convert.Unit.ETHER);
			LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.longValue()),
					ZoneId.systemDefault());

			BlockchainTransaction transaction = BlockchainTransaction.builder()
				.txHash(eventLog.getTransactionHash())
				.blockNumber(Numeric.toBigInt(eventLog.getBlockNumber()).longValue())
				.logIndex(Numeric.toBigInt(eventLog.getLogIndex()).intValue())
				.userAddress(userAddress)
				.eventType(BlockchainTransaction.TransactionType.WIN)
				.amount(amountEth)
				.newBalance(newBalanceEth)
				.gameAddress(gameAddress)
				.timestamp(eventTime)
				.build();

			transactionRepository.save(transaction);
			log.info("Saved WIN transaction: {} tokens for {} from game {}", amountEth, userAddress, gameAddress);
		}
		catch (Exception e) {
			log.error("Error processing WinPaid event", e);
		}
	}

}
