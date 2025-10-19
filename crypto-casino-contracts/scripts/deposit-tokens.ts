import { network } from "hardhat";
import { parseEther, formatEther } from "viem";

const CASINO_TOKEN_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
const CASINO_VAULT_ADDRESS = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";

async function main() {
  console.log("💰 CryptoCasino - Token Purchase & Deposit\n");

  const { viem } = await network.connect();
  const publicClient = await viem.getPublicClient();
  const [deployer, testUser] = await viem.getWalletClients();

  console.log(`Using test user: ${testUser.account.address}\n`);

  const casinoToken = await viem.getContractAt(
    "CasinoToken",
    CASINO_TOKEN_ADDRESS
  );

  const casinoVault = await viem.getContractAt(
    "CasinoVault",
    CASINO_VAULT_ADDRESS
  );

  console.log("Step 1: Purchasing tokens with ETH...");
  console.log("   Buying 1000 CST (costs 1 ETH)\n");

  const purchaseHash = await casinoToken.write.purchaseTokens({
    value: parseEther("1"),
    account: testUser.account,
  });

  await publicClient.waitForTransactionReceipt({ hash: purchaseHash });

  console.log("   ✅ Tokens purchased!");
  console.log(`   TX Hash: ${purchaseHash}\n`);

  console.log("Step 2: Checking token balance...");
  const tokenBalance = await casinoToken.read.balanceOf([
    testUser.account.address,
  ]);

  console.log(`   💰 Token Balance: ${formatEther(tokenBalance)} CST\n`);

  console.log("Step 3: Approving CasinoVault to spend tokens...");
  console.log("   Approving 500 CST for deposit\n");

  const depositAmount = parseEther("500");

  const approveHash = await casinoToken.write.approve(
    [CASINO_VAULT_ADDRESS, depositAmount],
    {
      account: testUser.account,
    }
  );

  await publicClient.waitForTransactionReceipt({ hash: approveHash });

  console.log("   ✅ Approval granted!");
  console.log(`   TX Hash: ${approveHash}\n`);

  console.log("Step 4: Depositing tokens to CasinoVault...");
  const depositHash = await casinoVault.write.deposit([depositAmount], {
    account: testUser.account,
  });

  await publicClient.waitForTransactionReceipt({ hash: depositHash });

  console.log("   ✅ Tokens deposited to vault!");
  console.log(`   TX Hash: ${depositHash}\n`);

  console.log("Step 5: Checking vault balance...");
  const vaultBalance = await casinoVault.read.getBalance([
    testUser.account.address,
  ]);

  console.log(`   🏦 Vault Balance: ${formatEther(vaultBalance)} CST\n`);

  console.log("✨ Setup complete! You can now play dice games.");
  console.log(`   💰 Wallet Balance: ${formatEther(tokenBalance)} CST`);
  console.log(`   🏦 Vault Balance: ${formatEther(vaultBalance)} CST`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
