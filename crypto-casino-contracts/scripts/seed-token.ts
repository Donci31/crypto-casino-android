import { network } from "hardhat";
import { parseEther, formatEther } from "viem";

const CASINO_TOKEN_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

async function main() {
  const { viem } = await network.connect();
  const publicClient = await viem.getPublicClient();
  const [deployer] = await viem.getWalletClients();

  const ethAmount = process.env.ETH_AMOUNT || "100";

  console.log("Seeding CasinoToken with ETH");
  console.log(`Deployer: ${deployer.account.address}`);
  console.log(`Amount: ${ethAmount} ETH\n`);

  const casinoToken = await viem.getContractAt(
    "CasinoToken",
    CASINO_TOKEN_ADDRESS
  );

  const seedEthAmount = parseEther(ethAmount);

  console.log("Depositing ETH to CasinoToken...");
  const depositTx = await casinoToken.write.depositEth({
    value: seedEthAmount,
    account: deployer.account,
  });
  await publicClient.waitForTransactionReceipt({ hash: depositTx });

  const balance = await publicClient.getBalance({
    address: CASINO_TOKEN_ADDRESS,
  });

  console.log(`Deposited ${ethAmount} ETH to CasinoToken`);
  console.log(`TX Hash: ${depositTx}`);
  console.log(`CasinoToken ETH Balance: ${formatEther(balance)} ETH`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
