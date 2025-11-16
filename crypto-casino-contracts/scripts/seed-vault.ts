import { network } from "hardhat";
import { parseEther, formatEther } from "viem";

const CASINO_TOKEN_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
const CASINO_VAULT_ADDRESS = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";

async function main() {
  const { viem } = await network.connect();
  const publicClient = await viem.getPublicClient();
  const [deployer] = await viem.getWalletClients();

  const tokenAmount = process.env.TOKEN_AMOUNT || "1000000";

  console.log("Seeding CasinoVault with tokens");
  console.log(`Deployer: ${deployer.account.address}`);
  console.log(`Amount: ${tokenAmount} tokens\n`);

  const casinoToken = await viem.getContractAt(
    "CasinoToken",
    CASINO_TOKEN_ADDRESS
  );

  const casinoVault = await viem.getContractAt(
    "CasinoVault",
    CASINO_VAULT_ADDRESS
  );

  const seedTokenAmount = parseEther(tokenAmount);

  console.log("Step 1: Approving tokens to CasinoVault...");
  const approveTx = await casinoToken.write.approve(
    [CASINO_VAULT_ADDRESS, seedTokenAmount],
    {
      account: deployer.account,
    }
  );
  await publicClient.waitForTransactionReceipt({ hash: approveTx });
  console.log(`Approved ${tokenAmount} tokens`);
  console.log(`TX Hash: ${approveTx}\n`);

  console.log("Step 2: Seeding CasinoVault houseBalance...");
  const seedTx = await casinoVault.write.seedHouseBalance([seedTokenAmount], {
    account: deployer.account,
  });
  await publicClient.waitForTransactionReceipt({ hash: seedTx });

  const houseBalance = await casinoVault.read.houseBalance() as bigint;

  console.log(`Seeded ${tokenAmount} tokens to houseBalance`);
  console.log(`TX Hash: ${seedTx}`);
  console.log(`House Balance: ${formatEther(houseBalance)} tokens`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
