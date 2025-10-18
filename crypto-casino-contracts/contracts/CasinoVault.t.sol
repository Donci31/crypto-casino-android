// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "forge-std/Test.sol";
import "./CasinoVault.sol";
import "./CasinoToken.sol";

contract CasinoVaultTest is Test {
    CasinoVault public vault;
    CasinoToken public token;
    address public owner;
    address public user1;
    address public user2;
    address public game1;
    address public game2;

    uint256 constant INITIAL_TOKENS = 10000 * 10**18;

    event GameAuthorized(address indexed gameAddress, uint256 timestamp);
    event GameDeauthorized(address indexed gameAddress, uint256 timestamp);
    event Deposit(address indexed userAddress, uint256 amount, uint256 newBalance, uint256 timestamp);
    event Withdrawal(address indexed userAddress, uint256 amount, uint256 newBalance, uint256 timestamp);
    event BetPlaced(address indexed userAddress, uint256 amount, uint256 newBalance, address indexed gameAddress, uint256 timestamp);
    event WinPaid(address indexed userAddress, uint256 amount, uint256 newBalance, address indexed gameAddress, uint256 timestamp);

    function setUp() public {
        owner = address(this);
        user1 = makeAddr("user1");
        user2 = makeAddr("user2");
        game1 = makeAddr("game1");
        game2 = makeAddr("game2");

        token = new CasinoToken();
        vault = new CasinoVault(address(token));

        token.transfer(user1, INITIAL_TOKENS);
        token.transfer(user2, INITIAL_TOKENS);
    }

    function test_InitialState() public view {
        assertEq(address(vault.token()), address(token));
        assertEq(vault.owner(), owner);
        assertFalse(vault.authorizedGames(game1));
        assertEq(vault.balances(user1), 0);
    }

    function test_AuthorizeGame() public {
        vm.expectEmit(true, false, false, true);
        emit GameAuthorized(game1, block.timestamp);

        vault.authorizeGame(game1);

        assertTrue(vault.authorizedGames(game1));
        assertTrue(vault.isGameAuthorized(game1));
    }

    function test_RevertAuthorizeGameAlreadyAuthorized() public {
        vault.authorizeGame(game1);

        vm.expectRevert("TokenGameVault: game already authorized");
        vault.authorizeGame(game1);
    }

    function test_RevertAuthorizeGameZeroAddress() public {
        vm.expectRevert("TokenGameVault: invalid address");
        vault.authorizeGame(address(0));
    }

    function test_RevertAuthorizeGameNotOwner() public {
        vm.prank(user1);
        vm.expectRevert();
        vault.authorizeGame(game1);
    }

    function test_DeauthorizeGame() public {
        vault.authorizeGame(game1);

        vm.expectEmit(true, false, false, true);
        emit GameDeauthorized(game1, block.timestamp);

        vault.deauthorizeGame(game1);

        assertFalse(vault.authorizedGames(game1));
        assertFalse(vault.isGameAuthorized(game1));
    }

    function test_RevertDeauthorizeGameNotAuthorized() public {
        vm.expectRevert("TokenGameVault: game not authorized");
        vault.deauthorizeGame(game1);
    }

    function test_RevertDeauthorizeGameNotOwner() public {
        vault.authorizeGame(game1);

        vm.prank(user1);
        vm.expectRevert();
        vault.deauthorizeGame(game1);
    }

    function test_Deposit() public {
        uint256 depositAmount = 1000 * 10**18;

        vm.startPrank(user1);
        token.approve(address(vault), depositAmount);

        vm.expectEmit(true, false, false, true);
        emit Deposit(user1, depositAmount, depositAmount, block.timestamp);

        vault.deposit(depositAmount);
        vm.stopPrank();

        assertEq(vault.balances(user1), depositAmount);
        assertEq(vault.getBalance(user1), depositAmount);
        assertEq(token.balanceOf(address(vault)), depositAmount);
    }

    function test_DepositMultipleTimes() public {
        vm.startPrank(user1);
        token.approve(address(vault), 5000 * 10**18);

        vault.deposit(1000 * 10**18);
        vault.deposit(2000 * 10**18);
        vm.stopPrank();

        assertEq(vault.balances(user1), 3000 * 10**18);
    }

    function test_RevertDepositZeroAmount() public {
        vm.prank(user1);
        vm.expectRevert("TokenGameVault: amount must be greater than 0");
        vault.deposit(0);
    }

    function test_RevertDepositInsufficientApproval() public {
        vm.prank(user1);
        vm.expectRevert();
        vault.deposit(1000 * 10**18);
    }

    function test_Withdraw() public {
        uint256 depositAmount = 5000 * 10**18;
        uint256 withdrawAmount = 2000 * 10**18;

        vm.startPrank(user1);
        token.approve(address(vault), depositAmount);
        vault.deposit(depositAmount);

        vm.expectEmit(true, false, false, true);
        emit Withdrawal(user1, withdrawAmount, depositAmount - withdrawAmount, block.timestamp);

        vault.withdraw(withdrawAmount);
        vm.stopPrank();

        assertEq(vault.balances(user1), depositAmount - withdrawAmount);
        assertEq(token.balanceOf(user1), INITIAL_TOKENS - depositAmount + withdrawAmount);
    }

    function test_RevertWithdrawZeroAmount() public {
        vm.prank(user1);
        vm.expectRevert("TokenGameVault: amount must be greater than 0");
        vault.withdraw(0);
    }

    function test_RevertWithdrawInsufficientBalance() public {
        vm.prank(user1);
        vm.expectRevert("TokenGameVault: insufficient balance");
        vault.withdraw(1000 * 10**18);
    }

    function test_PlaceBet() public {
        uint256 depositAmount = 5000 * 10**18;
        uint256 betAmount = 1000 * 10**18;

        vm.startPrank(user1);
        token.approve(address(vault), depositAmount);
        vault.deposit(depositAmount);
        vm.stopPrank();

        vault.authorizeGame(game1);

        vm.prank(game1);
        vm.expectEmit(true, true, false, true);
        emit BetPlaced(user1, betAmount, depositAmount - betAmount, game1, block.timestamp);

        bool success = vault.placeBet(user1, betAmount);

        assertTrue(success);
        assertEq(vault.balances(user1), depositAmount - betAmount);
    }

    function test_RevertPlaceBetNotAuthorized() public {
        vm.prank(game1);
        vm.expectRevert("TokenGameVault: not an authorized game");
        vault.placeBet(user1, 1000 * 10**18);
    }

    function test_RevertPlaceBetZeroAmount() public {
        vault.authorizeGame(game1);

        vm.prank(game1);
        vm.expectRevert("TokenGameVault: amount must be greater than 0");
        vault.placeBet(user1, 0);
    }

    function test_RevertPlaceBetZeroAddress() public {
        vault.authorizeGame(game1);

        vm.prank(game1);
        vm.expectRevert("TokenGameVault: invalid address");
        vault.placeBet(address(0), 1000 * 10**18);
    }

    function test_RevertPlaceBetInsufficientBalance() public {
        vault.authorizeGame(game1);

        vm.prank(game1);
        vm.expectRevert("TokenGameVault: insufficient balance");
        vault.placeBet(user1, 1000 * 10**18);
    }

    function test_PayWinnings() public {
        uint256 winAmount = 5000 * 10**18;

        vault.authorizeGame(game1);

        vm.prank(game1);
        vm.expectEmit(true, true, false, true);
        emit WinPaid(user1, winAmount, winAmount, game1, block.timestamp);

        bool success = vault.payWinnings(user1, winAmount);

        assertTrue(success);
        assertEq(vault.balances(user1), winAmount);
    }

    function test_RevertPayWinningsNotAuthorized() public {
        vm.prank(game1);
        vm.expectRevert("TokenGameVault: not an authorized game");
        vault.payWinnings(user1, 1000 * 10**18);
    }

    function test_RevertPayWinningsZeroAmount() public {
        vault.authorizeGame(game1);

        vm.prank(game1);
        vm.expectRevert("TokenGameVault: amount must be greater than 0");
        vault.payWinnings(user1, 0);
    }

    function test_RevertPayWinningsZeroAddress() public {
        vault.authorizeGame(game1);

        vm.prank(game1);
        vm.expectRevert("TokenGameVault: invalid address");
        vault.payWinnings(address(0), 1000 * 10**18);
    }

    function test_FullGameFlow() public {
        uint256 depositAmount = 10000 * 10**18;
        uint256 betAmount = 1000 * 10**18;
        uint256 winAmount = 2000 * 10**18;

        vm.startPrank(user1);
        token.approve(address(vault), depositAmount);
        vault.deposit(depositAmount);
        vm.stopPrank();

        vault.authorizeGame(game1);

        vm.startPrank(game1);
        vault.placeBet(user1, betAmount);
        vault.payWinnings(user1, winAmount);
        vm.stopPrank();

        assertEq(vault.balances(user1), depositAmount - betAmount + winAmount);
    }

    function test_MultipleGamesAndUsers() public {
        vault.authorizeGame(game1);
        vault.authorizeGame(game2);

        vm.startPrank(user1);
        token.approve(address(vault), 5000 * 10**18);
        vault.deposit(5000 * 10**18);
        vm.stopPrank();

        vm.startPrank(user2);
        token.approve(address(vault), 3000 * 10**18);
        vault.deposit(3000 * 10**18);
        vm.stopPrank();

        vm.prank(game1);
        vault.placeBet(user1, 1000 * 10**18);

        vm.prank(game2);
        vault.placeBet(user2, 500 * 10**18);

        assertEq(vault.balances(user1), 4000 * 10**18);
        assertEq(vault.balances(user2), 2500 * 10**18);
    }

    function testFuzz_Deposit(uint96 amount) public {
        vm.assume(amount > 0);
        vm.assume(amount <= INITIAL_TOKENS);

        vm.startPrank(user1);
        token.approve(address(vault), amount);
        vault.deposit(amount);
        vm.stopPrank();

        assertEq(vault.balances(user1), amount);
    }

    function testFuzz_PlaceBet(uint96 depositAmount, uint96 betAmount) public {
        vm.assume(depositAmount > 0);
        vm.assume(betAmount > 0);
        vm.assume(depositAmount <= INITIAL_TOKENS);
        vm.assume(betAmount <= depositAmount);

        vm.startPrank(user1);
        token.approve(address(vault), depositAmount);
        vault.deposit(depositAmount);
        vm.stopPrank();

        vault.authorizeGame(game1);

        vm.prank(game1);
        vault.placeBet(user1, betAmount);

        assertEq(vault.balances(user1), depositAmount - betAmount);
    }
}
