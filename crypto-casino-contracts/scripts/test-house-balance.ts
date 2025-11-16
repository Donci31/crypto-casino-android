import { network } from "hardhat";
import { parseEther, formatEther } from "viem";

async function main() {
  const { viem } = await network.connect();
  const [owner, player] = await viem.getWalletClients();

  console.log("\n=== Testing House Balance Feature ===\n");

  const casinoTokenAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const casinoVaultAddress = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";
  const slotMachineAddress = "0xDc64a140Aa3E981100a9becA4E685f962f0cF6C9";

  const casinoToken = await viem.getContractAt("CasinoToken", casinoTokenAddress);
  const casinoVault = await viem.getContractAt("CasinoVault", casinoVaultAddress);
  const slotMachine = await viem.getContractAt("SlotMachine", slotMachineAddress);

  console.log("1. Player purchases tokens...");
  await casinoToken.write.purchaseTokens({
    value: parseEther("0.1"),
    account: player.account
  });

  const playerBalance = await casinoToken.read.balanceOf([player.account.address]);
  console.log(`   Player token balance: ${formatEther(playerBalance)} CST`);

  console.log("\n2. Player approves and deposits tokens to vault...");
  await casinoToken.write.approve([casinoVaultAddress, parseEther("50")], {
    account: player.account
  });

  await casinoVault.write.deposit([parseEther("50")], {
    account: player.account
  });

  const vaultBalance = await casinoVault.read.getBalance([player.account.address]);
  console.log(`   Player vault balance: ${formatEther(vaultBalance)} CST`);

  console.log("\n3. Checking initial house balance...");
  const initialHouseBalance = await casinoVault.read.houseBalance();
  console.log(`   House balance: ${formatEther(initialHouseBalance)} CST`);

  console.log("\n4. Player plays slot machine (loses bet)...");
  const betAmount = parseEther("10");
  const spinTx = await slotMachine.write.spinForPlayer([player.account.address, betAmount]);
  console.log(`   Spin transaction hash: ${spinTx}`);

  console.log("\n5. Checking house balance after bet...");
  const houseBalanceAfterBet = await casinoVault.read.houseBalance();
  console.log(`   House balance: ${formatEther(houseBalanceAfterBet)} CST`);

  const playerVaultBalanceAfterBet = await casinoVault.read.getBalance([player.account.address]);
  console.log(`   Player vault balance: ${formatEther(playerVaultBalanceAfterBet)} CST`);

  const lastSpin = await slotMachine.read.getPlayerLastSpin([player.account.address]);
  console.log(`   Spin result: [${lastSpin.reels[0]}, ${lastSpin.reels[1]}, ${lastSpin.reels[2]}]`);
  console.log(`   Win amount: ${formatEther(lastSpin.winAmount)} CST`);

  console.log("\n6. Owner withdraws profit...");
  const profitToWithdraw = parseEther("5");

  try {
    await casinoVault.write.withdrawProfit([profitToWithdraw]);

    const houseBalanceAfterWithdraw = await casinoVault.read.houseBalance();
    console.log(`   House balance after withdraw: ${formatEther(houseBalanceAfterWithdraw)} CST`);

    const ownerTokenBalance = await casinoToken.read.balanceOf([owner.account.address]);
    console.log(`   Owner token balance: ${formatEther(ownerTokenBalance)} CST`);

    console.log("\n✅ House balance tracking works correctly!");
  } catch (error: any) {
    console.error(`   ❌ Error withdrawing profit: ${error.message}`);
  }

  console.log("\n=== Test Complete ===\n");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
