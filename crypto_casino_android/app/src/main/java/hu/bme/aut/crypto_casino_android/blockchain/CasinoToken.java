package hu.bme.aut.crypto_casino_android.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
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
public class CasinoToken extends Contract {
    public static final String BINARY = "60806040526103e8600655348015610015575f5ffd5b50336040518060400160405280600b81526020016a21b0b9b4b737aa37b5b2b760a91b8152506040518060400160405280600381526020016210d4d560ea1b8152508160039081610066919061031c565b506004610073828261031c565b5050506001600160a01b0381166100a457604051631e4fbdf760e01b81525f60048201526024015b60405180910390fd5b6100ad816100d5565b506100d0336100be6012600a6104cf565b6100cb90620f42406104e4565b610126565b61050e565b600580546001600160a01b038381166001600160a01b0319831681179093556040519116919082907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0905f90a35050565b6001600160a01b03821661014f5760405163ec442f0560e01b81525f600482015260240161009b565b61015a5f838361015e565b5050565b6001600160a01b038316610188578060025f82825461017d91906104fb565b909155506101f89050565b6001600160a01b0383165f90815260208190526040902054818110156101da5760405163391434e360e21b81526001600160a01b0385166004820152602481018290526044810183905260640161009b565b6001600160a01b0384165f9081526020819052604090209082900390555b6001600160a01b03821661021457600280548290039055610232565b6001600160a01b0382165f9081526020819052604090208054820190555b816001600160a01b0316836001600160a01b03167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef8360405161027791815260200190565b60405180910390a3505050565b634e487b7160e01b5f52604160045260245ffd5b600181811c908216806102ac57607f821691505b6020821081036102ca57634e487b7160e01b5f52602260045260245ffd5b50919050565b601f82111561031757805f5260205f20601f840160051c810160208510156102f55750805b601f840160051c820191505b81811015610314575f8155600101610301565b50505b505050565b81516001600160401b0381111561033557610335610284565b610349816103438454610298565b846102d0565b6020601f82116001811461037b575f83156103645750848201515b5f19600385901b1c1916600184901b178455610314565b5f84815260208120601f198516915b828110156103aa578785015182556020948501946001909201910161038a565b50848210156103c757868401515f19600387901b60f8161c191681555b50505050600190811b01905550565b634e487b7160e01b5f52601160045260245ffd5b6001815b600184111561042557808504811115610409576104096103d6565b600184161561041757908102905b60019390931c9280026103ee565b935093915050565b5f8261043b575060016104c9565b8161044757505f6104c9565b816001811461045d576002811461046757610483565b60019150506104c9565b60ff841115610478576104786103d6565b50506001821b6104c9565b5060208310610133831016604e8410600b84101617156104a6575081810a6104c9565b6104b25f1984846103ea565b805f19048211156104c5576104c56103d6565b0290505b92915050565b5f6104dd60ff84168361042d565b9392505050565b80820281158282048414176104c9576104c96103d6565b808201808211156104c9576104c96103d6565b610c508061051b5f395ff3fe6080604052600436106100ef575f3560e01c806370a0823111610087578063a0ef91df11610057578063a0ef91df14610265578063a9059cbb14610279578063dd62ed3e14610298578063f2fde38b146102dc575f5ffd5b806370a08231146101e2578063715018a6146102165780638da5cb5b1461022a57806395d89b4114610251575f5ffd5b80632c4e722e116100c25780632c4e722e14610189578063313ce5671461019e5780633290ce29146101b957806340477126146101c3575f5ffd5b806306fdde03146100f3578063095ea7b31461011d57806318160ddd1461014c57806323b872dd1461016a575b5f5ffd5b3480156100fe575f5ffd5b506101076102fb565b6040516101149190610a6b565b60405180910390f35b348015610128575f5ffd5b5061013c610137366004610abb565b61038b565b6040519015158152602001610114565b348015610157575f5ffd5b506002545b604051908152602001610114565b348015610175575f5ffd5b5061013c610184366004610ae3565b6103a4565b348015610194575f5ffd5b5061015c60065481565b3480156101a9575f5ffd5b5060405160128152602001610114565b6101c16103c7565b005b3480156101ce575f5ffd5b506101c16101dd366004610b1d565b610485565b3480156101ed575f5ffd5b5061015c6101fc366004610b34565b6001600160a01b03165f9081526020819052604090205490565b348015610221575f5ffd5b506101c1610618565b348015610235575f5ffd5b506005546040516001600160a01b039091168152602001610114565b34801561025c575f5ffd5b5061010761062b565b348015610270575f5ffd5b506101c161063a565b348015610284575f5ffd5b5061013c610293366004610abb565b6106c0565b3480156102a3575f5ffd5b5061015c6102b2366004610b54565b6001600160a01b039182165f90815260016020908152604080832093909416825291909152205490565b3480156102e7575f5ffd5b506101c16102f6366004610b34565b6106cd565b60606003805461030a90610b85565b80601f016020809104026020016040519081016040528092919081815260200182805461033690610b85565b80156103815780601f1061035857610100808354040283529160200191610381565b820191905f5260205f20905b81548152906001019060200180831161036457829003601f168201915b5050505050905090565b5f3361039881858561070a565b60019150505b92915050565b5f336103b185828561071c565b6103bc858585610798565b506001949350505050565b5f341161041b5760405162461bcd60e51b815260206004820181905260248201527f4d7573742073656e642045544820746f20707572636861736520746f6b656e7360448201526064015b60405180910390fd5b5f6006543461042a9190610bd1565b90506104486104416005546001600160a01b031690565b3383610798565b6040805182815242602082015233917f8fafebcaf9d154343dad25669bfa277f4fbacd7ac6b0c4fed522580e040a0f33910160405180910390a250565b5f81116104d45760405162461bcd60e51b815260206004820152601d60248201527f416d6f756e74206d7573742062652067726561746572207468616e20300000006044820152606401610412565b335f908152602081905260409020548111156105325760405162461bcd60e51b815260206004820152601a60248201527f496e73756666696369656e7420746f6b656e2062616c616e63650000000000006044820152606401610412565b5f600654826105419190610be8565b9050804710156105935760405162461bcd60e51b815260206004820152601c60248201527f496e73756666696369656e742045544820696e20636f6e7472616374000000006044820152606401610412565b6105af336105a96005546001600160a01b031690565b84610798565b604051339082156108fc029083905f818181858888f193505050501580156105d9573d5f5f3e3d5ffd5b506040805183815242602082015233917fd209e032bcf7ee1571736b6cca6adaa3c4a9b59c06d396c6180ba49d983b28d0910160405180910390a25050565b6106206107f5565b6106295f610822565b565b60606004805461030a90610b85565b6106426107f5565b47806106855760405162461bcd60e51b81526020600482015260126024820152714e6f2045544820746f20776974686472617760701b6044820152606401610412565b6005546040516001600160a01b039091169082156108fc029083905f818181858888f193505050501580156106bc573d5f5f3e3d5ffd5b5050565b5f33610398818585610798565b6106d56107f5565b6001600160a01b0381166106fe57604051631e4fbdf760e01b81525f6004820152602401610412565b61070781610822565b50565b6107178383836001610873565b505050565b6001600160a01b038381165f908152600160209081526040808320938616835292905220545f19811015610792578181101561078457604051637dc7a0d960e11b81526001600160a01b03841660048201526024810182905260448101839052606401610412565b61079284848484035f610873565b50505050565b6001600160a01b0383166107c157604051634b637e8f60e11b81525f6004820152602401610412565b6001600160a01b0382166107ea5760405163ec442f0560e01b81525f6004820152602401610412565b610717838383610945565b6005546001600160a01b031633146106295760405163118cdaa760e01b8152336004820152602401610412565b600580546001600160a01b038381166001600160a01b0319831681179093556040519116919082907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0905f90a35050565b6001600160a01b03841661089c5760405163e602df0560e01b81525f6004820152602401610412565b6001600160a01b0383166108c557604051634a1406b160e11b81525f6004820152602401610412565b6001600160a01b038085165f908152600160209081526040808320938716835292905220829055801561079257826001600160a01b0316846001600160a01b03167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9258460405161093791815260200190565b60405180910390a350505050565b6001600160a01b03831661096f578060025f8282546109649190610c07565b909155506109df9050565b6001600160a01b0383165f90815260208190526040902054818110156109c15760405163391434e360e21b81526001600160a01b03851660048201526024810182905260448101839052606401610412565b6001600160a01b0384165f9081526020819052604090209082900390555b6001600160a01b0382166109fb57600280548290039055610a19565b6001600160a01b0382165f9081526020819052604090208054820190555b816001600160a01b0316836001600160a01b03167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef83604051610a5e91815260200190565b60405180910390a3505050565b602081525f82518060208401528060208501604085015e5f604082850101526040601f19601f83011684010191505092915050565b80356001600160a01b0381168114610ab6575f5ffd5b919050565b5f5f60408385031215610acc575f5ffd5b610ad583610aa0565b946020939093013593505050565b5f5f5f60608486031215610af5575f5ffd5b610afe84610aa0565b9250610b0c60208501610aa0565b929592945050506040919091013590565b5f60208284031215610b2d575f5ffd5b5035919050565b5f60208284031215610b44575f5ffd5b610b4d82610aa0565b9392505050565b5f5f60408385031215610b65575f5ffd5b610b6e83610aa0565b9150610b7c60208401610aa0565b90509250929050565b600181811c90821680610b9957607f821691505b602082108103610bb757634e487b7160e01b5f52602260045260245ffd5b50919050565b634e487b7160e01b5f52601160045260245ffd5b808202811582820484141761039e5761039e610bbd565b5f82610c0257634e487b7160e01b5f52601260045260245ffd5b500490565b8082018082111561039e5761039e610bbd56fea26469706673582212200b86b57e5d35b62786927027f57ad1c4e9097f87867d74463faa973bc3d589d764736f6c634300081d0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_EXCHANGETOKENS = "exchangeTokens";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PURCHASETOKENS = "purchaseTokens";

