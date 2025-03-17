// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "./CasinoToken.sol";
import "./CasinoWallet.sol";
import "./ProvableFairness.sol";
import "./SlotMachine.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

/**
 * @title CasinoFactory
 * @dev Factory contract for deploying the simplified casino with only slot machine
 * This is the main contract that will be used to deploy the casino for the first semester
 */
contract CasinoFactory is Ownable {
    
    // Contract addresses
    address public tokenAddress;
    address public walletAddress;
    address public fairnessAddress;
    address public slotMachineAddress;
    
    // Events
    event CasinoDeployed(
        address tokenAddress,
        address walletAddress,
        address fairnessAddress
    );
    
    event GameDeployed(
        string gameType,
        address gameAddress
    );
    
    /**
     * @dev Constructor
     */
    constructor() Ownable(msg.sender) {}
    
    /**
     * @dev Deploy the core casino contracts
     * @param _vrfCoordinator VRF Coordinator address for Chainlink
     * @param _subscriptionId Chainlink subscription ID
     * @param _keyHash Gas lane to use for VRF requests
     */
    function deployCasino(
        address _vrfCoordinator,
        uint64 _subscriptionId,
        bytes32 _keyHash
    ) external onlyOwner {
        // Deploy core contracts
        
        // 1. Deploy token contract
        CasinoToken token = new CasinoToken();
        tokenAddress = address(token);
        
        // 2. Deploy wallet contract
        CasinoWallet wallet = new CasinoWallet(tokenAddress);
        walletAddress = address(wallet);
        
        // 3. Deploy fairness contract
        ProvableFairness fairness = new ProvableFairness(
            _vrfCoordinator,
            _subscriptionId,
            _keyHash
        );
        fairnessAddress = address(fairness);
        
        emit CasinoDeployed(tokenAddress, walletAddress, fairnessAddress);
    }
    
    /**
     * @dev Deploy slot machine game
     */
    function deploySlotMachine() external onlyOwner {
        require(walletAddress != address(0), "Wallet not deployed");
        require(fairnessAddress != address(0), "Fairness not deployed");
        
        // Deploy slot machine contract
        SlotMachine slotMachine = new SlotMachine(walletAddress, fairnessAddress);
        slotMachineAddress = address(slotMachine);
        
        // Authorize the game in both wallet and fairness systems
        CasinoWallet(walletAddress).authorizeGame(slotMachineAddress);
        ProvableFairness(fairnessAddress).authorizeGame(slotMachineAddress);
        
        // Transfer ownership to the caller
        slotMachine.transferOwnership(owner());
        
        emit GameDeployed("SlotMachine", slotMachineAddress);
    }
    
    /**
     * @dev Transfer ownership of all deployed contracts to a new owner
     * @param newOwner Address of the new owner
     */
    function transferAllOwnership(address newOwner) external onlyOwner {
        require(newOwner != address(0), "New owner cannot be zero address");
        
        // Transfer ownership of core contracts
        if (tokenAddress != address(0)) {
            CasinoToken(tokenAddress).transferOwnership(newOwner);
        }
        
        if (walletAddress != address(0)) {
            CasinoWallet(walletAddress).transferOwnership(newOwner);
        }
        
        if (fairnessAddress != address(0)) {
            ProvableFairness(fairnessAddress).transferOwnership(newOwner);
        }
        
        // Transfer ownership of slot machine contract
        if (slotMachineAddress != address(0)) {
            SlotMachine(slotMachineAddress).transferOwnership(newOwner);
        }
        
        // Transfer ownership of the factory itself
        transferOwnership(newOwner);
    }
}
