// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import "./CasinoToken.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title CasinoWallet
 * @dev Manages user balances and transactions for the Casino
 */
contract CasinoWallet is Ownable {
    
    CasinoToken private token;
    
    // User data structure
    struct User {
        uint256 balance;
        uint256 totalBets;
        uint256 totalWins;
        bool registered;
        mapping(address => bool) authorizedAddresses;
    }
    
    // Mapping from user address to User struct
    mapping(address => User) private users;
    
    // Events
    event UserRegistered(address indexed userAddress);
    event Deposit(address indexed userAddress, uint256 amount);
    event Withdrawal(address indexed userAddress, uint256 amount);
    event BetPlaced(address indexed userAddress, uint256 amount, bytes32 gameId);
    event WinPaid(address indexed userAddress, uint256 amount, bytes32 gameId);
    
    // Allowed game contracts that can call this wallet
    mapping(address => bool) private authorizedGames;
    
    // Modifier to check if a game contract is authorized
    modifier onlyAuthorizedGame() {
        require(authorizedGames[msg.sender], "Caller is not an authorized game");
        _;
    }
    
    // Constructor initializes with the Casino Token contract
    constructor(address tokenAddress) Ownable(msg.sender) {
        token = CasinoToken(tokenAddress);
    }
    
    /**
     * @dev Register a new user
     */
    function registerUser() external {
        require(!users[msg.sender].registered, "User already registered");
        
        User storage newUser = users[msg.sender];
        newUser.registered = true;
        newUser.balance = 0;
        
        emit UserRegistered(msg.sender);
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
     * @dev Deposit tokens into the casino wallet
     * @param amount Amount of tokens to deposit
     */
    function deposit(uint256 amount) external {
        require(users[msg.sender].registered, "User not registered");
        require(amount > 0, "Amount must be greater than 0");
        require(token.transferFrom(msg.sender, address(this), amount), "Token transfer failed");
        
        users[msg.sender].balance += amount;
        
        emit Deposit(msg.sender, amount);
    }
    
    /**
     * @dev Withdraw tokens from the casino wallet
     * @param amount Amount of tokens to withdraw
     */
    function withdraw(uint256 amount) external {
        require(users[msg.sender].registered, "User not registered");
        require(amount > 0, "Amount must be greater than 0");
        require(users[msg.sender].balance >= amount, "Insufficient balance");
        
        users[msg.sender].balance -= amount;
        
        require(token.transfer(msg.sender, amount), "Token transfer failed");
        
        emit Withdrawal(msg.sender, amount);
    }
    
    /**
     * @dev Get user balance
     * @return uint256 User's balance
     */
    function getBalance() external view returns (uint256) {
        require(users[msg.sender].registered, "User not registered");
        return users[msg.sender].balance;
    }
    
    /**
     * @dev Get user statistics
     * @return uint256 totalBets, uint256 totalWins
     */
    function getUserStats() external view returns (uint256, uint256) {
        require(users[msg.sender].registered, "User not registered");
        return (users[msg.sender].totalBets, users[msg.sender].totalWins);
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
        
        emit BetPlaced(userAddress, amount, gameId);
        
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
        
        emit WinPaid(userAddress, amount, gameId);
        
        return true;
    }
    
    /**
     * @dev Authorize a game contract to interact with this wallet
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
