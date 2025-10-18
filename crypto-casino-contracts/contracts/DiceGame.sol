// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "./CasinoVault.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";

contract DiceGame is Ownable, ReentrancyGuard, Pausable {
    CasinoVault public immutable vault;

    uint256 public minBet;
    uint256 public maxBet;
    uint256 public houseEdge;

    struct Roll {
        uint256 bet;
        uint256 prediction;
        uint256 dice1;
        uint256 dice2;
        uint256 sum;
        uint256 winAmount;
        bool resolved;
    }

    mapping(uint256 => Roll) private s_rollIdToRoll;
    mapping(address => uint256) public playerLastRoll;
    uint256 private rollCounter;

    uint256 private seed;

    event RollStarted(
        address indexed player,
        uint256 bet,
        uint256 prediction,
        uint256 rollId,
        uint256 timestamp
    );
    event RollResult(
        address indexed player,
        uint256 rollId,
        uint256 dice1,
        uint256 dice2,
        uint256 sum,
        uint256 winAmount,
        uint256 timestamp
    );
    event GameConfigUpdated(uint256 minBet, uint256 maxBet, uint256 houseEdge, uint256 timestamp);

    constructor(
        address _vaultAddress,
        uint256 _minBet,
        uint256 _maxBet,
        uint256 _houseEdge
    ) Ownable(msg.sender) {
        require(_vaultAddress != address(0), "DiceGame: invalid vault address");
        require(_minBet > 0, "DiceGame: minBet must be positive");
        require(_maxBet > _minBet, "DiceGame: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "DiceGame: houseEdge cannot exceed 20%");

        vault = CasinoVault(_vaultAddress);
        minBet = _minBet;
        maxBet = _maxBet;
        houseEdge = _houseEdge;
        seed = uint256(keccak256(abi.encodePacked(block.timestamp, block.prevrandao, msg.sender)));
        rollCounter = 1;
    }

    function getRandomNumber(uint256 max) internal returns (uint256) {
        seed = uint256(keccak256(abi.encodePacked(seed, block.timestamp, block.prevrandao)));
        return seed % max;
    }

    function rollForPlayer(address player, uint256 betAmount, uint256 prediction)
        external
        onlyOwner
        nonReentrant
        whenNotPaused
        returns (uint256 rollId)
    {
        require(betAmount >= minBet, "DiceGame: bet too small");
        require(betAmount <= maxBet, "DiceGame: bet too large");
        require(prediction >= 2 && prediction <= 12, "DiceGame: invalid prediction (must be 2-12)");
        require(vault.getBalance(player) >= betAmount, "DiceGame: insufficient balance");

        bool success = vault.placeBet(player, betAmount);
        require(success, "DiceGame: failed to place bet");

        rollId = rollCounter;
        rollCounter++;

        uint256 dice1 = getRandomNumber(6) + 1;
        uint256 dice2 = getRandomNumber(6) + 1;
        uint256 sum = dice1 + dice2;

        uint256 winAmount = calculateWinnings(betAmount, prediction, sum);

        s_rollIdToRoll[rollId] = Roll({
            bet: betAmount,
            prediction: prediction,
            dice1: dice1,
            dice2: dice2,
            sum: sum,
            winAmount: winAmount,
            resolved: true
        });

        playerLastRoll[player] = rollId;

        if (winAmount > 0) {
            vault.payWinnings(player, winAmount);
        }

        emit RollStarted(player, betAmount, prediction, rollId, block.timestamp);
        emit RollResult(player, rollId, dice1, dice2, sum, winAmount, block.timestamp);

        return rollId;
    }

    function calculateWinnings(uint256 _bet, uint256 _prediction, uint256 _sum)
        internal
        view
        returns (uint256)
    {
        if (_prediction != _sum) {
            return 0;
        }

        uint256 multiplier;

        if (_sum == 2 || _sum == 12) {
            multiplier = 3600;
        } else if (_sum == 3 || _sum == 11) {
            multiplier = 1800;
        } else if (_sum == 4 || _sum == 10) {
            multiplier = 1200;
        } else if (_sum == 5 || _sum == 9) {
            multiplier = 900;
        } else if (_sum == 6 || _sum == 8) {
            multiplier = 720;
        } else {
            multiplier = 600;
        }

        uint256 grossPayout = (_bet * multiplier) / 100;
        return adjustHouseEdgePayout(grossPayout);
    }

    function adjustHouseEdgePayout(uint256 _oldPayout) internal view returns (uint256) {
        uint256 adjustment = (_oldPayout * houseEdge) / 10000;
        return _oldPayout - adjustment;
    }

    function getRollState(uint256 rollId) external view returns (Roll memory) {
        return s_rollIdToRoll[rollId];
    }

    function getPlayerLastRoll(address player) external view returns (Roll memory) {
        uint256 rollId = playerLastRoll[player];
        require(rollId != 0, "DiceGame: no rolls found for player");
        return s_rollIdToRoll[rollId];
    }

    function updateGameConfig(uint256 _minBet, uint256 _maxBet, uint256 _houseEdge) external onlyOwner {
        require(_minBet > 0, "DiceGame: minBet must be positive");
        require(_maxBet > _minBet, "DiceGame: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "DiceGame: houseEdge cannot exceed 20%");

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
