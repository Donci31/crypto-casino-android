// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract CasinoToken is ERC20, Ownable {
    uint256 public rate = 1000;

    event TokensPurchased(address indexed userAddress, uint256 tokenAmount, uint256 timestamp);
    event TokensExchanged(address indexed userAddress, uint256 tokenAmount, uint256 timestamp);

    constructor() ERC20("CasinoToken", "CST") Ownable(msg.sender) {
        _mint(msg.sender, 10000000 * 10 ** decimals());
    }

    function purchaseTokens() external payable {
        require(msg.value > 0, "Must send ETH to purchase tokens");

        uint256 tokenAmount = msg.value * rate;

        _transfer(owner(), msg.sender, tokenAmount);

        emit TokensPurchased(msg.sender, tokenAmount, block.timestamp);
    }

    function exchangeTokens(uint256 tokenAmount) external {
        require(tokenAmount > 0, "Amount must be greater than 0");
        require(balanceOf(msg.sender) >= tokenAmount, "Insufficient token balance");

        uint256 ethAmount = tokenAmount / rate;

        require(address(this).balance >= ethAmount, "Insufficient ETH in contract");

        _transfer(msg.sender, owner(), tokenAmount);

        payable(msg.sender).transfer(ethAmount);

        emit TokensExchanged(msg.sender, tokenAmount, block.timestamp);
    }

    function depositEth() external payable onlyOwner {
        require(msg.value > 0, "Must send ETH to deposit");
    }

    function withdrawEth(uint256 amount) external onlyOwner {
        require(amount > 0, "Amount must be greater than 0");
        require(address(this).balance >= amount, "Insufficient ETH in contract");

        payable(owner()).transfer(amount);
    }
}
