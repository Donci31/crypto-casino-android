// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "forge-std/Test.sol";
import "./SlotMachine.sol";
import "./CasinoVault.sol";
import "./CasinoToken.sol";

contract SlotMachineTest is Test {
    SlotMachine public slotMachine;
    CasinoVault public vault;
    CasinoToken public token;
    address public owner;
    address public player1;
    address public player2;

    uint256 constant MIN_BET = 10 * 10**18;
    uint256 constant MAX_BET = 1000 * 10**18;
    uint256 constant HOUSE_EDGE = 100;
    uint256 constant INITIAL_TOKENS = 100000 * 10**18;

    event SpinStarted(address indexed player, uint256 bet, uint256 spinId, uint256 timestamp);
    event SpinResult(address indexed player, uint256 spinId, uint256[3] reels, uint256 winAmount, uint256 timestamp);
    event GameConfigUpdated(uint256 minBet, uint256 maxBet, uint256 houseEdge, uint256 timestamp);

    function setUp() public {
        owner = address(this);
        player1 = makeAddr("player1");
        player2 = makeAddr("player2");

        token = new CasinoToken();
        vault = new CasinoVault(address(token));
        slotMachine = new SlotMachine(address(vault), MIN_BET, MAX_BET, HOUSE_EDGE);

        vault.authorizeGame(address(slotMachine));

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
        assertEq(address(slotMachine.vault()), address(vault));
        assertEq(slotMachine.minBet(), MIN_BET);
        assertEq(slotMachine.maxBet(), MAX_BET);
        assertEq(slotMachine.houseEdge(), HOUSE_EDGE);
        assertEq(slotMachine.owner(), owner);
        assertFalse(slotMachine.paused());
    }

    function test_RevertConstructorInvalidVault() public {
        vm.expectRevert("SlotMachine: invalid vault address");
        new SlotMachine(address(0), MIN_BET, MAX_BET, HOUSE_EDGE);
    }

    function test_RevertConstructorInvalidMinBet() public {
        vm.expectRevert("SlotMachine: minBet must be positive");
        new SlotMachine(address(vault), 0, MAX_BET, HOUSE_EDGE);
    }

    function test_RevertConstructorInvalidMaxBet() public {
        vm.expectRevert("SlotMachine: maxBet must be greater than minBet");
        new SlotMachine(address(vault), MAX_BET, MIN_BET, HOUSE_EDGE);
    }

    function test_RevertConstructorInvalidHouseEdge() public {
        vm.expectRevert("SlotMachine: houseEdge cannot exceed 20%");
        new SlotMachine(address(vault), MIN_BET, MAX_BET, 2001);
    }

    function test_SpinForPlayer() public {
        uint256 betAmount = 100 * 10**18;

        vm.expectEmit(true, false, false, false);
        emit SpinStarted(player1, betAmount, 1, block.timestamp);

        uint256 spinId = slotMachine.spinForPlayer(player1, betAmount);

        assertEq(spinId, 1);
        assertEq(slotMachine.playerLastSpin(player1), spinId);
    }

    function test_SpinMultipleTimes() public {
        uint256 spinId1 = slotMachine.spinForPlayer(player1, 100 * 10**18);
        uint256 spinId2 = slotMachine.spinForPlayer(player1, 200 * 10**18);
        uint256 spinId3 = slotMachine.spinForPlayer(player2, 150 * 10**18);

        assertEq(spinId1, 1);
        assertEq(spinId2, 2);
        assertEq(spinId3, 3);
        assertEq(slotMachine.playerLastSpin(player1), spinId2);
        assertEq(slotMachine.playerLastSpin(player2), spinId3);
    }

    function test_RevertSpinBetTooSmall() public {
        vm.expectRevert("SlotMachine: bet too small");
        slotMachine.spinForPlayer(player1, MIN_BET - 1);
    }

    function test_RevertSpinBetTooLarge() public {
        vm.expectRevert("SlotMachine: bet too large");
        slotMachine.spinForPlayer(player1, MAX_BET + 1);
    }

    function test_RevertSpinInsufficientBalance() public {
        vm.expectRevert("SlotMachine: bet too large");
        slotMachine.spinForPlayer(player1, INITIAL_TOKENS + 1);
    }

    function test_RevertSpinNotOwner() public {
        vm.prank(player1);
        vm.expectRevert();
        slotMachine.spinForPlayer(player1, 100 * 10**18);
    }

    function test_GetSpinState() public {
        uint256 betAmount = 100 * 10**18;
        uint256 spinId = slotMachine.spinForPlayer(player1, betAmount);

        SlotMachine.Spin memory spin = slotMachine.getSpinState(spinId);

        assertEq(spin.bet, betAmount);
        assertTrue(spin.resolved);
        assertTrue(spin.reels[0] < 10);
        assertTrue(spin.reels[1] < 10);
        assertTrue(spin.reels[2] < 10);
    }

    function test_GetPlayerLastSpin() public {
        uint256 betAmount = 100 * 10**18;
        slotMachine.spinForPlayer(player1, betAmount);

        SlotMachine.Spin memory spin = slotMachine.getPlayerLastSpin(player1);

        assertEq(spin.bet, betAmount);
        assertTrue(spin.resolved);
    }

    function test_RevertGetPlayerLastSpinNoSpins() public {
        vm.expectRevert("SlotMachine: no spins found for player");
        slotMachine.getPlayerLastSpin(player1);
    }

    function test_UpdateGameConfig() public {
        uint256 newMinBet = 50 * 10**18;
        uint256 newMaxBet = 5000 * 10**18;
        uint256 newHouseEdge = 200;

        vm.expectEmit(false, false, false, true);
        emit GameConfigUpdated(newMinBet, newMaxBet, newHouseEdge, block.timestamp);

        slotMachine.updateGameConfig(newMinBet, newMaxBet, newHouseEdge);

        assertEq(slotMachine.minBet(), newMinBet);
        assertEq(slotMachine.maxBet(), newMaxBet);
        assertEq(slotMachine.houseEdge(), newHouseEdge);
    }

    function test_RevertUpdateGameConfigInvalidMinBet() public {
        vm.expectRevert("SlotMachine: minBet must be positive");
        slotMachine.updateGameConfig(0, MAX_BET, HOUSE_EDGE);
    }

    function test_RevertUpdateGameConfigInvalidMaxBet() public {
        vm.expectRevert("SlotMachine: maxBet must be greater than minBet");
        slotMachine.updateGameConfig(MAX_BET, MIN_BET, HOUSE_EDGE);
    }

    function test_RevertUpdateGameConfigInvalidHouseEdge() public {
        vm.expectRevert("SlotMachine: houseEdge cannot exceed 20%");
        slotMachine.updateGameConfig(MIN_BET, MAX_BET, 2001);
    }

    function test_RevertUpdateGameConfigNotOwner() public {
        vm.prank(player1);
        vm.expectRevert();
        slotMachine.updateGameConfig(MIN_BET, MAX_BET, HOUSE_EDGE);
    }

    function test_Pause() public {
        slotMachine.pause();
        assertTrue(slotMachine.paused());
    }

    function test_RevertPauseNotOwner() public {
        vm.prank(player1);
        vm.expectRevert();
        slotMachine.pause();
    }

    function test_Unpause() public {
        slotMachine.pause();
        slotMachine.unpause();
        assertFalse(slotMachine.paused());
    }

    function test_RevertUnpauseNotOwner() public {
        slotMachine.pause();

        vm.prank(player1);
        vm.expectRevert();
        slotMachine.unpause();
    }

    function test_RevertSpinWhenPaused() public {
        slotMachine.pause();

        vm.expectRevert();
        slotMachine.spinForPlayer(player1, 100 * 10**18);
    }

    function test_SpinAfterUnpause() public {
        slotMachine.pause();
        slotMachine.unpause();

        uint256 spinId = slotMachine.spinForPlayer(player1, 100 * 10**18);
        assertEq(spinId, 1);
    }

    function test_WinningsCalculation() public {
        uint256 totalSpins = 100;
        uint256 totalWinnings = 0;
        uint256 totalBets = 0;
        uint256 betAmount = 100 * 10**18;

        for (uint256 i = 0; i < totalSpins; i++) {
            uint256 spinId = slotMachine.spinForPlayer(player1, betAmount);
            SlotMachine.Spin memory spin = slotMachine.getSpinState(spinId);
            totalWinnings += spin.winAmount;
            totalBets += betAmount;
        }

        assertTrue(totalWinnings < totalBets);
    }

    function testFuzz_SpinForPlayer(uint96 betAmount) public {
        vm.assume(betAmount >= MIN_BET);
        vm.assume(betAmount <= MAX_BET);
        vm.assume(betAmount <= INITIAL_TOKENS);

        uint256 spinId = slotMachine.spinForPlayer(player1, betAmount);

        SlotMachine.Spin memory spin = slotMachine.getSpinState(spinId);

        assertEq(spin.bet, betAmount);
        assertTrue(spin.resolved);
        assertTrue(spin.reels[0] < 10);
        assertTrue(spin.reels[1] < 10);
        assertTrue(spin.reels[2] < 10);
    }

    function testFuzz_UpdateGameConfig(uint96 minBet, uint96 maxBet, uint16 houseEdge) public {
        vm.assume(minBet > 0);
        vm.assume(maxBet > minBet);
        vm.assume(houseEdge <= 2000);

        slotMachine.updateGameConfig(minBet, maxBet, houseEdge);

        assertEq(slotMachine.minBet(), minBet);
        assertEq(slotMachine.maxBet(), maxBet);
        assertEq(slotMachine.houseEdge(), houseEdge);
    }
}
