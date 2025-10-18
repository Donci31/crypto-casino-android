// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import {Test} from "forge-std/Test.sol";
import {DiceGame} from "./DiceGame.sol";
import {CasinoVault} from "./CasinoVault.sol";
import {CasinoToken} from "./CasinoToken.sol";

contract DiceGameTest is Test {
    DiceGame public diceGame;
    CasinoVault public vault;
    CasinoToken public token;

    address public owner;
    address public player;

    uint256 public constant MIN_BET = 10 * 10 ** 18;
    uint256 public constant MAX_BET = 1000 * 10 ** 18;
    uint256 public constant HOUSE_EDGE = 100;
    uint256 public constant DEPOSIT_AMOUNT = 10000 * 10 ** 18;

    function setUp() public {
        owner = address(this);
        player = address(0x1);

        token = new CasinoToken();
        vault = new CasinoVault(address(token));
        diceGame = new DiceGame(address(vault), MIN_BET, MAX_BET, HOUSE_EDGE);

        vault.authorizeGame(address(diceGame));

        token.transfer(player, DEPOSIT_AMOUNT);
        vm.startPrank(player);
        token.approve(address(vault), DEPOSIT_AMOUNT);
        vault.deposit(DEPOSIT_AMOUNT);
        vm.stopPrank();
    }

    function testDeployment() public view {
        assertEq(address(diceGame.vault()), address(vault));
        assertEq(diceGame.minBet(), MIN_BET);
        assertEq(diceGame.maxBet(), MAX_BET);
        assertEq(diceGame.houseEdge(), HOUSE_EDGE);
    }

    function testRollForPlayer() public {
        uint256 betAmount = 100 * 10 ** 18;
        uint256 prediction = 7;

        uint256 rollId = diceGame.rollForPlayer(player, betAmount, prediction);

        assertEq(rollId, 1);

        DiceGame.Roll memory roll = diceGame.getRollState(rollId);

        assertEq(roll.bet, betAmount);
        assertEq(roll.prediction, prediction);
        assertGe(roll.dice1, 1);
        assertLe(roll.dice1, 6);
        assertGe(roll.dice2, 1);
        assertLe(roll.dice2, 6);
        assertEq(roll.sum, roll.dice1 + roll.dice2);
        assertTrue(roll.resolved);
    }

    function testRollForPlayerBetTooSmall() public {
        uint256 betAmount = 5 * 10 ** 18;
        uint256 prediction = 7;

        vm.expectRevert("DiceGame: bet too small");
        diceGame.rollForPlayer(player, betAmount, prediction);
    }

    function testRollForPlayerBetTooLarge() public {
        uint256 betAmount = 2000 * 10 ** 18;
        uint256 prediction = 7;

        vm.expectRevert("DiceGame: bet too large");
        diceGame.rollForPlayer(player, betAmount, prediction);
    }

    function testRollForPlayerInvalidPredictionLow() public {
        uint256 betAmount = 100 * 10 ** 18;
        uint256 prediction = 1;

        vm.expectRevert("DiceGame: invalid prediction (must be 2-12)");
        diceGame.rollForPlayer(player, betAmount, prediction);
    }

    function testRollForPlayerInvalidPredictionHigh() public {
        uint256 betAmount = 100 * 10 ** 18;
        uint256 prediction = 13;

        vm.expectRevert("DiceGame: invalid prediction (must be 2-12)");
        diceGame.rollForPlayer(player, betAmount, prediction);
    }

    function testRollForPlayerInsufficientBalance() public {
        uint256 betAmount = 500 * 10 ** 18;
        uint256 prediction = 7;

        address poorPlayer = address(0x2);
        token.transfer(poorPlayer, 100 * 10 ** 18);
        vm.startPrank(poorPlayer);
        token.approve(address(vault), 100 * 10 ** 18);
        vault.deposit(100 * 10 ** 18);
        vm.stopPrank();

        vm.expectRevert("DiceGame: insufficient balance");
        diceGame.rollForPlayer(poorPlayer, betAmount, prediction);
    }

    function testUpdateGameConfig() public {
        uint256 newMinBet = 20 * 10 ** 18;
        uint256 newMaxBet = 2000 * 10 ** 18;
        uint256 newHouseEdge = 200;

        diceGame.updateGameConfig(newMinBet, newMaxBet, newHouseEdge);

        assertEq(diceGame.minBet(), newMinBet);
        assertEq(diceGame.maxBet(), newMaxBet);
        assertEq(diceGame.houseEdge(), newHouseEdge);
    }

    function testPause() public {
        diceGame.pause();

        uint256 betAmount = 100 * 10 ** 18;
        uint256 prediction = 7;

        vm.expectRevert();
        diceGame.rollForPlayer(player, betAmount, prediction);
    }

    function testUnpause() public {
        diceGame.pause();
        diceGame.unpause();

        uint256 betAmount = 100 * 10 ** 18;
        uint256 prediction = 7;

        uint256 rollId = diceGame.rollForPlayer(player, betAmount, prediction);
        assertEq(rollId, 1);
    }

    function testGetPlayerLastRoll() public {
        uint256 betAmount = 100 * 10 ** 18;
        uint256 prediction = 7;

        diceGame.rollForPlayer(player, betAmount, prediction);

        DiceGame.Roll memory lastRoll = diceGame.getPlayerLastRoll(player);

        assertEq(lastRoll.bet, betAmount);
        assertEq(lastRoll.prediction, prediction);
    }
}
