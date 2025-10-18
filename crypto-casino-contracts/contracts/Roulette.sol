// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "./CasinoVault.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";

contract Roulette is Ownable, ReentrancyGuard, Pausable {
    CasinoVault public immutable vault;

    uint256 public minBet;
    uint256 public maxBet;
    uint256 public houseEdge;

    enum BetType {
        Single,
        Red,
        Black,
        Even,
        Odd,
        Low,
        High,
        Dozen1,
        Dozen2,
        Dozen3,
        Column1,
        Column2,
        Column3
    }

    struct Bet {
        uint256 amount;
        BetType betType;
        uint256 number;
        uint256 result;
        uint256 winAmount;
        bool resolved;
    }

    mapping(uint256 => Bet) private s_betIdToBet;
    mapping(address => uint256) public playerLastBet;
    uint256 private betCounter;

    uint256 private seed;

    uint256[18] private redNumbers = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36];

    event BetPlaced(
        address indexed player,
        uint256 amount,
        BetType betType,
        uint256 number,
        uint256 betId,
        uint256 timestamp
    );
    event SpinResult(
        address indexed player,
        uint256 betId,
        uint256 result,
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
        require(_vaultAddress != address(0), "Roulette: invalid vault address");
        require(_minBet > 0, "Roulette: minBet must be positive");
        require(_maxBet > _minBet, "Roulette: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "Roulette: houseEdge cannot exceed 20%");

        vault = CasinoVault(_vaultAddress);
        minBet = _minBet;
        maxBet = _maxBet;
        houseEdge = _houseEdge;
        seed = uint256(keccak256(abi.encodePacked(block.timestamp, block.prevrandao, msg.sender)));
        betCounter = 1;
    }

    function getRandomNumber(uint256 max) internal returns (uint256) {
        seed = uint256(keccak256(abi.encodePacked(seed, block.timestamp, block.prevrandao)));
        return seed % max;
    }

    function spinForPlayer(
        address player,
        uint256 betAmount,
        BetType betType,
        uint256 number
    ) external onlyOwner nonReentrant whenNotPaused returns (uint256 betId) {
        require(betAmount >= minBet, "Roulette: bet too small");
        require(betAmount <= maxBet, "Roulette: bet too large");
        require(vault.getBalance(player) >= betAmount, "Roulette: insufficient balance");

        if (betType == BetType.Single) {
            require(number <= 36, "Roulette: invalid number for single bet");
        }

        bool success = vault.placeBet(player, betAmount);
        require(success, "Roulette: failed to place bet");

        betId = betCounter;
        betCounter++;

        uint256 result = getRandomNumber(37);

        uint256 winAmount = calculateWinnings(betAmount, betType, number, result);

        s_betIdToBet[betId] = Bet({
            amount: betAmount,
            betType: betType,
            number: number,
            result: result,
            winAmount: winAmount,
            resolved: true
        });

        playerLastBet[player] = betId;

        if (winAmount > 0) {
            vault.payWinnings(player, winAmount);
        }

        emit BetPlaced(player, betAmount, betType, number, betId, block.timestamp);
        emit SpinResult(player, betId, result, winAmount, block.timestamp);

        return betId;
    }

    function calculateWinnings(uint256 _bet, BetType _betType, uint256 _number, uint256 _result)
        internal
        view
        returns (uint256)
    {
        bool won = false;
        uint256 multiplier = 100;

        if (_betType == BetType.Single) {
            if (_number == _result) {
                won = true;
                multiplier = 3600;
            }
        } else if (_betType == BetType.Red) {
            if (isRed(_result)) {
                won = true;
                multiplier = 200;
            }
        } else if (_betType == BetType.Black) {
            if (!isRed(_result) && _result != 0) {
                won = true;
                multiplier = 200;
            }
        } else if (_betType == BetType.Even) {
            if (_result != 0 && _result % 2 == 0) {
                won = true;
                multiplier = 200;
            }
        } else if (_betType == BetType.Odd) {
            if (_result % 2 == 1) {
                won = true;
                multiplier = 200;
            }
        } else if (_betType == BetType.Low) {
            if (_result >= 1 && _result <= 18) {
                won = true;
                multiplier = 200;
            }
        } else if (_betType == BetType.High) {
            if (_result >= 19 && _result <= 36) {
                won = true;
                multiplier = 200;
            }
        } else if (_betType == BetType.Dozen1) {
            if (_result >= 1 && _result <= 12) {
                won = true;
                multiplier = 300;
            }
        } else if (_betType == BetType.Dozen2) {
            if (_result >= 13 && _result <= 24) {
                won = true;
                multiplier = 300;
            }
        } else if (_betType == BetType.Dozen3) {
            if (_result >= 25 && _result <= 36) {
                won = true;
                multiplier = 300;
            }
        } else if (_betType == BetType.Column1) {
            if (_result != 0 && (_result - 1) % 3 == 0) {
                won = true;
                multiplier = 300;
            }
        } else if (_betType == BetType.Column2) {
            if (_result != 0 && (_result - 2) % 3 == 0) {
                won = true;
                multiplier = 300;
            }
        } else if (_betType == BetType.Column3) {
            if (_result != 0 && _result % 3 == 0) {
                won = true;
                multiplier = 300;
            }
        }

        if (!won) {
            return 0;
        }

        uint256 grossPayout = (_bet * multiplier) / 100;
        return adjustHouseEdgePayout(grossPayout);
    }

    function isRed(uint256 number) internal view returns (bool) {
        for (uint256 i = 0; i < redNumbers.length; i++) {
            if (redNumbers[i] == number) {
                return true;
            }
        }
        return false;
    }

    function adjustHouseEdgePayout(uint256 _oldPayout) internal view returns (uint256) {
        uint256 adjustment = (_oldPayout * houseEdge) / 10000;
        return _oldPayout - adjustment;
    }

    function getBetState(uint256 betId) external view returns (Bet memory) {
        return s_betIdToBet[betId];
    }

    function getPlayerLastBet(address player) external view returns (Bet memory) {
        uint256 betId = playerLastBet[player];
        require(betId != 0, "Roulette: no bets found for player");
        return s_betIdToBet[betId];
    }

    function updateGameConfig(uint256 _minBet, uint256 _maxBet, uint256 _houseEdge) external onlyOwner {
        require(_minBet > 0, "Roulette: minBet must be positive");
        require(_maxBet > _minBet, "Roulette: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "Roulette: houseEdge cannot exceed 20%");

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
