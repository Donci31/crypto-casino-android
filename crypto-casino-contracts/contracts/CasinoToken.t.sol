// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.26;

import "forge-std/Test.sol";
import "./CasinoToken.sol";

contract CasinoTokenTest is Test {
    CasinoToken public token;
    address public owner;
    address public user1;
    address public user2;

    uint256 constant INITIAL_SUPPLY = 1000000 * 10**18;
    uint256 constant RATE = 1000;

    event TokensPurchased(address indexed userAddress, uint256 tokenAmount, uint256 timestamp);
    event TokensExchanged(address indexed userAddress, uint256 tokenAmount, uint256 timestamp);

    function setUp() public {
        owner = address(this);
        user1 = makeAddr("user1");
        user2 = makeAddr("user2");

        token = new CasinoToken();

        vm.deal(user1, 100 ether);
        vm.deal(user2, 100 ether);
    }

    function test_InitialState() public view {
        assertEq(token.name(), "CasinoToken");
        assertEq(token.symbol(), "CST");
        assertEq(token.decimals(), 18);
        assertEq(token.totalSupply(), INITIAL_SUPPLY);
        assertEq(token.balanceOf(owner), INITIAL_SUPPLY);
        assertEq(token.rate(), RATE);
        assertEq(token.owner(), owner);
    }

    function test_PurchaseTokens() public {
        uint256 ethAmount = 1 ether;
        uint256 expectedTokens = ethAmount * RATE;

        vm.prank(user1);
        vm.expectEmit(true, false, false, true);
        emit TokensPurchased(user1, expectedTokens, block.timestamp);

        token.purchaseTokens{value: ethAmount}();

        assertEq(token.balanceOf(user1), expectedTokens);
        assertEq(address(token).balance, ethAmount);
    }

    function test_PurchaseTokensMultipleTimes() public {
        vm.startPrank(user1);
        token.purchaseTokens{value: 1 ether}();
        token.purchaseTokens{value: 2 ether}();
        vm.stopPrank();

        assertEq(token.balanceOf(user1), 3000 * 10**18);
        assertEq(address(token).balance, 3 ether);
    }

    function test_RevertPurchaseTokensWithZeroEth() public {
        vm.prank(user1);
        vm.expectRevert("Must send ETH to purchase tokens");
        token.purchaseTokens{value: 0}();
    }

    function test_RevertPurchaseTokensInsufficientOwnerBalance() public {
        uint256 largeAmount = (INITIAL_SUPPLY / RATE) + 1 ether;

        vm.deal(user1, largeAmount);

        vm.prank(user1);
        vm.expectRevert();
        token.purchaseTokens{value: largeAmount}();
    }

    function test_ExchangeTokens() public {
        vm.prank(user1);
        token.purchaseTokens{value: 10 ether}();

        uint256 tokenAmount = 5000 * 10**18;

        vm.prank(user1);
        vm.expectEmit(true, false, false, true);
        emit TokensExchanged(user1, tokenAmount, block.timestamp);

        token.exchangeTokens(tokenAmount);

        assertEq(token.balanceOf(user1), 5000 * 10**18);
        assertEq(address(token).balance, 5 ether);
    }

    function test_RevertExchangeTokensZeroAmount() public {
        vm.prank(user1);
        vm.expectRevert("Amount must be greater than 0");
        token.exchangeTokens(0);
    }

    function test_RevertExchangeTokensInsufficientBalance() public {
        vm.prank(user1);
        vm.expectRevert("Insufficient token balance");
        token.exchangeTokens(1000 * 10**18);
    }

    function test_RevertExchangeTokensInsufficientContractEth() public {
        token.transfer(user1, 1000 * 10**18);

        vm.prank(user1);
        vm.expectRevert("Insufficient ETH in contract");
        token.exchangeTokens(1000 * 10**18);
    }

    function test_WithdrawEth() public {
        vm.prank(user1);
        token.purchaseTokens{value: 5 ether}();

        uint256 ownerBalanceBefore = owner.balance;

        token.withdrawEth();

        assertEq(owner.balance, ownerBalanceBefore + 5 ether);
        assertEq(address(token).balance, 0);
    }

    function test_RevertWithdrawEthNoBalance() public {
        vm.expectRevert("No ETH to withdraw");
        token.withdrawEth();
    }

    function test_RevertWithdrawEthNotOwner() public {
        vm.prank(user1);
        token.purchaseTokens{value: 1 ether}();

        vm.prank(user2);
        vm.expectRevert();
        token.withdrawEth();
    }

    function test_PurchaseAndExchangeRoundTrip() public {
        vm.startPrank(user1);

        uint256 initialBalance = user1.balance;
        token.purchaseTokens{value: 10 ether}();

        assertEq(token.balanceOf(user1), 10000 * 10**18);

        token.exchangeTokens(10000 * 10**18);

        assertEq(token.balanceOf(user1), 0);
        assertEq(user1.balance, initialBalance);

        vm.stopPrank();
    }

    function testFuzz_PurchaseTokens(uint96 ethAmount) public {
        vm.assume(ethAmount > 0);
        vm.assume(uint256(ethAmount) * RATE <= INITIAL_SUPPLY);

        vm.deal(user1, ethAmount);

        vm.prank(user1);
        token.purchaseTokens{value: ethAmount}();

        assertEq(token.balanceOf(user1), uint256(ethAmount) * RATE);
    }

    function testFuzz_ExchangeTokens(uint96 tokenAmount) public {
        vm.assume(tokenAmount > 0);
        vm.assume(tokenAmount <= 10000 * 10**18);

        vm.prank(user1);
        token.purchaseTokens{value: 10 ether}();

        vm.prank(user1);
        token.exchangeTokens(tokenAmount);

        assertEq(token.balanceOf(user1), 10000 * 10**18 - tokenAmount);
    }

    receive() external payable {}
}
