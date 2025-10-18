// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "forge-std/Test.sol";
import "./Dice.sol";
import "./CasinoVault.sol";
import "./CasinoToken.sol";

contract DiceTest is Test {
    Dice public dice;
    CasinoVault public vault;
    CasinoToken public token;
    address public owner;
    address public player1;
    address public player2;

    uint256 constant MIN_BET = 10 * 10**18;
    uint256 constant MAX_BET = 1000 * 10**18;
    uint256 constant HOUSE_EDGE = 100;
    uint256 constant INITIAL_TOKENS = 100000 * 10**18;

    event GameCreated(
        uint256 indexed gameId,
        address indexed player,
        bytes32 serverSeedHash,
        uint256 betAmount,
        uint8 prediction,
        Dice.BetType betType,
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

    function setUp() public {
        owner = address(this);
        player1 = makeAddr("player1");
        player2 = makeAddr("player2");

        token = new CasinoToken();
        vault = new CasinoVault(address(token));
        dice = new Dice(address(vault), MIN_BET, MAX_BET, HOUSE_EDGE);

        vault.authorizeGame(address(dice));

        token.transfer(player1, INITIAL_TOKENS);
        token.transfer(player2, INITIAL_TOKENS);

        vm.startPrank(player1);
        token.approve(address(vault), INITIAL_TOKENS);
        vault.deposit(INITIAL_TOKENS);
        vm.stopPrank();

        vm.startPrank(player2);
        token.approve(address(vault), INITIAL_TOKENS);
        vault.deposit(INITIAL_TOKENS);
        vm.stopPrank();
    }

    function test_InitialState() public view {
        assertEq(address(dice.vault()), address(vault));
        assertEq(dice.minBet(), MIN_BET);
        assertEq(dice.maxBet(), MAX_BET);
        assertEq(dice.houseEdge(), HOUSE_EDGE);
        assertEq(dice.owner(), owner);
        assertFalse(dice.paused());
    }

    function test_RevertConstructorInvalidVault() public {
        vm.expectRevert("Dice: invalid vault address");
        new Dice(address(0), MIN_BET, MAX_BET, HOUSE_EDGE);
    }

    function test_RevertConstructorInvalidMinBet() public {
        vm.expectRevert("Dice: minBet must be positive");
        new Dice(address(vault), 0, MAX_BET, HOUSE_EDGE);
    }

    function test_RevertConstructorInvalidMaxBet() public {
        vm.expectRevert("Dice: maxBet must be greater than minBet");
        new Dice(address(vault), MAX_BET, MIN_BET, HOUSE_EDGE);
    }

    function test_RevertConstructorInvalidHouseEdge() public {
        vm.expectRevert("Dice: houseEdge cannot exceed 20%");
        new Dice(address(vault), MIN_BET, MAX_BET, 2001);
    }

    function test_CreateGame() public {
        bytes32 serverSeed = keccak256(abi.encodePacked("server_seed_123"));
        bytes32 serverSeedHash = keccak256(abi.encodePacked(serverSeed));
        bytes32 clientSeed = keccak256(abi.encodePacked("client_seed_456"));
        uint256 betAmount = 100 * 10**18;
        uint8 prediction = 50;

        vm.expectEmit(true, true, false, true);
        emit GameCreated(
            1,
            player1,
            serverSeedHash,
            betAmount,
            prediction,
            Dice.BetType.ROLL_UNDER,
            clientSeed,
            block.timestamp
        );

        uint256 gameId = dice.createGame(
            player1,
            serverSeedHash,
            betAmount,
            prediction,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        assertEq(gameId, 1);
        assertEq(dice.playerLastGame(player1), gameId);

        Dice.DiceGame memory game = dice.getGame(gameId);

        assertEq(game.player, player1);
        assertEq(game.betAmount, betAmount);
        assertEq(game.prediction, prediction);
        assertTrue(game.betType == Dice.BetType.ROLL_UNDER);
        assertEq(game.serverSeedHash, serverSeedHash);
        assertEq(game.clientSeed, clientSeed);
        assertFalse(game.settled);
    }

    function test_RevertCreateGameBetTooSmall() public {
        bytes32 serverSeedHash = keccak256(abi.encodePacked("hash"));
        bytes32 clientSeed = keccak256(abi.encodePacked("client"));

        vm.expectRevert("Dice: bet too small");
        dice.createGame(
            player1,
            serverSeedHash,
            5 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );
    }

    function test_RevertCreateGameBetTooLarge() public {
        bytes32 serverSeedHash = keccak256(abi.encodePacked("hash"));
        bytes32 clientSeed = keccak256(abi.encodePacked("client"));

        vm.expectRevert("Dice: bet too large");
        dice.createGame(
            player1,
            serverSeedHash,
            2000 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );
    }

    function test_RevertCreateGameInvalidPrediction() public {
        bytes32 serverSeedHash = keccak256(abi.encodePacked("hash"));
        bytes32 clientSeed = keccak256(abi.encodePacked("client"));

        vm.expectRevert("Dice: prediction must be 1-100");
        dice.createGame(
            player1,
            serverSeedHash,
            100 * 10**18,
            0,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        vm.expectRevert("Dice: prediction must be 1-100");
        dice.createGame(
            player1,
            serverSeedHash,
            100 * 10**18,
            101,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );
    }

    function test_SettleGame() public {
        bytes32 serverSeed = keccak256(abi.encodePacked("server_seed"));
        bytes32 serverSeedHash = keccak256(abi.encodePacked(serverSeed));
        bytes32 clientSeed = keccak256(abi.encodePacked("client_seed"));
        uint256 betAmount = 100 * 10**18;
        uint8 prediction = 50;

        uint256 gameId = dice.createGame(
            player1,
            serverSeedHash,
            betAmount,
            prediction,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        uint8 expectedResult = dice.calculateResult(serverSeed, clientSeed, player1, gameId);

        (uint8 result, uint256 payout) = dice.settleGame(gameId, serverSeed);

        assertEq(result, expectedResult);

        Dice.DiceGame memory game = dice.getGame(gameId);

        assertEq(game.serverSeed, serverSeed);
        assertEq(game.result, result);
        assertEq(game.payout, payout);
        assertTrue(game.settled);
    }

    function test_RevertSettleGameWrongServerSeed() public {
        bytes32 correctServerSeed = keccak256(abi.encodePacked("correct_seed"));
        bytes32 serverSeedHash = keccak256(abi.encodePacked(correctServerSeed));
        bytes32 clientSeed = keccak256(abi.encodePacked("client_seed"));

        uint256 gameId = dice.createGame(
            player1,
            serverSeedHash,
            100 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        bytes32 wrongServerSeed = keccak256(abi.encodePacked("wrong_seed"));

        vm.expectRevert("Dice: server seed does not match hash");
        dice.settleGame(gameId, wrongServerSeed);
    }

    function test_RevertSettleGameAlreadySettled() public {
        bytes32 serverSeed = keccak256(abi.encodePacked("server_seed"));
        bytes32 serverSeedHash = keccak256(abi.encodePacked(serverSeed));
        bytes32 clientSeed = keccak256(abi.encodePacked("client_seed"));

        uint256 gameId = dice.createGame(
            player1,
            serverSeedHash,
            100 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        dice.settleGame(gameId, serverSeed);

        vm.expectRevert("Dice: game already settled");
        dice.settleGame(gameId, serverSeed);
    }

    function test_CheckWinRollUnder() public view {
        assertTrue(dice.checkWin(30, 50, Dice.BetType.ROLL_UNDER));
        assertTrue(dice.checkWin(49, 50, Dice.BetType.ROLL_UNDER));
        assertFalse(dice.checkWin(50, 50, Dice.BetType.ROLL_UNDER));
        assertFalse(dice.checkWin(70, 50, Dice.BetType.ROLL_UNDER));
    }

    function test_CheckWinRollOver() public view {
        assertTrue(dice.checkWin(70, 50, Dice.BetType.ROLL_OVER));
        assertTrue(dice.checkWin(51, 50, Dice.BetType.ROLL_OVER));
        assertFalse(dice.checkWin(50, 50, Dice.BetType.ROLL_OVER));
        assertFalse(dice.checkWin(30, 50, Dice.BetType.ROLL_OVER));
    }

    function test_CheckWinExact() public view {
        assertTrue(dice.checkWin(42, 42, Dice.BetType.EXACT));
        assertFalse(dice.checkWin(41, 42, Dice.BetType.EXACT));
        assertFalse(dice.checkWin(43, 42, Dice.BetType.EXACT));
    }

    function test_VerifyFairness() public {
        bytes32 serverSeed = keccak256(abi.encodePacked("verify_seed"));
        bytes32 serverSeedHash = keccak256(abi.encodePacked(serverSeed));
        bytes32 clientSeed = keccak256(abi.encodePacked("client_seed"));

        uint256 gameId = dice.createGame(
            player1,
            serverSeedHash,
            100 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        dice.settleGame(gameId, serverSeed);

        (bool isValid, uint8 result) = dice.verifyFairness(gameId, serverSeed);

        assertTrue(isValid);
        assertGt(result, 0);
        assertLe(result, 100);

        Dice.DiceGame memory game = dice.getGame(gameId);
        assertEq(result, game.result);
    }

    function test_UpdateGameConfig() public {
        uint256 newMinBet = 20 * 10**18;
        uint256 newMaxBet = 2000 * 10**18;
        uint256 newHouseEdge = 200;

        vm.expectEmit(false, false, false, true);
        emit GameConfigUpdated(newMinBet, newMaxBet, newHouseEdge, block.timestamp);

        dice.updateGameConfig(newMinBet, newMaxBet, newHouseEdge);

        assertEq(dice.minBet(), newMinBet);
        assertEq(dice.maxBet(), newMaxBet);
        assertEq(dice.houseEdge(), newHouseEdge);
    }

    function test_RevertUpdateGameConfigNotOwner() public {
        vm.prank(player1);
        vm.expectRevert();
        dice.updateGameConfig(20 * 10**18, 2000 * 10**18, 200);
    }

    function test_Pause() public {
        dice.pause();
        assertTrue(dice.paused());
    }

    function test_Unpause() public {
        dice.pause();
        dice.unpause();
        assertFalse(dice.paused());
    }

    function test_RevertCreateGameWhenPaused() public {
        dice.pause();

        bytes32 serverSeedHash = keccak256(abi.encodePacked("hash"));
        bytes32 clientSeed = keccak256(abi.encodePacked("client"));

        vm.expectRevert();
        dice.createGame(
            player1,
            serverSeedHash,
            100 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );
    }

    function test_RevertSettleGameWhenPaused() public {
        bytes32 serverSeed = keccak256(abi.encodePacked("server_seed"));
        bytes32 serverSeedHash = keccak256(abi.encodePacked(serverSeed));
        bytes32 clientSeed = keccak256(abi.encodePacked("client_seed"));

        uint256 gameId = dice.createGame(
            player1,
            serverSeedHash,
            100 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        dice.pause();

        vm.expectRevert();
        dice.settleGame(gameId, serverSeed);
    }

    function test_MultipleGames() public {
        bytes32 serverSeed1 = keccak256(abi.encodePacked("seed1"));
        bytes32 serverSeedHash1 = keccak256(abi.encodePacked(serverSeed1));
        bytes32 clientSeed1 = keccak256(abi.encodePacked("client1"));

        bytes32 serverSeed2 = keccak256(abi.encodePacked("seed2"));
        bytes32 serverSeedHash2 = keccak256(abi.encodePacked(serverSeed2));
        bytes32 clientSeed2 = keccak256(abi.encodePacked("client2"));

        uint256 gameId1 = dice.createGame(
            player1,
            serverSeedHash1,
            100 * 10**18,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed1
        );

        uint256 gameId2 = dice.createGame(
            player2,
            serverSeedHash2,
            200 * 10**18,
            75,
            Dice.BetType.ROLL_OVER,
            clientSeed2
        );

        assertEq(gameId1, 1);
        assertEq(gameId2, 2);
        assertEq(dice.playerLastGame(player1), gameId1);
        assertEq(dice.playerLastGame(player2), gameId2);

        dice.settleGame(gameId1, serverSeed1);
        dice.settleGame(gameId2, serverSeed2);

        Dice.DiceGame memory game1 = dice.getGame(gameId1);
        Dice.DiceGame memory game2 = dice.getGame(gameId2);

        assertTrue(game1.settled);
        assertTrue(game2.settled);
    }

    function test_CalculateResultDeterministic() public view {
        bytes32 serverSeed = keccak256(abi.encodePacked("test_seed"));
        bytes32 clientSeed = keccak256(abi.encodePacked("test_client"));

        uint8 result1 = dice.calculateResult(serverSeed, clientSeed, player1, 1);
        uint8 result2 = dice.calculateResult(serverSeed, clientSeed, player1, 1);

        assertEq(result1, result2);
        assertGt(result1, 0);
        assertLe(result1, 100);
    }

    function testFuzz_CreateGame(uint96 betAmount) public {
        vm.assume(betAmount >= MIN_BET && betAmount <= MAX_BET);
        vm.assume(betAmount <= INITIAL_TOKENS);

        bytes32 serverSeedHash = keccak256(abi.encodePacked("hash"));
        bytes32 clientSeed = keccak256(abi.encodePacked("client"));

        uint256 gameId = dice.createGame(
            player1,
            serverSeedHash,
            betAmount,
            50,
            Dice.BetType.ROLL_UNDER,
            clientSeed
        );

        assertGt(gameId, 0);
    }

    function testFuzz_CalculateResult(bytes32 serverSeed, bytes32 clientSeed) public view {
        uint8 result = dice.calculateResult(serverSeed, clientSeed, player1, 1);

        assertGt(result, 0);
        assertLe(result, 100);
    }
}
