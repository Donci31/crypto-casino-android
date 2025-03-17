// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title CasinoToken
 * @dev ERC20 Token for the Crypto Casino
 * This will be used for transactions within the casino platform
 */
contract CasinoToken is ERC20, Ownable {
    
    // Events
    event TokensPurchased(address indexed buyer, uint256 amount);
    event TokensWithdrawn(address indexed player, uint256 amount);
    
    // Constructor sets the token name and symbol
    constructor() ERC20("CasinoToken", "CST") Ownable(msg.sender) {
        // Mint initial supply to the contract owner (casino)
        _mint(msg.sender, 1000000 * 10 ** decimals());
    }
    
    /**
     * @dev Allows users to purchase tokens with Ether
     * Exchange rate can be adjusted as needed
     */
    function purchaseTokens() external payable {
        require(msg.value > 0, "Must send ETH to purchase tokens");
        
        // Simple 1:100 conversion (1 ETH = 100 CST)
        uint256 tokenAmount = msg.value * 100;
        
        // Transfer tokens to the buyer
        _transfer(owner(), msg.sender, tokenAmount);
        
        emit TokensPurchased(msg.sender, tokenAmount);
    }
    
    /**
     * @dev Allows users to withdraw their tokens back to ETH
     * @param amount The amount of tokens to withdraw
     */
    function withdrawTokens(uint256 amount) external {
        require(amount > 0, "Amount must be greater than 0");
        require(balanceOf(msg.sender) >= amount, "Insufficient balance");
        
        // Calculate ETH amount (reverse of purchase rate)
        uint256 ethAmount = amount / 100;
        
        // Check if contract has enough ETH
        require(address(this).balance >= ethAmount, "Insufficient ETH in contract");
        
        // Transfer tokens from user to owner
        _transfer(msg.sender, owner(), amount);
        
        // Transfer ETH to user
        payable(msg.sender).transfer(ethAmount);
        
        emit TokensWithdrawn(msg.sender, amount);
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
