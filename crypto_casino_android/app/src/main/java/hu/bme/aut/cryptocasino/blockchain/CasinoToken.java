package hu.bme.aut.cryptocasino.blockchain;

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
 * <p>Generated with web3j version 4.12.3.
 */
@SuppressWarnings("rawtypes")
public class CasinoToken extends Contract {
    public static final String BINARY = "60806040526103e8600655348015610015575f5ffd5b50336040518060400160405280600b81526020016a21b0b9b4b737aa37b5b2b760a91b8152506040518060400160405280600381526020016210d4d560ea1b8152508160039081610066919061031c565b506004610073828261031c565b5050506001600160a01b0381166100a457604051631e4fbdf760e01b81525f60048201526024015b60405180910390fd5b6100ad816100d5565b506100d0336100be6012600a6104cf565b6100cb90629896806104e4565b610126565b61050e565b600580546001600160a01b038381166001600160a01b0319831681179093556040519116919082907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0905f90a35050565b6001600160a01b03821661014f5760405163ec442f0560e01b81525f600482015260240161009b565b61015a5f838361015e565b5050565b6001600160a01b038316610188578060025f82825461017d91906104fb565b909155506101f89050565b6001600160a01b0383165f90815260208190526040902054818110156101da5760405163391434e360e21b81526001600160a01b0385166004820152602481018290526044810183905260640161009b565b6001600160a01b0384165f9081526020819052604090209082900390555b6001600160a01b03821661021457600280548290039055610232565b6001600160a01b0382165f9081526020819052604090208054820190555b816001600160a01b0316836001600160a01b03167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef8360405161027791815260200190565b60405180910390a3505050565b634e487b7160e01b5f52604160045260245ffd5b600181811c908216806102ac57607f821691505b6020821081036102ca57634e487b7160e01b5f52602260045260245ffd5b50919050565b601f82111561031757805f5260205f20601f840160051c810160208510156102f55750805b601f840160051c820191505b81811015610314575f8155600101610301565b50505b505050565b81516001600160401b0381111561033557610335610284565b610349816103438454610298565b846102d0565b6020601f82116001811461037b575f83156103645750848201515b5f19600385901b1c1916600184901b178455610314565b5f84815260208120601f198516915b828110156103aa578785015182556020948501946001909201910161038a565b50848210156103c757868401515f19600387901b60f8161c191681555b50505050600190811b01905550565b634e487b7160e01b5f52601160045260245ffd5b6001815b600184111561042557808504811115610409576104096103d6565b600184161561041757908102905b60019390931c9280026103ee565b935093915050565b5f8261043b575060016104c9565b8161044757505f6104c9565b816001811461045d576002811461046757610483565b60019150506104c9565b60ff841115610478576104786103d6565b50506001821b6104c9565b5060208310610133831016604e8410600b84101617156104a6575081810a6104c9565b6104b25f1984846103ea565b805f19048211156104c5576104c56103d6565b0290505b92915050565b5f6104dd60ff84168361042d565b9392505050565b80820281158282048414176104c9576104c96103d6565b808201808211156104c9576104c96103d6565b610d218061051b5f395ff3fe6080604052600436106100fa575f3560e01c8063439370b11161009257806395d89b411161006257806395d89b4114610264578063a9059cbb14610278578063c311d04914610297578063dd62ed3e146102b6578063f2fde38b146102fa575f5ffd5b8063439370b1146101ed57806370a08231146101f5578063715018a6146102295780638da5cb5b1461023d575f5ffd5b80632c4e722e116100cd5780632c4e722e14610194578063313ce567146101a95780633290ce29146101c457806340477126146101ce575f5ffd5b806306fdde03146100fe578063095ea7b31461012857806318160ddd1461015757806323b872dd14610175575b5f5ffd5b348015610109575f5ffd5b50610112610319565b60405161011f9190610b3c565b60405180910390f35b348015610133575f5ffd5b50610147610142366004610b8c565b6103a9565b604051901515815260200161011f565b348015610162575f5ffd5b506002545b60405190815260200161011f565b348015610180575f5ffd5b5061014761018f366004610bb4565b6103c2565b34801561019f575f5ffd5b5061016760065481565b3480156101b4575f5ffd5b506040516012815260200161011f565b6101cc6103e5565b005b3480156101d9575f5ffd5b506101cc6101e8366004610bee565b6104a3565b6101cc610636565b348015610200575f5ffd5b5061016761020f366004610c05565b6001600160a01b03165f9081526020819052604090205490565b348015610234575f5ffd5b506101cc61068f565b348015610248575f5ffd5b506005546040516001600160a01b03909116815260200161011f565b34801561026f575f5ffd5b506101126106a0565b348015610283575f5ffd5b50610147610292366004610b8c565b6106af565b3480156102a2575f5ffd5b506101cc6102b1366004610bee565b6106bc565b3480156102c1575f5ffd5b506101676102d0366004610c25565b6001600160a01b039182165f90815260016020908152604080832093909416825291909152205490565b348015610305575f5ffd5b506101cc610314366004610c05565b61079e565b60606003805461032890610c56565b80601f016020809104026020016040519081016040528092919081815260200182805461035490610c56565b801561039f5780601f106103765761010080835404028352916020019161039f565b820191905f5260205f20905b81548152906001019060200180831161038257829003601f168201915b5050505050905090565b5f336103b68185856107db565b60019150505b92915050565b5f336103cf8582856107ed565b6103da858585610869565b506001949350505050565b5f34116104395760405162461bcd60e51b815260206004820181905260248201527f4d7573742073656e642045544820746f20707572636861736520746f6b656e7360448201526064015b60405180910390fd5b5f600654346104489190610ca2565b905061046661045f6005546001600160a01b031690565b3383610869565b6040805182815242602082015233917f8fafebcaf9d154343dad25669bfa277f4fbacd7ac6b0c4fed522580e040a0f33910160405180910390a250565b5f81116104f25760405162461bcd60e51b815260206004820152601d60248201527f416d6f756e74206d7573742062652067726561746572207468616e20300000006044820152606401610430565b335f908152602081905260409020548111156105505760405162461bcd60e51b815260206004820152601a60248201527f496e73756666696369656e7420746f6b656e2062616c616e63650000000000006044820152606401610430565b5f6006548261055f9190610cb9565b9050804710156105b15760405162461bcd60e51b815260206004820152601c60248201527f496e73756666696369656e742045544820696e20636f6e7472616374000000006044820152606401610430565b6105cd336105c76005546001600160a01b031690565b84610869565b604051339082156108fc029083905f818181858888f193505050501580156105f7573d5f5f3e3d5ffd5b506040805183815242602082015233917fd209e032bcf7ee1571736b6cca6adaa3c4a9b59c06d396c6180ba49d983b28d0910160405180910390a25050565b61063e6108c6565b5f341161068d5760405162461bcd60e51b815260206004820152601860248201527f4d7573742073656e642045544820746f206465706f73697400000000000000006044820152606401610430565b565b6106976108c6565b61068d5f6108f3565b60606004805461032890610c56565b5f336103b6818585610869565b6106c46108c6565b5f81116107135760405162461bcd60e51b815260206004820152601d60248201527f416d6f756e74206d7573742062652067726561746572207468616e20300000006044820152606401610430565b804710156107635760405162461bcd60e51b815260206004820152601c60248201527f496e73756666696369656e742045544820696e20636f6e7472616374000000006044820152606401610430565b6005546040516001600160a01b039091169082156108fc029083905f818181858888f1935050505015801561079a573d5f5f3e3d5ffd5b5050565b6107a66108c6565b6001600160a01b0381166107cf57604051631e4fbdf760e01b81525f6004820152602401610430565b6107d8816108f3565b50565b6107e88383836001610944565b505050565b6001600160a01b038381165f908152600160209081526040808320938616835292905220545f19811015610863578181101561085557604051637dc7a0d960e11b81526001600160a01b03841660048201526024810182905260448101839052606401610430565b61086384848484035f610944565b50505050565b6001600160a01b03831661089257604051634b637e8f60e11b81525f6004820152602401610430565b6001600160a01b0382166108bb5760405163ec442f0560e01b81525f6004820152602401610430565b6107e8838383610a16565b6005546001600160a01b0316331461068d5760405163118cdaa760e01b8152336004820152602401610430565b600580546001600160a01b038381166001600160a01b0319831681179093556040519116919082907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0905f90a35050565b6001600160a01b03841661096d5760405163e602df0560e01b81525f6004820152602401610430565b6001600160a01b03831661099657604051634a1406b160e11b81525f6004820152602401610430565b6001600160a01b038085165f908152600160209081526040808320938716835292905220829055801561086357826001600160a01b0316846001600160a01b03167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92584604051610a0891815260200190565b60405180910390a350505050565b6001600160a01b038316610a40578060025f828254610a359190610cd8565b90915550610ab09050565b6001600160a01b0383165f9081526020819052604090205481811015610a925760405163391434e360e21b81526001600160a01b03851660048201526024810182905260448101839052606401610430565b6001600160a01b0384165f9081526020819052604090209082900390555b6001600160a01b038216610acc57600280548290039055610aea565b6001600160a01b0382165f9081526020819052604090208054820190555b816001600160a01b0316836001600160a01b03167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef83604051610b2f91815260200190565b60405180910390a3505050565b602081525f82518060208401528060208501604085015e5f604082850101526040601f19601f83011684010191505092915050565b80356001600160a01b0381168114610b87575f5ffd5b919050565b5f5f60408385031215610b9d575f5ffd5b610ba683610b71565b946020939093013593505050565b5f5f5f60608486031215610bc6575f5ffd5b610bcf84610b71565b9250610bdd60208501610b71565b929592945050506040919091013590565b5f60208284031215610bfe575f5ffd5b5035919050565b5f60208284031215610c15575f5ffd5b610c1e82610b71565b9392505050565b5f5f60408385031215610c36575f5ffd5b610c3f83610b71565b9150610c4d60208401610b71565b90509250929050565b600181811c90821680610c6a57607f821691505b602082108103610c8857634e487b7160e01b5f52602260045260245ffd5b50919050565b634e487b7160e01b5f52601160045260245ffd5b80820281158282048414176103bc576103bc610c8e565b5f82610cd357634e487b7160e01b5f52601260045260245ffd5b500490565b808201808211156103bc576103bc610c8e56fea2646970667358221220ac38834c0c81e47dcb1a0f2c7f35efc909b932bc901c5cb9d602795f77dbe88264736f6c634300081d0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_DEPOSITETH = "depositEth";

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

    public RemoteFunctionCall<TransactionReceipt> depositEth(BigInteger weiValue) {
        final Function function = new Function(
                FUNC_DEPOSITETH, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
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

    public RemoteFunctionCall<TransactionReceipt> withdrawEth(BigInteger amount) {
        final Function function = new Function(
                FUNC_WITHDRAWETH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amount)), 
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
