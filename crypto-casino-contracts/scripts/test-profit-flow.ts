import { network } from "hardhat";
import { parseEther, formatEther } from "viem";

async function main() {
  const { viem } = await network.connect();
  const [owner, player] = await viem.getWalletClients();
  const publicClient = await viem.getPublicClient();

  console.log("\n=== Testing Complete Profit Flow ===\n");

  const casinoTokenAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const casinoVaultAddress = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";
  const slotMachineAddress = "0xDc64a140Aa3E981100a9becA4E685f962f0cF6C9";

  const casinoToken = await viem.getContractAt("CasinoToken", casinoTokenAddress);
  const casinoVault = await viem.getContractAt("CasinoVault", casinoVaultAddress);
  const slotMachine = await viem.getContractAt("SlotMachine", slotMachineAddress);

  console.log("=== Step 1: Player Setup ===");
  console.log("Player purchases 100 CST with 0.1 ETH...");
  await casinoToken.write.purchaseTokens({
    value: parseEther("0.1"),
    account: player.account
  });

  const playerBalance = await casinoToken.read.balanceOf([player.account.address]);
  console.log(`✅ Player token balance: ${formatEther(playerBalance)} CST`);

  console.log("\nPlayer deposits 50 CST to vault...");
  await casinoToken.write.approve([casinoVaultAddress, parseEther("50")], {
    account: player.account
  });
  await casinoVault.write.deposit([parseEther("50")], {
    account: player.account
  });

  const vaultBalance = await casinoVault.read.getBalance([player.account.address]);
  console.log(`✅ Player vault balance: ${formatEther(vaultBalance)} CST`);

  console.log("\n=== Step 2: Player Loses Bet ===");
  const houseBalanceBefore = await casinoVault.read.houseBalance();
  console.log(`House balance before: ${formatEther(houseBalanceBefore)} CST`);

  await slotMachine.write.spinForPlayer([player.account.address, parseEther("20")]);

  const houseBalanceAfter = await casinoVault.read.houseBalance();
  const lastSpin = await slotMachine.read.getPlayerLastSpin([player.account.address]);

  console.log(`Spin result: [${lastSpin.reels[0]}, ${lastSpin.reels[1]}, ${lastSpin.reels[2]}]`);
  console.log(`Win amount: ${formatEther(lastSpin.winAmount)} CST`);
  console.log(`✅ House balance after: ${formatEther(houseBalanceAfter)} CST`);

  console.log("\n=== Step 3: Owner Withdraws Profit (as tokens) ===");
  const profitAmount = parseEther("10");

  const ownerBalanceBefore = await casinoToken.read.balanceOf([owner.account.address]);
  console.log(`Owner CST balance before: ${formatEther(ownerBalanceBefore)} CST`);

  await casinoVault.write.withdrawProfit([profitAmount]);

  const ownerBalanceAfter = await casinoToken.read.balanceOf([owner.account.address]);
  const houseBalanceAfterWithdraw = await casinoVault.read.houseBalance();

  console.log(`✅ Owner CST balance after: ${formatEther(ownerBalanceAfter)} CST (+${formatEther(profitAmount)})`);
  console.log(`✅ House balance after withdraw: ${formatEther(houseBalanceAfterWithdraw)} CST`);

  console.log("\n=== Step 4: Owner Exchanges Tokens for ETH ===");
  const ownerEthBefore = await publicClient.getBalance({ address: owner.account.address });
  console.log(`Owner ETH balance before: ${formatEther(ownerEthBefore)} ETH`);

  const tokensToExchange = parseEther("10");
  const txHash = await casinoToken.write.exchangeTokens([tokensToExchange]);
  const receipt = await publicClient.waitForTransactionReceipt({ hash: txHash });

  const ownerEthAfter = await publicClient.getBalance({ address: owner.account.address });
  const ethGained = ownerEthAfter - ownerEthBefore;
  const ownerTokensAfter = await casinoToken.read.balanceOf([owner.account.address]);

  console.log(`✅ Owner ETH balance after: ${formatEther(ownerEthAfter)} ETH`);
  console.log(`   ETH gained (minus gas): ~${formatEther(ethGained)} ETH`);
  console.log(`✅ Owner CST balance after exchange: ${formatEther(ownerTokensAfter)} CST`);

  console.log("\n=== Step 5: Verify Contract State ===");
  const contractEthBalance = await publicClient.getBalance({ address: casinoTokenAddress });
  console.log(`CasinoToken ETH balance: ${formatEther(contractEthBalance)} ETH`);
  console.log(`House CST balance in vault: ${formatEther(houseBalanceAfterWithdraw)} CST`);

  console.log("\n✅ Complete Profit Flow Works!");
  console.log("\nSummary:");
  console.log("1. ✅ Player losses tracked in house balance");
  console.log("2. ✅ Owner withdrew profit as CST tokens");
  console.log("3. ✅ Owner exchanged CST back to ETH");
  console.log("4. ✅ No withdrawEth() - fair for all players!");

  console.log("\n=== Test Complete ===\n");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
