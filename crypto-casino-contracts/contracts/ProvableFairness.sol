// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@chainlink/contracts/src/v0.8/vrf/VRFConsumerBaseV2.sol";
import "@chainlink/contracts/src/v0.8/vrf/interfaces/VRFCoordinatorV2Interface.sol";

/**
 * @title ProvableFairness
 * @dev Contract for providing provably fair random numbers for casino games
 * Uses Chainlink VRF for secure randomness
 */
contract ProvableFairness is VRFConsumerBaseV2, Ownable {
    
    VRFCoordinatorV2Interface private immutable coordinator;
    
    // Chainlink VRF subscription ID
    uint64 private immutable subscriptionId;
    
    // Hash of the gas lane to use for the VRF requests
    bytes32 private immutable keyHash;
    
    // Callback gas limit
    uint32 private immutable callbackGasLimit;
    
    // Number of confirmations required before the random number is finalized
    uint16 private immutable requestConfirmations;
    
    // Number of random values to request per request
    uint32 private immutable numWords;
    
    // Mapping from requestId to player address
    mapping(uint256 => address) private requestToPlayer;
    
    // Mapping from requestId to game contract
    mapping(uint256 => address) private requestToGame;
    
    // Mapping from requestId to gameId
    mapping(uint256 => bytes32) private requestToGameId;
    
    // Mapping from player address to client seed
    mapping(address => bytes32) private clientSeeds;
    
    // Mapping to store server seeds
    mapping(bytes32 => bytes32) private serverSeeds;
    
    // Events
    event RandomnessRequested(uint256 indexed requestId, address indexed player, bytes32 gameId);
    event RandomnessReceived(uint256 indexed requestId, uint256[] randomWords);
    event ClientSeedSet(address indexed player, bytes32 hashedSeed);
    event ServerSeedRevealed(bytes32 indexed gameId, bytes32 serverSeed);
    
    // Authorized game contracts that can request randomness
    mapping(address => bool) private authorizedGames;
    
    // Modifier to check if a game contract is authorized
    modifier onlyAuthorizedGame() {
        require(authorizedGames[msg.sender], "Caller is not an authorized game");
        _;
    }
    
    /**
     * @dev Constructor initializes Chainlink VRF parameters
     * @param _vrfCoordinator VRF Coordinator address
     * @param _subscriptionId Chainlink subscription ID
     * @param _keyHash Gas lane to use for VRF requests
     */
    constructor(
        address _vrfCoordinator,
        uint64 _subscriptionId,
        bytes32 _keyHash
    ) VRFConsumerBaseV2(_vrfCoordinator) Ownable(msg.sender) {
        coordinator = VRFCoordinatorV2Interface(_vrfCoordinator);
        subscriptionId = _subscriptionId;
        keyHash = _keyHash;
        callbackGasLimit = 100000; // Adjust based on needs
        requestConfirmations = 3;  // Recommended minimum
        numWords = 1;              // Default to one random number
    }
    
    /**
     * @dev Sets a client seed for a player
     * @param hashedSeed Keccak256 hash of the seed chosen by the player
     */
    function setClientSeed(bytes32 hashedSeed) external {
        require(hashedSeed != bytes32(0), "Invalid seed hash");
        clientSeeds[msg.sender] = hashedSeed;
        
        emit ClientSeedSet(msg.sender, hashedSeed);
    }
    
    /**
     * @dev Get the client seed for a player
     * @param player Address of the player
     * @return bytes32 Hashed client seed
     */
    function getClientSeed(address player) external view returns (bytes32) {
        return clientSeeds[player];
    }
    
    /**
     * @dev Request randomness for a game
     * Can only be called by authorized game contracts
     * @param player Address of the player
     * @param gameId Unique identifier for the game session
     * @return requestId The ID of the VRF request
     */
    function requestRandomness(address player, bytes32 gameId) external onlyAuthorizedGame returns (uint256) {
        // Ensure the player has set a client seed
        require(clientSeeds[player] != bytes32(0), "Client seed not set");
        
        // Generate a server seed for this game and store its hash
        bytes32 serverSeed = keccak256(abi.encodePacked(block.timestamp, player, block.prevrandao));
        serverSeeds[gameId] = serverSeed;
        
        // Request randomness from Chainlink VRF
        uint256 requestId = coordinator.requestRandomWords(
            keyHash,
            subscriptionId,
            requestConfirmations,
            callbackGasLimit,
            numWords
        );
        
        // Store request information
        requestToPlayer[requestId] = player;
        requestToGame[requestId] = msg.sender;
        requestToGameId[requestId] = gameId;
        
        emit RandomnessRequested(requestId, player, gameId);
        
        return requestId;
    }
    
    /**
     * @dev Callback function used by Chainlink VRF to deliver randomness
     * @param requestId The ID of the request
     * @param randomWords The random values from Chainlink VRF
     */
    function fulfillRandomWords(uint256 requestId, uint256[] memory randomWords) internal override {
        address player = requestToPlayer[requestId];
        address gameContract = requestToGame[requestId];
        bytes32 gameId = requestToGameId[requestId];
        
        // Ensure we have a valid request
        require(player != address(0), "Invalid request ID");
        require(gameContract != address(0), "Invalid game contract");
        
        // Combine VRF randomness with client seed for additional entropy
        uint256 combinedRandom = uint256(keccak256(abi.encodePacked(
            randomWords[0],
            clientSeeds[player]
        )));
        
        // Create an array with the combined random number
        uint256[] memory resultWords = new uint256[](1);
        resultWords[0] = combinedRandom;
        
        emit RandomnessReceived(requestId, resultWords);
        
        // Call the receiving function on the game contract
        IRandomnessReceiver(gameContract).receiveRandomness(requestId, resultWords, player, gameId);
        
        // Clean up mappings
        delete requestToPlayer[requestId];
        delete requestToGame[requestId];
        delete requestToGameId[requestId];
    }
    
    /**
     * @dev Reveals the server seed for verification after a game is complete
     * @param gameId The ID of the completed game
     * @return bytes32 The server seed
     */
    function revealServerSeed(bytes32 gameId) external returns (bytes32) {
        bytes32 serverSeed = serverSeeds[gameId];
        require(serverSeed != bytes32(0), "Server seed not found");
        
        emit ServerSeedRevealed(gameId, serverSeed);
        
        // Clean up
        delete serverSeeds[gameId];
        
        return serverSeed;
    }
    
    /**
     * @dev Authorize a game contract to request randomness
     * @param gameAddress Address of the game contract
     */
    function authorizeGame(address gameAddress) external onlyOwner {
        authorizedGames[gameAddress] = true;
    }
    
    /**
     * @dev Revoke authorization from a game contract
     * @param gameAddress Address of the game contract
     */
    function revokeGameAuthorization(address gameAddress) external onlyOwner {
        authorizedGames[gameAddress] = false;
    }
    
    /**
     * @dev Check if a game is authorized
     * @param gameAddress Address of the game contract
     * @return bool True if game is authorized
     */
    function isGameAuthorized(address gameAddress) external view returns (bool) {
        return authorizedGames[gameAddress];
    }
}

/**
 * @dev Interface for contracts that receive randomness
 */
interface IRandomnessReceiver {
    function receiveRandomness(
        uint256 requestId,
        uint256[] memory randomWords,
        address player,
        bytes32 gameId
    ) external;
}
