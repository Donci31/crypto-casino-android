// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import "./CasinoWallet.sol";
import "./ProvableFairness.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title SlotMachine
 * @dev Implements a provably fair slot machine game
 */
contract SlotMachine is Ownable, IRandomnessReceiver {
    
    // References to other contracts
    CasinoWallet private wallet;
    ProvableFairness private fairness;
    
    // Constants for slot machine configuration
    uint8 private constant NUM_REELS = 3;
    uint8 private constant NUM_SYMBOLS = 5;
    
    // Symbol types (0-4)
    // 0: Cherry, 1: Lemon, 2: Orange, 3: Star, 4: Seven
    
    // Payout multipliers for different symbol combinations
    // Index represents the symbol type
    uint256[] private payoutMultipliers;
    
    // Game state mapping
    struct GameState {
        address player;
        uint256 betAmount;
        bool settled;
        uint256[] reelResults;
        uint256 winAmount;
        uint256 timestamp;
    }
    
    // Mapping from gameId to game state
    mapping(bytes32 => GameState) private games;
    
    // Mapping from request ID to game ID
    mapping(uint256 => bytes32) private requestToGame;
    
    // Events
    event GameStarted(address indexed player, bytes32 indexed gameId, uint256 betAmount);
    event GameResult(bytes32 indexed gameId, uint256[] reelResults, uint256 winAmount);
    event WinningPaid(address indexed player, bytes32 indexed gameId, uint256 amount);
    
    /**
     * @dev Constructor sets initial parameters
     * @param walletAddress Address of the CasinoWallet contract
     * @param fairnessAddress Address of the ProvableFairness contract
     */
    constructor(address walletAddress, address fairnessAddress) Ownable(msg.sender) {
        wallet = CasinoWallet(walletAddress);
        fairness = ProvableFairness(fairnessAddress);
        
        // Set payout multipliers for each symbol combination
        // These can be adjusted based on the desired house edge
        payoutMultipliers = new uint256[](NUM_SYMBOLS);
        payoutMultipliers[0] = 2;   // Cherry: 2x
        payoutMultipliers[1] = 3;   // Lemon: 3x
        payoutMultipliers[2] = 5;   // Orange: 5x
        payoutMultipliers[3] = 10;  // Star: 10x
        payoutMultipliers[4] = 25;  // Seven: 25x
    }
    
    /**
     * @dev Start a new slot machine game
     * @param betAmount Amount to bet
     * @return gameId Unique identifier for this game
     */
    function spin(uint256 betAmount) external returns (bytes32) {
        require(betAmount > 0, "Bet must be greater than zero");
        
        // Generate a unique game ID
        bytes32 gameId = keccak256(abi.encodePacked(
            msg.sender,
            block.timestamp,
            block.prevrandao,
            betAmount
        ));
        
        // Ensure the game ID is unique
        require(games[gameId].timestamp == 0, "Game ID already exists");
        
        // Place the bet through the wallet contract
        require(wallet.placeBet(msg.sender, betAmount, gameId), "Failed to place bet");
        
        // Initialize game state
        GameState storage newGame = games[gameId];
        newGame.player = msg.sender;
        newGame.betAmount = betAmount;
        newGame.settled = false;
        newGame.timestamp = block.timestamp;
        
        // Request randomness
        uint256 requestId = fairness.requestRandomness(msg.sender, gameId);
        requestToGame[requestId] = gameId;
        
        emit GameStarted(msg.sender, gameId, betAmount);
        
        return gameId;
    }
    
    /**
     * @dev Receive randomness from the ProvableFairness contract
     * @param requestId The ID of the randomness request
     * @param randomWords The random values
     * @param player The player's address
     * @param gameId The game ID
     */
    function receiveRandomness(
        uint256 requestId,
        uint256[] memory randomWords,
        address player,
        bytes32 gameId
    ) external override {
        // Ensure the caller is the fairness contract
        require(msg.sender == address(fairness), "Caller is not the fairness contract");
        
        // Ensure the game ID matches what we expect
        require(requestToGame[requestId] == gameId, "Game ID mismatch");
        
        // Retrieve the game state
        GameState storage game = games[gameId];
        
        // Ensure the game is valid and unsettled
        require(game.player == player, "Player mismatch");
        require(!game.settled, "Game already settled");
        
        // Use the random value to determine reel results
        uint256 randomValue = randomWords[0];
        uint256[] memory reelResults = new uint256[](NUM_REELS);
        
        // Determine result for each reel
        for (uint8 i = 0; i < NUM_REELS; i++) {
            randomValue = uint256(keccak256(abi.encodePacked(randomValue, i)));
            reelResults[i] = randomValue % NUM_SYMBOLS;
        }
        
        // Store the results
        game.reelResults = reelResults;
        
        // Calculate and store win amount
        uint256 winAmount = calculateWin(reelResults, game.betAmount);
        game.winAmount = winAmount;
        
        // Mark the game as settled
        game.settled = true;
        
        // Pay winnings if any
        if (winAmount > 0) {
            require(wallet.payWinnings(player, winAmount, gameId), "Failed to pay winnings");
            emit WinningPaid(player, gameId, winAmount);
        }
        
        // Clean up
        delete requestToGame[requestId];
        
        // Emit event with results
        emit GameResult(gameId, reelResults, winAmount);
    }
    
    /**
     * @dev Calculate win amount based on reel results and bet amount
     * @param reelResults Array of reel results
     * @param betAmount Amount that was bet
     * @return uint256 Win amount
     */
    function calculateWin(uint256[] memory reelResults, uint256 betAmount) private view returns (uint256) {
        // Check if all reels show the same symbol
        bool allSame = true;
        for (uint8 i = 1; i < reelResults.length; i++) {
            if (reelResults[i] != reelResults[0]) {
                allSame = false;
                break;
            }
        }
        
        // If all symbols are the same, pay according to the symbol's multiplier
        if (allSame) {
            uint256 symbolIndex = reelResults[0];
            return betAmount * payoutMultipliers[symbolIndex];
        }
        
        // Additional winning patterns could be implemented here
        // For example, certain symbols appearing in certain positions
        
        // Default: no win
        return 0;
    }
    
    /**
     * @dev Get the state of a game
     * @param gameId ID of the game to check
     * @return player Address of the player
     * @return betAmount Amount that was bet
     * @return settled Whether the game is settled
     * @return reelResults Array of reel results (empty if game not settled)
     * @return winAmount Amount won (0 if not won or not settled)
     */
    function getGameState(bytes32 gameId) external view returns (
        address player,
        uint256 betAmount,
        bool settled,
        uint256[] memory reelResults,
        uint256 winAmount
    ) {
        GameState storage game = games[gameId];
        return (
            game.player,
            game.betAmount,
            game.settled,
            game.reelResults,
            game.winAmount
        );
    }
    
    /**
     * @dev Update payout multipliers (owner only)
     * @param symbol Symbol index to update
     * @param multiplier New payout multiplier
     */
    function updatePayoutMultiplier(uint8 symbol, uint256 multiplier) external onlyOwner {
        require(symbol < NUM_SYMBOLS, "Invalid symbol index");
        require(multiplier > 0, "Multiplier must be greater than zero");
        
        payoutMultipliers[symbol] = multiplier;
    }
    
    /**
     * @dev Get the current payout multiplier for a symbol
     * @param symbol Symbol index
     * @return uint256 Payout multiplier
     */
    function getPayoutMultiplier(uint8 symbol) external view returns (uint256) {
        require(symbol < NUM_SYMBOLS, "Invalid symbol index");
        return payoutMultipliers[symbol];
    }
}