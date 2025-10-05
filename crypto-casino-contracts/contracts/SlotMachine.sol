// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "./CasinoVault.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";

contract SlotMachine is Ownable, ReentrancyGuard, Pausable {
    CasinoVault public immutable vault;
    
    uint256 public minBet;
    uint256 public maxBet;
    uint256 public houseEdge;
    
    struct Spin {
        uint256 bet;
        uint256[3] reels;
        uint256 winAmount;
        bool resolved;
    }
    
    mapping(uint256 => Spin) private s_spinIdToSpin;
    mapping(address => uint256) public playerLastSpin;
    uint256 private spinCounter;
    
    uint256 private seed;
    
    event SpinStarted(address indexed player, uint256 bet, uint256 spinId, uint256 timestamp);
    event SpinResult(address indexed player, uint256 spinId, uint256[3] reels, uint256 winAmount, uint256 timestamp);
    event GameConfigUpdated(uint256 minBet, uint256 maxBet, uint256 houseEdge, uint256 timestamp);
    
    constructor(
        address _vaultAddress,
        uint256 _minBet,
        uint256 _maxBet,
        uint256 _houseEdge
    ) Ownable(msg.sender) {
        require(_vaultAddress != address(0), "SlotMachine: invalid vault address");
        require(_minBet > 0, "SlotMachine: minBet must be positive");
        require(_maxBet > _minBet, "SlotMachine: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "SlotMachine: houseEdge cannot exceed 20%");
        
        vault = CasinoVault(_vaultAddress);
        minBet = _minBet;
        maxBet = _maxBet;
        houseEdge = _houseEdge;
        seed = uint256(keccak256(abi.encodePacked(block.timestamp, block.prevrandao, msg.sender)));
        spinCounter = 1;
    }
    
    function getRandomNumber(uint256 max) internal returns (uint256) {
        seed = uint256(keccak256(abi.encodePacked(seed, block.timestamp, block.prevrandao)));
        return seed % max;
    }
    
    function spinForPlayer(address player, uint256 betAmount) 
        external 
        onlyOwner 
        nonReentrant 
        whenNotPaused 
        returns (uint256 spinId) 
    {
        require(betAmount >= minBet, "SlotMachine: bet too small");
        require(betAmount <= maxBet, "SlotMachine: bet too large");
        require(vault.getBalance(player) >= betAmount, "SlotMachine: insufficient balance");
        
        bool success = vault.placeBet(player, betAmount);
        require(success, "SlotMachine: failed to place bet");
        
        spinId = spinCounter;
        spinCounter++;
        
        uint256[3] memory reels;
        reels[0] = getRandomNumber(10);
        reels[1] = getRandomNumber(10);
        reels[2] = getRandomNumber(10);
        
        uint256 winAmount = calculateWinnings(betAmount, reels);
        
        s_spinIdToSpin[spinId] = Spin({
            bet: betAmount,
            reels: reels,
            winAmount: winAmount,
            resolved: true
        });
        
        playerLastSpin[player] = spinId;
        
        if (winAmount > 0) {
            vault.payWinnings(player, winAmount);
        }
        
        emit SpinStarted(player, betAmount, spinId, block.timestamp);
        emit SpinResult(player, spinId, reels, winAmount, block.timestamp);
        
        return spinId;
    }
    
    function calculateWinnings(uint256 _bet, uint256[3] memory _reels) internal view returns (uint256) {
        if (_reels[0] == _reels[1] && _reels[1] == _reels[2]) {
            if (_reels[0] == 7) {
                return adjustHouseEdgePayout((_bet * 1000) / 100);
            } 
            return adjustHouseEdgePayout((_bet * 500) / 100);
        }
        
        if (_reels[0] == _reels[1] || _reels[1] == _reels[2] || _reels[0] == _reels[2]) {
            return adjustHouseEdgePayout((_bet * 200) / 100);
        }
        
        if (_reels[0] == 7 || _reels[1] == 7 || _reels[2] == 7) {
            return adjustHouseEdgePayout((_bet * 110) / 100);
        }
        
        return 0;
    }
    
    function adjustHouseEdgePayout(uint256 _oldPayout) internal view returns (uint256) {
        uint256 adjustment = (_oldPayout * houseEdge) / 10000;
        return _oldPayout - adjustment;
    }
    
    function getSpinState(uint256 spinId) external view returns (Spin memory) {
        return s_spinIdToSpin[spinId];
    }
    
    function getPlayerLastSpin(address player) external view returns (Spin memory) {
        uint256 spinId = playerLastSpin[player];
        require(spinId != 0, "SlotMachine: no spins found for player");
        return s_spinIdToSpin[spinId];
    }
    
    function updateGameConfig(uint256 _minBet, uint256 _maxBet, uint256 _houseEdge) external onlyOwner {
        require(_minBet > 0, "SlotMachine: minBet must be positive");
        require(_maxBet > _minBet, "SlotMachine: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "SlotMachine: houseEdge cannot exceed 20%");
        
        minBet = _minBet;
        maxBet = _maxBet;
        houseEdge = _houseEdge;
        
        emit GameConfigUpdated(minBet, maxBet, houseEdge, block.timestamp);
    }
    
    function pause() external onlyOwner {
        _pause();
    }
    
    function unpause() external onlyOwner {
        _unpause();
    }
}
