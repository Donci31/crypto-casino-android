// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "./CasinoToken.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";


contract CasinoVault is Ownable, ReentrancyGuard {
    using SafeERC20 for IERC20;

    IERC20 public immutable token;

    mapping(address => bool) public authorizedGames;

    mapping(address => uint256) public balances;

    event GameAuthorized(address indexed gameAddress, uint256 timestamp);
    event GameDeauthorized(address indexed gameAddress, uint256 timestamp);
    event Deposit(address indexed userAddress, uint256 amount, uint256 newBalance, uint256 timestamp);
    event Withdrawal(address indexed userAddress, uint256 amount, uint256 newBalance, uint256 timestamp);
    event BetPlaced(address indexed userAddress, uint256 amount, uint256 newBalance, address indexed gameAddress, uint256 timestamp);
    event WinPaid(address indexed userAddress, uint256 amount, uint256 newBalance, address indexed gameAddress, uint256 timestamp);

    modifier onlyAuthorizedGame() {
        require(authorizedGames[msg.sender], "TokenGameVault: not an authorized game");
        _;
    }

    modifier validAmount(uint256 amount) {
        require(amount > 0, "TokenGameVault: amount must be greater than 0");
        _;
    }
    
    modifier validAddress(address addr) {
        require(addr != address(0), "TokenGameVault: invalid address");
        _;
    }

    constructor(address tokenAddress) Ownable(msg.sender) validAddress(tokenAddress) {
        token = IERC20(tokenAddress);
    }

    function authorizeGame(address gameAddress) external onlyOwner validAddress(gameAddress) {
        require(!authorizedGames[gameAddress], "TokenGameVault: game already authorized");
        
        authorizedGames[gameAddress] = true;
        emit GameAuthorized(gameAddress, block.timestamp);
    }

    function deauthorizeGame(address gameAddress) external onlyOwner {
        require(authorizedGames[gameAddress], "TokenGameVault: game not authorized");
        
        authorizedGames[gameAddress] = false;
        emit GameDeauthorized(gameAddress, block.timestamp);
    }

    function deposit(uint256 amount) external nonReentrant validAmount(amount) {
        token.safeTransferFrom(msg.sender, address(this), amount);

        balances[msg.sender] += amount;
        
        emit Deposit(msg.sender, amount, balances[msg.sender], block.timestamp);
    }

    function withdraw(uint256 amount) external nonReentrant validAmount(amount) {
        require(balances[msg.sender] >= amount, "TokenGameVault: insufficient balance");

        balances[msg.sender] -= amount;

        token.safeTransfer(msg.sender, amount);
        
        emit Withdrawal(msg.sender, amount, balances[msg.sender], block.timestamp);
    }

    function placeBet(address player, uint256 amount) 
        external 
        onlyAuthorizedGame 
        validAmount(amount)
        validAddress(player)
        returns (bool) 
    {
        require(balances[player] >= amount, "TokenGameVault: insufficient balance");
        
        balances[player] -= amount;
        emit BetPlaced(player, amount, balances[player], msg.sender, block.timestamp);
        
        return true;
    }

    function payWinnings(address player, uint256 amount) 
        external 
        onlyAuthorizedGame 
        validAmount(amount)
        validAddress(player)
        returns (bool) 
    {
        balances[player] += amount;
        emit WinPaid(player, amount, balances[player], msg.sender, block.timestamp);
        
        return true;
    }

    function getBalance(address player) external view returns (uint256) {
        return balances[player];
    }

    function isGameAuthorized(address gameAddress) external view returns (bool) {
        return authorizedGames[gameAddress];
    }
}
