// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "./CasinoToken.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title CasinoWallet
 * @dev Manages user balances and transactions for the Casino with master wallet architecture
 */
contract CasinoWallet is Ownable {
    
    CasinoToken private token;
    
    // Game contracts authorized to interact with the wallet
    mapping(address => bool) public authorizedGames;

    struct User {
        uint256 balance;
        uint256 totalBets;
        uint256 totalWins;
        bool registered;
    }

    mapping(address => User) private users;

    event UserRegistered(address indexed userAddress, address indexed operator);
    event Deposit(address indexed userAddress, uint256 amount, address indexed operator);
    event Withdrawal(address indexed userAddress, uint256 amount, address indexed operator);
    event BetPlaced(address indexed userAddress, uint256 amount, bytes32 gameId, address indexed gameContract);
    event WinPaid(address indexed userAddress, uint256 amount, bytes32 gameId, address indexed gameContract);
    event GameAuthorized(address indexed gameAddress, address indexed operator);
    event GameDeauthorized(address indexed gameAddress, address indexed operator);

    // Modifiers
    modifier onlyAuthorizedGame() {
        require(authorizedGames[msg.sender], "Not an authorized game contract");
        _;
    }

    constructor(address tokenAddress) Ownable(msg.sender) {
        token = CasinoToken(tokenAddress);
    }
    
    /**
     * @dev Authorize a game contract to interact with this wallet
     * @param gameAddress Address of the game contract
     */
    function authorizeGame(address gameAddress) external onlyOwner {
        require(gameAddress != address(0), "Invalid game address");
        require(!authorizedGames[gameAddress], "Game already authorized");
        
        authorizedGames[gameAddress] = true;
        
        emit GameAuthorized(gameAddress, msg.sender);
    }
    
    /**
     * @dev Deauthorize a game contract
     * @param gameAddress Address of the game contract
     */
    function deauthorizeGame(address gameAddress) external onlyOwner {
        require(authorizedGames[gameAddress], "Game not authorized");
        
        authorizedGames[gameAddress] = false;
        
        emit GameDeauthorized(gameAddress, msg.sender);
    }
    
    /**
     * @dev Register a new user (only callable by casino operator)
     * @param userAddress Address of the user to register
     */
    function registerUser(address userAddress) external onlyOwner {
        require(!users[userAddress].registered, "User already registered");
        require(userAddress != address(0), "Invalid user address");
        
        User storage newUser = users[userAddress];
        newUser.registered = true;
        newUser.balance = 0;
        
        emit UserRegistered(userAddress, msg.sender);
    }
    
    /**
     * @dev Check if user is registered
     * @param userAddress Address of the user
     * @return bool True if user is registered
     */
    function isRegistered(address userAddress) external view returns (bool) {
        return users[userAddress].registered;
    }
    
    /**
     * @dev Deposit tokens into the casino wallet for a user
     * @param userAddress Address of the user
     * @param amount Amount of tokens to deposit
     */
    function depositFor(address userAddress, uint256 amount) external onlyOwner {
        require(users[userAddress].registered, "User not registered");
        require(amount > 0, "Amount must be greater than 0");
        require(token.transferFrom(userAddress, address(this), amount), "Token transfer failed");
        
        users[userAddress].balance += amount;
        
        emit Deposit(userAddress, amount, msg.sender);
    }
    
    /**
     * @dev Withdraw tokens from the casino wallet for a user
     * @param userAddress Address of the user
     * @param amount Amount of tokens to withdraw
     */
    function withdrawFor(address userAddress, uint256 amount) external onlyOwner {
        require(users[userAddress].registered, "User not registered");
        require(amount > 0, "Amount must be greater than 0");
        require(users[userAddress].balance >= amount, "Insufficient balance");
        
        users[userAddress].balance -= amount;
        
        require(token.transfer(userAddress, amount), "Token transfer failed");
        
        emit Withdrawal(userAddress, amount, msg.sender);
    }
    
    /**
     * @dev Get user balance
     * @param userAddress Address of the user
     * @return uint256 User's balance
     */
    function getBalance(address userAddress) external view returns (uint256) {
        require(users[userAddress].registered, "User not registered");
        return users[userAddress].balance;
    }
    
    /**
     * @dev Get user statistics
     * @param userAddress Address of the user
     * @return uint256 totalBets, uint256 totalWins
     */
    function getUserStats(address userAddress) external view returns (uint256, uint256) {
        require(users[userAddress].registered, "User not registered");
        return (users[userAddress].totalBets, users[userAddress].totalWins);
    }
    
    /**
     * @dev Place a bet (can only be called by authorized game contracts)
     * @param userAddress Address of the user placing the bet
     * @param amount Amount of tokens to bet
     * @param gameId Unique identifier for the game session
     * @return bool Success of the bet placement
     */
    function placeBet(address userAddress, uint256 amount, bytes32 gameId) external onlyAuthorizedGame returns (bool) {
        require(users[userAddress].registered, "User not registered");
        require(amount > 0, "Bet amount must be greater than 0");
        require(users[userAddress].balance >= amount, "Insufficient balance");
        
        users[userAddress].balance -= amount;
        users[userAddress].totalBets += amount;
        
        emit BetPlaced(userAddress, amount, gameId, msg.sender);
        
        return true;
    }
    
    /**
     * @dev Pay winnings to a user (can only be called by authorized game contracts)
     * @param userAddress Address of the winner
     * @param amount Amount of tokens won
     * @param gameId Unique identifier for the game session
     * @return bool Success of the payment
     */
    function payWinnings(address userAddress, uint256 amount, bytes32 gameId) external onlyAuthorizedGame returns (bool) {
        require(users[userAddress].registered, "User not registered");
        require(amount > 0, "Win amount must be greater than 0");
        
        users[userAddress].balance += amount;
        users[userAddress].totalWins += amount;
        
        emit WinPaid(userAddress, amount, gameId, msg.sender);
        
        return true;
    }
}
