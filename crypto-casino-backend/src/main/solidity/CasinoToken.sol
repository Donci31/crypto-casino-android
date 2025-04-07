// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title CasinoToken
 * @dev ERC20 Token for the Crypto Casino with master wallet architecture
 * This will be used for transactions within the casino platform
 */
contract CasinoToken is ERC20, Ownable {
    
    // Events
    event TokensPurchased(address indexed buyer, uint256 amount, address indexed operator);
    event TokensWithdrawn(address indexed player, uint256 amount, address indexed operator);
    
    // Constructor sets the token name and symbol
    constructor() ERC20("CasinoToken", "CST") Ownable(msg.sender) {
        // Mint initial supply to the contract owner (casino)
        _mint(msg.sender, 1000000 * 10 ** decimals());
    }
    
    /**
     * @dev Allows the casino operator to purchase tokens for a user
     * @param user Address of the user to receive tokens
     */
    function purchaseTokensFor(address user) external payable onlyOwner {
        require(msg.value > 0, "Must send ETH to purchase tokens");
        require(user != address(0), "Invalid user address");
        
        // Simple 1:100 conversion (1 ETH = 100 CST)
        uint256 tokenAmount = msg.value * 100;
        
        // Transfer tokens to the user
        _transfer(owner(), user, tokenAmount);
        
        emit TokensPurchased(user, tokenAmount, msg.sender);
    }
    
    /**
     * @dev Allows the casino operator to withdraw user tokens back to ETH
     * @param user Address of the user exchanging tokens
     * @param amount The amount of tokens to withdraw
     */
    function withdrawTokensFor(address user, uint256 amount) external onlyOwner {
        require(amount > 0, "Amount must be greater than 0");
        require(balanceOf(user) >= amount, "Insufficient balance");
        
        // Calculate ETH amount (reverse of purchase rate)
        uint256 ethAmount = amount / 100;
        
        // Check if contract has enough ETH
        require(address(this).balance >= ethAmount, "Insufficient ETH in contract");
        
        // Transfer tokens from user to owner (requires allowance)
        transferFrom(user, owner(), amount);
        
        // Transfer ETH to user
        payable(user).transfer(ethAmount);
        
        emit TokensWithdrawn(user, amount, msg.sender);
    }
    
    /**
     * @dev Allows the owner to withdraw ETH from the contract
     */
    function withdrawEth() external onlyOwner {
        uint256 balance = address(this).balance;
        require(balance > 0, "No ETH to withdraw");
        
        payable(owner()).transfer(balance);
    }
}