    public static final String FUNC_RATE = "rate";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_WITHDRAWETH = "withdrawEth";

    public static final Event APPROVAL_EVENT = new Event("Approval", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event TOKENSEXCHANGED_EVENT = new Event("TokensExchanged", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event TOKENSPURCHASED_EVENT = new Event("TokensPurchased", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected CasinoToken(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CasinoToken(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CasinoToken(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CasinoToken(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<ApprovalEventResponse> getApprovalEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ApprovalEventResponse getApprovalEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVAL_EVENT, log);
        ApprovalEventResponse typedResponse = new ApprovalEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalEventFromLog(log));
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
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

    public static List<TokensExchangedEventResponse> getTokensExchangedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TOKENSEXCHANGED_EVENT, transactionReceipt);
        ArrayList<TokensExchangedEventResponse> responses = new ArrayList<TokensExchangedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokensExchangedEventResponse typedResponse = new TokensExchangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.tokenAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TokensExchangedEventResponse getTokensExchangedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TOKENSEXCHANGED_EVENT, log);
        TokensExchangedEventResponse typedResponse = new TokensExchangedEventResponse();
        typedResponse.log = log;
        typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.tokenAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<TokensExchangedEventResponse> tokensExchangedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTokensExchangedEventFromLog(log));
    }

    public Flowable<TokensExchangedEventResponse> tokensExchangedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENSEXCHANGED_EVENT));
        return tokensExchangedEventFlowable(filter);
    }

    public static List<TokensPurchasedEventResponse> getTokensPurchasedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TOKENSPURCHASED_EVENT, transactionReceipt);
        ArrayList<TokensPurchasedEventResponse> responses = new ArrayList<TokensPurchasedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokensPurchasedEventResponse typedResponse = new TokensPurchasedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.tokenAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TokensPurchasedEventResponse getTokensPurchasedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TOKENSPURCHASED_EVENT, log);
        TokensPurchasedEventResponse typedResponse = new TokensPurchasedEventResponse();
        typedResponse.log = log;
        typedResponse.userAddress = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.tokenAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<TokensPurchasedEventResponse> tokensPurchasedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTokensPurchasedEventFromLog(log));
    }

    public Flowable<TokensPurchasedEventResponse> tokensPurchasedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENSPURCHASED_EVENT));
        return tokensPurchasedEventFlowable(filter);
    }

    public static List<TransferEventResponse> getTransferEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> allowance(String owner, String spender) {
        final Function function = new Function(FUNC_ALLOWANCE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner), 
                new org.web3j.abi.datatypes.Address(160, spender)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String spender, BigInteger value) {
        final Function function = new Function(
                FUNC_APPROVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, spender), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String account) {
        final Function function = new Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, account)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> decimals() {
        final Function function = new Function(FUNC_DECIMALS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> exchangeTokens(BigInteger tokenAmount) {
        final Function function = new Function(
                FUNC_EXCHANGETOKENS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenAmount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> name() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> purchaseTokens(BigInteger weiValue) {
        final Function function = new Function(
                FUNC_PURCHASETOKENS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<BigInteger> rate() {
        final Function function = new Function(FUNC_RATE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transfer(String to, BigInteger value) {
        final Function function = new Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String from, String to,
            BigInteger value) {
        final Function function = new Function(
                FUNC_TRANSFERFROM, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from), 
                new org.web3j.abi.datatypes.Address(160, to), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawEth() {
        final Function function = new Function(
                FUNC_WITHDRAWETH, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static CasinoToken load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new CasinoToken(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CasinoToken load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new CasinoToken(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CasinoToken load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new CasinoToken(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CasinoToken load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new CasinoToken(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CasinoToken> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CasinoToken.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    public static RemoteCall<CasinoToken> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(CasinoToken.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<CasinoToken> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CasinoToken.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<CasinoToken> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(CasinoToken.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String spender;

        public BigInteger value;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class TokensExchangedEventResponse extends BaseEventResponse {
        public String userAddress;

        public BigInteger tokenAmount;

        public BigInteger timestamp;
    }

    public static class TokensPurchasedEventResponse extends BaseEventResponse {
        public String userAddress;

        public BigInteger tokenAmount;

        public BigInteger timestamp;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger value;
    }
}
