package hu.bme.aut.crypto_casino_android.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.13.0.
 */
@SuppressWarnings("rawtypes")
public class CasinoVault extends Contract {
    public static final String BINARY = "60a060405234801561000f575f5ffd5b50604051610de4380380610de483398101604081905261002e9161011a565b338061005457604051631e4fbdf760e01b81525f60048201526024015b60405180910390fd5b61005d816100cb565b5060018055806001600160a01b0381166100b95760405162461bcd60e51b815260206004820152601f60248201527f546f6b656e47616d655661756c743a20696e76616c6964206164647265737300604482015260640161004b565b506001600160a01b0316608052610147565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b5f6020828403121561012a575f5ffd5b81516001600160a01b0381168114610140575f5ffd5b9392505050565b608051610c7761016d5f395f81816102580152818161030a01526105b60152610c775ff3fe608060405234801561000f575f5ffd5b50600436106100e5575f3560e01c80639e492b9911610088578063d4de4ba511610063578063d4de4ba514610205578063f2fde38b14610218578063f8b2cb4f1461022b578063fc0c546a14610253575f5ffd5b80639e492b99146101bd578063b6b55f25146101df578063b8d0b334146101f2575f5ffd5b80633784ead5116100c35780633784ead51461016b5780635e4402df1461017e578063715018a6146101915780638da5cb5b14610199575f5ffd5b806307979736146100e957806327e235e3146101295780632e1a7d4d14610156575b5f5ffd5b6101146100f7366004610a94565b6001600160a01b03165f9081526002602052604090205460ff1690565b60405190151581526020015b60405180910390f35b610148610137366004610a94565b60036020525f908152604090205481565b604051908152602001610120565b610169610164366004610ab4565b61027a565b005b610169610179366004610a94565b610390565b61016961018c366004610a94565b610467565b61016961056e565b5f546001600160a01b03165b6040516001600160a01b039091168152602001610120565b6101146101cb366004610a94565b60026020525f908152604090205460ff1681565b6101696101ed366004610ab4565b610581565b610114610200366004610acb565b61064f565b610114610213366004610acb565b610759565b610169610226366004610a94565b61088b565b610148610239366004610a94565b6001600160a01b03165f9081526003602052604090205490565b6101a57f000000000000000000000000000000000000000000000000000000000000000081565b6102826108c5565b805f81116102ab5760405162461bcd60e51b81526004016102a290610af3565b60405180910390fd5b335f908152600360205260409020548211156102d95760405162461bcd60e51b81526004016102a290610b40565b335f90815260036020526040812080548492906102f7908490610b98565b9091555061033190506001600160a01b037f00000000000000000000000000000000000000000000000000000000000000001633846108ef565b335f818152600360209081526040918290205482518681529182015242918101919091527f650fdf669e93aa6c8ff3defe2da9c12b64f1548e5e1e54e803f4c1beb6466c8e906060015b60405180910390a25061038d60018055565b50565b610398610953565b6001600160a01b0381165f9081526002602052604090205460ff1661040b5760405162461bcd60e51b815260206004820152602360248201527f546f6b656e47616d655661756c743a2067616d65206e6f7420617574686f72696044820152621e995960ea1b60648201526084016102a2565b6001600160a01b0381165f8181526002602052604090819020805460ff19169055517f384b537c6fc3b0fcd54828d21067009fd37e92e4f0017aa128849c711d62e13a9061045c9042815260200190565b60405180910390a250565b61046f610953565b806001600160a01b0381166104965760405162461bcd60e51b81526004016102a290610bb1565b6001600160a01b0382165f9081526002602052604090205460ff161561050e5760405162461bcd60e51b815260206004820152602760248201527f546f6b656e47616d655661756c743a2067616d6520616c7265616479206175746044820152661a1bdc9a5e995960ca1b60648201526084016102a2565b6001600160a01b0382165f8181526002602052604090819020805460ff19166001179055517f08170c28b41f9131f2d900bc244ccd4d9f6f5283285dd5210f91a5a5b84798e0906105629042815260200190565b60405180910390a25050565b610576610953565b61057f5f61097f565b565b6105896108c5565b805f81116105a95760405162461bcd60e51b81526004016102a290610af3565b6105de6001600160a01b037f0000000000000000000000000000000000000000000000000000000000000000163330856109ce565b335f90815260036020526040812080548492906105fc908490610be8565b9091555050335f818152600360209081526040918290205482518681529182015242918101919091527f36af321ec8d3c75236829c5317affd40ddb308863a1236d2d277a4025cccee1e9060600161037b565b335f9081526002602052604081205460ff1661067d5760405162461bcd60e51b81526004016102a290610bfb565b815f811161069d5760405162461bcd60e51b81526004016102a290610af3565b836001600160a01b0381166106c45760405162461bcd60e51b81526004016102a290610bb1565b6001600160a01b0385165f90815260036020526040812080548692906106eb908490610be8565b90915550506001600160a01b0385165f818152600360209081526040918290205482518881529182015242918101919091523391907f500c3a76ef99d154118590e273498a40a6f514142dfa88fb167b6e812edb7165906060015b60405180910390a3506001949350505050565b335f9081526002602052604081205460ff166107875760405162461bcd60e51b81526004016102a290610bfb565b815f81116107a75760405162461bcd60e51b81526004016102a290610af3565b836001600160a01b0381166107ce5760405162461bcd60e51b81526004016102a290610bb1565b6001600160a01b0385165f908152600360205260409020548411156108055760405162461bcd60e51b81526004016102a290610b40565b6001600160a01b0385165f908152600360205260408120805486929061082c908490610b98565b90915550506001600160a01b0385165f818152600360209081526040918290205482518881529182015242918101919091523391907fdce6bffb3d193bb1dd489de0a8d777ecc3cd299d9209cf7f8c99004521b1394d90606001610746565b610893610953565b6001600160a01b0381166108bc57604051631e4fbdf760e01b81525f60048201526024016102a2565b61038d8161097f565b6002600154036108e857604051633ee5aeb560e01b815260040160405180910390fd5b6002600155565b6040516001600160a01b0383811660248301526044820183905261094e91859182169063a9059cbb906064015b604051602081830303815290604052915060e01b6020820180516001600160e01b038381831617835250505050610a0d565b505050565b5f546001600160a01b0316331461057f5760405163118cdaa760e01b81523360048201526024016102a2565b5f80546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b6040516001600160a01b038481166024830152838116604483015260648201839052610a079186918216906323b872dd9060840161091c565b50505050565b5f5f60205f8451602086015f885af180610a2c576040513d5f823e3d81fd5b50505f513d91508115610a43578060011415610a50565b6001600160a01b0384163b155b15610a0757604051635274afe760e01b81526001600160a01b03851660048201526024016102a2565b80356001600160a01b0381168114610a8f575f5ffd5b919050565b5f60208284031215610aa4575f5ffd5b610aad82610a79565b9392505050565b5f60208284031215610ac4575f5ffd5b5035919050565b5f5f60408385031215610adc575f5ffd5b610ae583610a79565b946020939093013593505050565b6020808252602d908201527f546f6b656e47616d655661756c743a20616d6f756e74206d757374206265206760408201526c0726561746572207468616e203609c1b606082015260800190565b60208082526024908201527f546f6b656e47616d655661756c743a20696e73756666696369656e742062616c604082015263616e636560e01b606082015260800190565b634e487b7160e01b5f52601160045260245ffd5b81810381811115610bab57610bab610b84565b92915050565b6020808252601f908201527f546f6b656e47616d655661756c743a20696e76616c6964206164647265737300604082015260600190565b80820180821115610bab57610bab610b84565b60208082526026908201527f546f6b656e47616d655661756c743a206e6f7420616e20617574686f72697a65604082015265642067616d6560d01b60608201526080019056fea2646970667358221220f9e5610ab11ebe24ce0668e146848a4458c1b222edf5b53555558253a1cab07e64736f6c634300081d0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_AUTHORIZEGAME = "authorizeGame";

