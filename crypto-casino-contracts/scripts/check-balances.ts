import { network } from "hardhat";
import { formatEther } from "viem";

const CASINO_TOKEN_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
const CASINO_VAULT_ADDRESS = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";

async function main() {
  const { viem } = await network.connect();
  const publicClient = await viem.getPublicClient();
  const [owner, user1, user2] = await viem.getWalletClients();

  console.log("Checking all balances...\n");

  const casinoToken = await viem.getContractAt(
    "CasinoToken",
    CASINO_TOKEN_ADDRESS
  );

  const casinoVault = await viem.getContractAt(
    "CasinoVault",
    CASINO_VAULT_ADDRESS
  );

  console.log("=== Contract Balances ===");

  const tokenEthBalance = await publicClient.getBalance({
    address: CASINO_TOKEN_ADDRESS,
  });
  console.log(`CasinoToken ETH: ${formatEther(tokenEthBalance)} ETH`);

  const vaultTokenBalance = await casinoToken.read.balanceOf([
    CASINO_VAULT_ADDRESS,
  ]) as bigint;
  console.log(`CasinoVault Tokens: ${formatEther(vaultTokenBalance)} CST`);

  const houseBalance = await casinoVault.read.houseBalance() as bigint;
  console.log(`House Balance: ${formatEther(houseBalance)} CST`);

  console.log("\n=== Owner Balances ===");
  console.log(`Address: ${owner.account.address}`);

  const ownerEth = await publicClient.getBalance({
    address: owner.account.address,
  });
  console.log(`ETH: ${formatEther(ownerEth)} ETH`);

  const ownerTokens = await casinoToken.read.balanceOf([
    owner.account.address,
  ]) as bigint;
  console.log(`Tokens: ${formatEther(ownerTokens)} CST`);

  const ownerVaultBalance = await casinoVault.read.getBalance([
    owner.account.address,
  ]) as bigint;
  console.log(`Vault Balance: ${formatEther(ownerVaultBalance)} CST`);

  console.log("\n=== User 1 Balances ===");
  console.log(`Address: ${user1.account.address}`);

  const user1Eth = await publicClient.getBalance({
    address: user1.account.address,
  });
  console.log(`ETH: ${formatEther(user1Eth)} ETH`);

  const user1Tokens = await casinoToken.read.balanceOf([
    user1.account.address,
  ]) as bigint;
  console.log(`Tokens: ${formatEther(user1Tokens)} CST`);

  const user1VaultBalance = await casinoVault.read.getBalance([
    user1.account.address,
  ]) as bigint;
  console.log(`Vault Balance: ${formatEther(user1VaultBalance)} CST`);

  console.log("\n=== User 2 Balances ===");
  console.log(`Address: ${user2.account.address}`);

  const user2Eth = await publicClient.getBalance({
    address: user2.account.address,
  });
  console.log(`ETH: ${formatEther(user2Eth)} ETH`);

  const user2Tokens = await casinoToken.read.balanceOf([
    user2.account.address,
  ]) as bigint;
  console.log(`Tokens: ${formatEther(user2Tokens)} CST`);

  const user2VaultBalance = await casinoVault.read.getBalance([
    user2.account.address,
  ]) as bigint;
  console.log(`Vault Balance: ${formatEther(user2VaultBalance)} CST`);

  console.log("\n=== Summary ===");
  const totalVaultBalances = ownerVaultBalance + user1VaultBalance + user2VaultBalance;
  const totalInVault = houseBalance + totalVaultBalances;
  console.log(`Total user vault balances: ${formatEther(totalVaultBalances)} CST`);
  console.log(`Total in vault (house + users): ${formatEther(totalInVault)} CST`);
  console.log(`Actual vault token balance: ${formatEther(vaultTokenBalance)} CST`);

  if (totalInVault === vaultTokenBalance) {
    console.log("Balance check: OK (accounting matches actual balance)");
  } else {
    console.log("Balance check: MISMATCH!");
    console.log(`Difference: ${formatEther(vaultTokenBalance - totalInVault)} CST`);
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
