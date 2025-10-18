// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "./CasinoVault.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";

contract Dice is Ownable, ReentrancyGuard, Pausable {
    CasinoVault public immutable vault;

    uint256 public minBet;
    uint256 public maxBet;
    uint256 public houseEdge;

    enum BetType { ROLL_UNDER, ROLL_OVER, EXACT }

    struct DiceGame {
        address player;
        uint256 betAmount;
        uint8 prediction;
        BetType betType;
        bytes32 serverSeedHash;
        bytes32 serverSeed;
        bytes32 clientSeed;
        uint8 result;
        uint256 payout;
        bool settled;
        uint256 createdAt;
        uint256 settledAt;
    }

    mapping(uint256 => DiceGame) private games;
    mapping(address => uint256) public playerLastGame;
    uint256 private gameCounter;

    event GameCreated(
        uint256 indexed gameId,
        address indexed player,
        bytes32 serverSeedHash,
        uint256 betAmount,
        uint8 prediction,
        BetType betType,
        bytes32 clientSeed,
        uint256 timestamp
    );

    event GameSettled(
        uint256 indexed gameId,
        address indexed player,
        uint8 result,
        uint256 payout,
        bool won,
        uint256 timestamp
    );

    event SeedRevealed(
        uint256 indexed gameId,
        bytes32 serverSeed,
        uint256 timestamp
    );

    event GameConfigUpdated(
        uint256 minBet,
        uint256 maxBet,
        uint256 houseEdge,
        uint256 timestamp
    );

    constructor(
        address _vaultAddress,
        uint256 _minBet,
        uint256 _maxBet,
        uint256 _houseEdge
    ) Ownable(msg.sender) {
        require(_vaultAddress != address(0), "Dice: invalid vault address");
        require(_minBet > 0, "Dice: minBet must be positive");
        require(_maxBet > _minBet, "Dice: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "Dice: houseEdge cannot exceed 20%");

        vault = CasinoVault(_vaultAddress);
        minBet = _minBet;
        maxBet = _maxBet;
        houseEdge = _houseEdge;
        gameCounter = 1;
    }

    function createGame(
        address player,
        bytes32 _serverSeedHash,
        uint256 _betAmount,
        uint8 _prediction,
        BetType _betType,
        bytes32 _clientSeed
    )
        external
        onlyOwner
        nonReentrant
        whenNotPaused
        returns (uint256 gameId)
    {
        require(player != address(0), "Dice: invalid player address");
        require(_betAmount >= minBet, "Dice: bet too small");
        require(_betAmount <= maxBet, "Dice: bet too large");
        require(_prediction >= 1 && _prediction <= 100, "Dice: prediction must be 1-100");

        if (_betType == BetType.ROLL_UNDER) {
            require(_prediction >= 2 && _prediction <= 99, "Dice: ROLL_UNDER prediction must be 2-99");
        } else if (_betType == BetType.ROLL_OVER) {
            require(_prediction >= 2 && _prediction <= 99, "Dice: ROLL_OVER prediction must be 2-99");
        }

        require(vault.getBalance(player) >= _betAmount, "Dice: insufficient balance");

        bool success = vault.placeBet(player, _betAmount);
        require(success, "Dice: failed to place bet");

        gameId = gameCounter;
        gameCounter++;

        games[gameId] = DiceGame({
            player: player,
            betAmount: _betAmount,
            prediction: _prediction,
            betType: _betType,
            serverSeedHash: _serverSeedHash,
            serverSeed: bytes32(0),
            clientSeed: _clientSeed,
            result: 0,
            payout: 0,
            settled: false,
            createdAt: block.timestamp,
            settledAt: 0
        });

        playerLastGame[player] = gameId;

        emit GameCreated(
            gameId,
            player,
            _serverSeedHash,
            _betAmount,
            _prediction,
            _betType,
            _clientSeed,
            block.timestamp
        );

        return gameId;
    }

    function settleGame(
        uint256 _gameId,
        bytes32 _serverSeed
    )
        external
        onlyOwner
        nonReentrant
        whenNotPaused
        returns (uint8 result, uint256 payout)
    {
        DiceGame storage game = games[_gameId];

        require(game.player != address(0), "Dice: game does not exist");
        require(!game.settled, "Dice: game already settled");
        require(
            keccak256(abi.encodePacked(_serverSeed)) == game.serverSeedHash,
            "Dice: server seed does not match hash"
        );

        game.serverSeed = _serverSeed;

        result = calculateResult(_serverSeed, game.clientSeed, game.player, _gameId);
        game.result = result;

        bool won = checkWin(result, game.prediction, game.betType);

        if (won) {
            payout = calculatePayout(game.betAmount, game.prediction, game.betType);
            game.payout = payout;
            vault.payWinnings(game.player, payout);
        }

        game.settled = true;
        game.settledAt = block.timestamp;

        emit SeedRevealed(_gameId, _serverSeed, block.timestamp);
        emit GameSettled(_gameId, game.player, result, payout, won, block.timestamp);

        return (result, payout);
    }

    function calculateResult(
        bytes32 _serverSeed,
        bytes32 _clientSeed,
        address _player,
        uint256 _gameId
    ) public pure returns (uint8) {
        bytes32 combinedHash = keccak256(abi.encodePacked(
            _serverSeed,
            _clientSeed,
            _player,
            _gameId
        ));

        uint256 randomNumber = uint256(combinedHash);
        return uint8((randomNumber % 100) + 1);
    }

    function checkWin(
        uint8 _result,
        uint8 _prediction,
        BetType _betType
    ) public pure returns (bool) {
        if (_betType == BetType.ROLL_UNDER) {
            return _result < _prediction;
        } else if (_betType == BetType.ROLL_OVER) {
            return _result > _prediction;
        } else {
            return _result == _prediction;
        }
    }

    function calculatePayout(
        uint256 _betAmount,
        uint8 _prediction,
        BetType _betType
    ) public view returns (uint256) {
        uint256 winProbability;

        if (_betType == BetType.ROLL_UNDER) {
            winProbability = uint256(_prediction - 1);
        } else if (_betType == BetType.ROLL_OVER) {
            winProbability = uint256(100 - _prediction);
        } else {
            winProbability = 1;
        }

        require(winProbability > 0, "Dice: invalid win probability");

        uint256 grossPayout = (_betAmount * 100) / winProbability;

        return adjustHouseEdgePayout(grossPayout);
    }

    function adjustHouseEdgePayout(uint256 _grossPayout) internal view returns (uint256) {
        uint256 adjustment = (_grossPayout * houseEdge) / 10000;
        return _grossPayout - adjustment;
    }

    function verifyFairness(
        uint256 _gameId,
        bytes32 _serverSeed
    ) external view returns (bool isValid, uint8 result) {
        DiceGame storage game = games[_gameId];

        require(game.player != address(0), "Dice: game does not exist");
        require(game.settled, "Dice: game not settled yet");

        bool hashValid = keccak256(abi.encodePacked(_serverSeed)) == game.serverSeedHash;

        if (hashValid) {
            result = calculateResult(_serverSeed, game.clientSeed, game.player, _gameId);
            isValid = (result == game.result);
        } else {
            isValid = false;
            result = 0;
        }

        return (isValid, result);
    }

    function getGame(uint256 _gameId) external view returns (DiceGame memory) {
        require(games[_gameId].player != address(0), "Dice: game does not exist");
        return games[_gameId];
    }

    function getPlayerLastGame(address player) external view returns (DiceGame memory) {
        uint256 gameId = playerLastGame[player];
        require(gameId != 0, "Dice: no games found for player");
        return games[gameId];
    }

    function updateGameConfig(
        uint256 _minBet,
        uint256 _maxBet,
        uint256 _houseEdge
    ) external onlyOwner {
        require(_minBet > 0, "Dice: minBet must be positive");
        require(_maxBet > _minBet, "Dice: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "Dice: houseEdge cannot exceed 20%");

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