    public static final String FUNC_AUTHORIZEDGAMES = "authorizedGames";

    public static final String FUNC_BALANCES = "balances";

    public static final String FUNC_DEAUTHORIZEGAME = "deauthorizeGame";

    public static final String FUNC_DEPOSIT = "deposit";

    public static final String FUNC_GETBALANCE = "getBalance";

    public static final String FUNC_ISGAMEAUTHORIZED = "isGameAuthorized";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PAYWINNINGS = "payWinnings";

    public static final String FUNC_PLACEBET = "placeBet";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_TOKEN = "token";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_WITHDRAW = "withdraw";

    public static final Event BETPLACED_EVENT = new Event("BetPlaced", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event DEPOSIT_EVENT = new Event("Deposit", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event GAMEAUTHORIZED_EVENT = new Event("GameAuthorized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event GAMEDEAUTHORIZED_EVENT = new Event("GameDeauthorized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event WINPAID_EVENT = new Event("WinPaid", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event WITHDRAWAL_EVENT = new Event("Withdrawal", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected CasinoVault(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CasinoVault(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CasinoVault(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CasinoVault(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<BetPlacedEventResponse> getBetPlacedEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BETPLACED_EVENT, transactionReceipt);
        ArrayList<BetPlacedEventResponse> responses = new ArrayList<BetPlacedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            BetPlacedEventResponse typedResponse = new BetPlacedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BetPlacedEventResponse getBetPlacedEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BETPLACED_EVENT, log);
        BetPlacedEventResponse typedResponse = new BetPlacedEventResponse();
        typedResponse.log = log;
        typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<BetPlacedEventResponse> betPlacedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBetPlacedEventFromLog(log));
    }

    public Flowable<BetPlacedEventResponse> betPlacedEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BETPLACED_EVENT));
        return betPlacedEventFlowable(filter);
    }

    public static List<DepositEventResponse> getDepositEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEPOSIT_EVENT, transactionReceipt);
        ArrayList<DepositEventResponse> responses = new ArrayList<DepositEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            DepositEventResponse typedResponse = new DepositEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DepositEventResponse getDepositEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEPOSIT_EVENT, log);
        DepositEventResponse typedResponse = new DepositEventResponse();
        typedResponse.log = log;
        typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<DepositEventResponse> depositEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDepositEventFromLog(log));
    }

    public Flowable<DepositEventResponse> depositEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEPOSIT_EVENT));
        return depositEventFlowable(filter);
    }

    public static List<GameAuthorizedEventResponse> getGameAuthorizedEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(GAMEAUTHORIZED_EVENT, transactionReceipt);
        ArrayList<GameAuthorizedEventResponse> responses = new ArrayList<GameAuthorizedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            GameAuthorizedEventResponse typedResponse = new GameAuthorizedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static GameAuthorizedEventResponse getGameAuthorizedEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(GAMEAUTHORIZED_EVENT, log);
        GameAuthorizedEventResponse typedResponse = new GameAuthorizedEventResponse();
        typedResponse.log = log;
        typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<GameAuthorizedEventResponse> gameAuthorizedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getGameAuthorizedEventFromLog(log));
    }

    public Flowable<GameAuthorizedEventResponse> gameAuthorizedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(GAMEAUTHORIZED_EVENT));
        return gameAuthorizedEventFlowable(filter);
    }

    public static List<GameDeauthorizedEventResponse> getGameDeauthorizedEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(GAMEDEAUTHORIZED_EVENT, transactionReceipt);
        ArrayList<GameDeauthorizedEventResponse> responses = new ArrayList<GameDeauthorizedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            GameDeauthorizedEventResponse typedResponse = new GameDeauthorizedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static GameDeauthorizedEventResponse getGameDeauthorizedEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(GAMEDEAUTHORIZED_EVENT, log);
        GameDeauthorizedEventResponse typedResponse = new GameDeauthorizedEventResponse();
        typedResponse.log = log;
        typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<GameDeauthorizedEventResponse> gameDeauthorizedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getGameDeauthorizedEventFromLog(log));
    }

    public Flowable<GameDeauthorizedEventResponse> gameDeauthorizedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(GAMEDEAUTHORIZED_EVENT));
        return gameDeauthorizedEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<WinPaidEventResponse> getWinPaidEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(WINPAID_EVENT, transactionReceipt);
        ArrayList<WinPaidEventResponse> responses = new ArrayList<WinPaidEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            WinPaidEventResponse typedResponse = new WinPaidEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static WinPaidEventResponse getWinPaidEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(WINPAID_EVENT, log);
        WinPaidEventResponse typedResponse = new WinPaidEventResponse();
        typedResponse.log = log;
        typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.gameAddress = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<WinPaidEventResponse> winPaidEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getWinPaidEventFromLog(log));
    }

    public Flowable<WinPaidEventResponse> winPaidEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WINPAID_EVENT));
        return winPaidEventFlowable(filter);
    }

    public static List<WithdrawalEventResponse> getWithdrawalEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(WITHDRAWAL_EVENT, transactionReceipt);
        ArrayList<WithdrawalEventResponse> responses = new ArrayList<WithdrawalEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            WithdrawalEventResponse typedResponse = new WithdrawalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static WithdrawalEventResponse getWithdrawalEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(WITHDRAWAL_EVENT, log);
        WithdrawalEventResponse typedResponse = new WithdrawalEventResponse();
        typedResponse.log = log;
        typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newBalance = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<WithdrawalEventResponse> withdrawalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getWithdrawalEventFromLog(log));
    }

    public Flowable<WithdrawalEventResponse> withdrawalEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WITHDRAWAL_EVENT));
        return withdrawalEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> authorizeGame(String gameAddress) {
        final Function function = new Function(
                FUNC_AUTHORIZEGAME, 
                Arrays.<Type>asList(new Address(160, gameAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> authorizedGames(String param0) {
        final Function function = new Function(FUNC_AUTHORIZEDGAMES, 
                Arrays.<Type>asList(new Address(160, param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<BigInteger> balances(String param0) {
        final Function function = new Function(FUNC_BALANCES, 
                Arrays.<Type>asList(new Address(160, param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> deauthorizeGame(String gameAddress) {
        final Function function = new Function(
                FUNC_DEAUTHORIZEGAME, 
                Arrays.<Type>asList(new Address(160, gameAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> deposit(BigInteger amount) {
        final Function function = new Function(
                FUNC_DEPOSIT, 
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getBalance(String player) {
        final Function function = new Function(FUNC_GETBALANCE, 
                Arrays.<Type>asList(new Address(160, player)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Boolean> isGameAuthorized(String gameAddress) {
        final Function function = new Function(FUNC_ISGAMEAUTHORIZED, 
                Arrays.<Type>asList(new Address(160, gameAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> payWinnings(String player, BigInteger amount) {
        final Function function = new Function(
                FUNC_PAYWINNINGS, 
                Arrays.<Type>asList(new Address(160, player),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> placeBet(String player, BigInteger amount) {
        final Function function = new Function(
                FUNC_PLACEBET, 
                Arrays.<Type>asList(new Address(160, player),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> token() {
        final Function function = new Function(FUNC_TOKEN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new Address(160, newOwner)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdraw(BigInteger amount) {
        final Function function = new Function(
                FUNC_WITHDRAW, 
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static CasinoVault load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new CasinoVault(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CasinoVault load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new CasinoVault(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CasinoVault load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new CasinoVault(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CasinoVault load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new CasinoVault(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CasinoVault> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, String tokenAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(160, tokenAddress)));
        return deployRemoteCall(CasinoVault.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<CasinoVault> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider, String tokenAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(160, tokenAddress)));
        return deployRemoteCall(CasinoVault.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<CasinoVault> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, String tokenAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(160, tokenAddress)));
        return deployRemoteCall(CasinoVault.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<CasinoVault> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit, String tokenAddress) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(160, tokenAddress)));
        return deployRemoteCall(CasinoVault.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    public static void linkLibraries(List<LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class BetPlacedEventResponse extends BaseEventResponse {
        public String userAddress;

        public String gameAddress;

        public BigInteger amount;

        public BigInteger newBalance;

        public BigInteger timestamp;
    }

    public static class DepositEventResponse extends BaseEventResponse {
        public String userAddress;

        public BigInteger amount;

        public BigInteger newBalance;

        public BigInteger timestamp;
    }

    public static class GameAuthorizedEventResponse extends BaseEventResponse {
        public String gameAddress;

        public BigInteger timestamp;
    }

    public static class GameDeauthorizedEventResponse extends BaseEventResponse {
        public String gameAddress;

        public BigInteger timestamp;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class WinPaidEventResponse extends BaseEventResponse {
        public String userAddress;

        public String gameAddress;

        public BigInteger amount;

        public BigInteger newBalance;

        public BigInteger timestamp;
    }

    public static class WithdrawalEventResponse extends BaseEventResponse {
        public String userAddress;

        public BigInteger amount;

        public BigInteger newBalance;

        public BigInteger timestamp;
    }
}
