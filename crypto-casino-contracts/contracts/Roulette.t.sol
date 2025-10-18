// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import {Test} from "forge-std/Test.sol";
import {Roulette} from "./Roulette.sol";
import {CasinoVault} from "./CasinoVault.sol";
import {CasinoToken} from "./CasinoToken.sol";

contract RouletteTest is Test {
    Roulette public roulette;
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
        roulette = new Roulette(address(vault), MIN_BET, MAX_BET, HOUSE_EDGE);

        vault.authorizeGame(address(roulette));

        token.transfer(player, DEPOSIT_AMOUNT);
        vm.startPrank(player);
        token.approve(address(vault), DEPOSIT_AMOUNT);
        vault.deposit(DEPOSIT_AMOUNT);
        vm.stopPrank();
    }

    function testDeployment() public view {
        assertEq(address(roulette.vault()), address(vault));
        assertEq(roulette.minBet(), MIN_BET);
        assertEq(roulette.maxBet(), MAX_BET);
        assertEq(roulette.houseEdge(), HOUSE_EDGE);
    }

    function testSpinForPlayerSingleBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Single;
        uint256 number = 17;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);

        assertEq(betId, 1);

        Roulette.Bet memory bet = roulette.getBetState(betId);

        assertEq(bet.amount, betAmount);
        assertEq(uint256(bet.betType), uint256(betType));
        assertEq(bet.number, number);
        assertGe(bet.result, 0);
        assertLe(bet.result, 36);
        assertTrue(bet.resolved);
    }

    function testSpinForPlayerRedBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Red;
        uint256 number = 0;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);
        assertEq(betId, 1);
    }

    function testSpinForPlayerBlackBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Black;
        uint256 number = 0;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);
        assertEq(betId, 1);
    }

    function testSpinForPlayerEvenBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Even;
        uint256 number = 0;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);
        assertEq(betId, 1);
    }

    function testSpinForPlayerOddBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Odd;
        uint256 number = 0;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);
        assertEq(betId, 1);
    }

    function testSpinForPlayerLowBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Low;
        uint256 number = 0;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);
        assertEq(betId, 1);
    }

    function testSpinForPlayerHighBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.High;
        uint256 number = 0;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);
        assertEq(betId, 1);
    }

    function testSpinForPlayerDozenBets() public {
        uint256 betAmount = 100 * 10 ** 18;

        uint256 betId1 = roulette.spinForPlayer(player, betAmount, Roulette.BetType.Dozen1, 0);
        assertEq(betId1, 1);

        uint256 betId2 = roulette.spinForPlayer(player, betAmount, Roulette.BetType.Dozen2, 0);
        assertEq(betId2, 2);

        uint256 betId3 = roulette.spinForPlayer(player, betAmount, Roulette.BetType.Dozen3, 0);
        assertEq(betId3, 3);
    }

    function testSpinForPlayerColumnBets() public {
        uint256 betAmount = 100 * 10 ** 18;

        uint256 betId1 = roulette.spinForPlayer(player, betAmount, Roulette.BetType.Column1, 0);
        assertEq(betId1, 1);

        uint256 betId2 = roulette.spinForPlayer(player, betAmount, Roulette.BetType.Column2, 0);
        assertEq(betId2, 2);

        uint256 betId3 = roulette.spinForPlayer(player, betAmount, Roulette.BetType.Column3, 0);
        assertEq(betId3, 3);
    }

    function testSpinForPlayerBetTooSmall() public {
        uint256 betAmount = 5 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Red;
        uint256 number = 0;

        vm.expectRevert("Roulette: bet too small");
        roulette.spinForPlayer(player, betAmount, betType, number);
    }

    function testSpinForPlayerBetTooLarge() public {
        uint256 betAmount = 2000 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Red;
        uint256 number = 0;

        vm.expectRevert("Roulette: bet too large");
        roulette.spinForPlayer(player, betAmount, betType, number);
    }

    function testSpinForPlayerInsufficientBalance() public {
        uint256 betAmount = 500 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Red;
        uint256 number = 0;

        address poorPlayer = address(0x2);
        token.transfer(poorPlayer, 100 * 10 ** 18);
        vm.startPrank(poorPlayer);
        token.approve(address(vault), 100 * 10 ** 18);
        vault.deposit(100 * 10 ** 18);
        vm.stopPrank();

        vm.expectRevert("Roulette: insufficient balance");
        roulette.spinForPlayer(poorPlayer, betAmount, betType, number);
    }

    function testSpinForPlayerInvalidSingleNumber() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Single;
        uint256 number = 37;

        vm.expectRevert("Roulette: invalid number for single bet");
        roulette.spinForPlayer(player, betAmount, betType, number);
    }

    function testUpdateGameConfig() public {
        uint256 newMinBet = 20 * 10 ** 18;
        uint256 newMaxBet = 2000 * 10 ** 18;
        uint256 newHouseEdge = 200;

        roulette.updateGameConfig(newMinBet, newMaxBet, newHouseEdge);

        assertEq(roulette.minBet(), newMinBet);
        assertEq(roulette.maxBet(), newMaxBet);
        assertEq(roulette.houseEdge(), newHouseEdge);
    }

    function testPause() public {
        roulette.pause();

        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Red;
        uint256 number = 0;

        vm.expectRevert();
        roulette.spinForPlayer(player, betAmount, betType, number);
    }

    function testUnpause() public {
        roulette.pause();
        roulette.unpause();

        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Red;
        uint256 number = 0;

        uint256 betId = roulette.spinForPlayer(player, betAmount, betType, number);
        assertEq(betId, 1);
    }

    function testGetPlayerLastBet() public {
        uint256 betAmount = 100 * 10 ** 18;
        Roulette.BetType betType = Roulette.BetType.Red;
        uint256 number = 0;

        roulette.spinForPlayer(player, betAmount, betType, number);

        Roulette.Bet memory lastBet = roulette.getPlayerLastBet(player);

        assertEq(lastBet.amount, betAmount);
        assertEq(uint256(lastBet.betType), uint256(betType));
    }
}
