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

    enum BetType { STRAIGHT, RED, BLACK, ODD, EVEN, LOW, HIGH, DOZEN_FIRST, DOZEN_SECOND, DOZEN_THIRD, COLUMN_FIRST, COLUMN_SECOND, COLUMN_THIRD }

    struct Bet {
        BetType betType;
        uint256 amount;
        uint8 number;
    }

    struct RouletteGame {
        address player;
        Bet[] bets;
        uint256 totalBetAmount;
        bytes32 serverSeedHash;
        bytes32 serverSeed;
        bytes32 clientSeed;
        uint8 winningNumber;
        uint256 totalPayout;
        bool settled;
        uint256 createdAt;
        uint256 settledAt;
    }

    mapping(uint256 => RouletteGame) private games;
    mapping(address => uint256) public playerLastGame;
    uint256 private gameCounter;

    uint8[18] private redNumbers = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36];

    event GameCreated(
        uint256 indexed gameId,
        address indexed player,
        bytes32 serverSeedHash,
        uint256 totalBetAmount,
        bytes32 clientSeed,
        uint256 timestamp
    );

    event BetPlaced(
        uint256 indexed gameId,
        BetType betType,
        uint8 number,
        uint256 amount
    );

    event GameSettled(
        uint256 indexed gameId,
        address indexed player,
        uint8 winningNumber,
        uint256 totalPayout,
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
        require(_vaultAddress != address(0), "Roulette: invalid vault address");
        require(_minBet > 0, "Roulette: minBet must be positive");
        require(_maxBet > _minBet, "Roulette: maxBet must be greater than minBet");
        require(_houseEdge <= 2000, "Roulette: houseEdge cannot exceed 20%");

        vault = CasinoVault(_vaultAddress);
        minBet = _minBet;
        maxBet = _maxBet;
        houseEdge = _houseEdge;
        gameCounter = 1;
    }

    function createGame(
        address player,
        bytes32 _serverSeedHash,
        Bet[] calldata _bets,
        bytes32 _clientSeed
    )
        external
        onlyOwner
        nonReentrant
        whenNotPaused
        returns (uint256 gameId)
    {
        require(player != address(0), "Roulette: invalid player address");
        require(_bets.length > 0, "Roulette: must place at least one bet");
        require(_bets.length <= 20, "Roulette: too many bets (max 20)");

        uint256 totalBetAmount = 0;

        for (uint256 i = 0; i < _bets.length; i++) {
            require(_bets[i].amount >= minBet, "Roulette: bet too small");
            require(_bets[i].amount <= maxBet, "Roulette: bet too large");
            validateBet(_bets[i]);
            totalBetAmount += _bets[i].amount;
        }

        require(vault.getBalance(player) >= totalBetAmount, "Roulette: insufficient balance");

        bool success = vault.placeBet(player, totalBetAmount);
        require(success, "Roulette: failed to place bet");

        gameId = gameCounter;
        gameCounter++;

        RouletteGame storage game = games[gameId];
        game.player = player;
        game.totalBetAmount = totalBetAmount;
        game.serverSeedHash = _serverSeedHash;
        game.clientSeed = _clientSeed;
        game.settled = false;
        game.createdAt = block.timestamp;

        for (uint256 i = 0; i < _bets.length; i++) {
            game.bets.push(_bets[i]);
            emit BetPlaced(gameId, _bets[i].betType, _bets[i].number, _bets[i].amount);
        }

        playerLastGame[player] = gameId;

        emit GameCreated(
            gameId,
            player,
            _serverSeedHash,
            totalBetAmount,
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
        returns (uint8 winningNumber, uint256 totalPayout)
    {
        RouletteGame storage game = games[_gameId];

        require(game.player != address(0), "Roulette: game does not exist");
        require(!game.settled, "Roulette: game already settled");
        require(
            keccak256(abi.encodePacked(_serverSeed)) == game.serverSeedHash,
            "Roulette: server seed does not match hash"
        );

        game.serverSeed = _serverSeed;

        winningNumber = calculateWinningNumber(_serverSeed, game.clientSeed, game.player, _gameId);
        game.winningNumber = winningNumber;

        totalPayout = 0;

        for (uint256 i = 0; i < game.bets.length; i++) {
            if (checkBetWin(game.bets[i], winningNumber)) {
                uint256 betPayout = calculateBetPayout(game.bets[i]);
                totalPayout += betPayout;
            }
        }

        game.totalPayout = totalPayout;

        if (totalPayout > 0) {
            vault.payWinnings(game.player, totalPayout);
        }

        game.settled = true;
        game.settledAt = block.timestamp;

        emit SeedRevealed(_gameId, _serverSeed, block.timestamp);
        emit GameSettled(_gameId, game.player, winningNumber, totalPayout, block.timestamp);

        return (winningNumber, totalPayout);
    }

    function validateBet(Bet calldata _bet) internal pure {
        if (_bet.betType == BetType.STRAIGHT) {
            require(_bet.number <= 36, "Roulette: STRAIGHT number must be 0-36");
        }
    }

    function calculateWinningNumber(
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
        return uint8(randomNumber % 37);
    }

    function checkBetWin(Bet memory _bet, uint8 _winningNumber) public view returns (bool) {
        if (_bet.betType == BetType.STRAIGHT) {
            return _winningNumber == _bet.number;
        } else if (_bet.betType == BetType.RED) {
            return isRed(_winningNumber);
        } else if (_bet.betType == BetType.BLACK) {
            return !isRed(_winningNumber) && _winningNumber != 0;
        } else if (_bet.betType == BetType.ODD) {
            return _winningNumber != 0 && _winningNumber % 2 == 1;
        } else if (_bet.betType == BetType.EVEN) {
            return _winningNumber != 0 && _winningNumber % 2 == 0;
        } else if (_bet.betType == BetType.LOW) {
            return _winningNumber >= 1 && _winningNumber <= 18;
        } else if (_bet.betType == BetType.HIGH) {
            return _winningNumber >= 19 && _winningNumber <= 36;
        } else if (_bet.betType == BetType.DOZEN_FIRST) {
            return _winningNumber >= 1 && _winningNumber <= 12;
        } else if (_bet.betType == BetType.DOZEN_SECOND) {
            return _winningNumber >= 13 && _winningNumber <= 24;
        } else if (_bet.betType == BetType.DOZEN_THIRD) {
            return _winningNumber >= 25 && _winningNumber <= 36;
        } else if (_bet.betType == BetType.COLUMN_FIRST) {
            return _winningNumber >= 1 && _winningNumber % 3 == 1;
        } else if (_bet.betType == BetType.COLUMN_SECOND) {
            return _winningNumber >= 2 && _winningNumber % 3 == 2;
        } else if (_bet.betType == BetType.COLUMN_THIRD) {
            return _winningNumber >= 3 && _winningNumber % 3 == 0;
        }

        return false;
    }

    function isRed(uint8 _number) internal view returns (bool) {
        for (uint256 i = 0; i < redNumbers.length; i++) {
            if (redNumbers[i] == _number) {
                return true;
            }
        }
        return false;
    }

    function calculateBetPayout(Bet memory _bet) internal view returns (uint256) {
        uint256 multiplier = getPayoutMultiplier(_bet.betType);
        uint256 grossPayout = _bet.amount * multiplier;
        return adjustHouseEdgePayout(grossPayout);
    }

    function getPayoutMultiplier(BetType _betType) public pure returns (uint256) {
        if (_betType == BetType.STRAIGHT) {
            return 36;
        } else if (
            _betType == BetType.RED ||
            _betType == BetType.BLACK ||
            _betType == BetType.ODD ||
            _betType == BetType.EVEN ||
            _betType == BetType.LOW ||
            _betType == BetType.HIGH
        ) {
            return 2;
        } else if (
            _betType == BetType.DOZEN_FIRST ||
            _betType == BetType.DOZEN_SECOND ||
            _betType == BetType.DOZEN_THIRD ||
            _betType == BetType.COLUMN_FIRST ||
            _betType == BetType.COLUMN_SECOND ||
            _betType == BetType.COLUMN_THIRD
        ) {
            return 3;
        }

        return 0;
    }

    function adjustHouseEdgePayout(uint256 _grossPayout) internal view returns (uint256) {
        uint256 adjustment = (_grossPayout * houseEdge) / 10000;
        return _grossPayout - adjustment;
    }

    function verifyFairness(
        uint256 _gameId,
        bytes32 _serverSeed
    ) external view returns (bool isValid, uint8 winningNumber) {
        RouletteGame storage game = games[_gameId];

        require(game.player != address(0), "Roulette: game does not exist");
        require(game.settled, "Roulette: game not settled yet");

        bool hashValid = keccak256(abi.encodePacked(_serverSeed)) == game.serverSeedHash;

        if (hashValid) {
            winningNumber = calculateWinningNumber(_serverSeed, game.clientSeed, game.player, _gameId);
            isValid = (winningNumber == game.winningNumber);
        } else {
            isValid = false;
            winningNumber = 0;
        }

        return (isValid, winningNumber);
    }

    function getGame(uint256 _gameId) external view returns (
        address player,
        uint256 totalBetAmount,
        bytes32 serverSeedHash,
        bytes32 serverSeed,
        bytes32 clientSeed,
        uint8 winningNumber,
        uint256 totalPayout,
        bool settled,
        uint256 createdAt,
        uint256 settledAt
    ) {
        RouletteGame storage game = games[_gameId];
        require(game.player != address(0), "Roulette: game does not exist");

        return (
            game.player,
            game.totalBetAmount,
            game.serverSeedHash,
            game.serverSeed,
            game.clientSeed,
            game.winningNumber,
            game.totalPayout,
            game.settled,
            game.createdAt,
            game.settledAt
        );
    }

    function getGameBets(uint256 _gameId) external view returns (Bet[] memory) {
        RouletteGame storage game = games[_gameId];
        require(game.player != address(0), "Roulette: game does not exist");
        return game.bets;
    }

    function getPlayerLastGame(address player) external view returns (uint256) {
        uint256 gameId = playerLastGame[player];
        require(gameId != 0, "Roulette: no games found for player");
        return gameId;
    }

    function updateGameConfig(
        uint256 _minBet,
        uint256 _maxBet,
        uint256 _houseEdge
    ) external onlyOwner {
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
