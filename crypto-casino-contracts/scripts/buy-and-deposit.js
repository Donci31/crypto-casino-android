// scripts/buy-and-deposit.js
const { ethers } = require("hardhat");

async function main() {
  console.log("Starting the casino token purchase and deposit scenario...");

  // Get signers
  const [owner, user] = await ethers.getSigners();
  console.log(`Owner address: ${owner.address}`);
  console.log(`User address: ${user.address}`);

  // Contract addresses
  const casinoTokenAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const casinoVaultAddress = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";

  // Connect to existing contracts
  const casinoToken = await ethers.getContractAt("CasinoToken", casinoTokenAddress);
  const casinoVault = await ethers.getContractAt("CasinoVault", casinoVaultAddress);

  console.log(`Connected to CasinoToken at: ${casinoToken.target}`);
  console.log(`Connected to CasinoVault at: ${casinoVault.target}`);

  // Check initial balances
  const initialUserETHBalance = await ethers.provider.getBalance(user.address);
  const initialUserTokenBalance = await casinoToken.balanceOf(user.address);
  const initialUserVaultBalance = await casinoVault.getBalance(user.address);

  console.log(`Initial user ETH balance: ${ethers.formatEther(initialUserETHBalance)} ETH`);
  console.log(`Initial user token balance: ${ethers.formatEther(initialUserTokenBalance)} CST`);
  console.log(`Initial user vault balance: ${ethers.formatEther(initialUserVaultBalance)} CST`);

  // Step 1: User purchases tokens by sending ETH
  const ethToPurchase = ethers.parseEther("1.0"); // Purchase 1 ETH worth of tokens
  console.log(`\nStep 1: Purchasing tokens with ${ethers.formatEther(ethToPurchase)} ETH...`);
  
  const purchaseTx = await casinoToken.connect(user).purchaseTokens({
    value: ethToPurchase
  });
  
  await purchaseTx.wait();
  console.log(`Transaction hash: ${purchaseTx.hash}`);

  // Get the token rate
  const rate = await casinoToken.rate();
  const expectedTokenAmount = ethToPurchase * rate;
  console.log(`Expected tokens received: ${ethers.formatEther(expectedTokenAmount)} CST (rate: ${rate})`);

  // Check updated token balance
  const userTokenBalance = await casinoToken.balanceOf(user.address);
  console.log(`Updated user token balance: ${ethers.formatEther(userTokenBalance)} CST`);

  // Step 2: User approves vault to spend tokens
  const approveAmount = userTokenBalance;
  console.log(`\nStep 2: Approving vault to spend ${ethers.formatEther(approveAmount)} tokens...`);
  
  const approveTx = await casinoToken.connect(user).approve(casinoVaultAddress, approveAmount);
  await approveTx.wait();
  console.log(`Transaction hash: ${approveTx.hash}`);

  // Check allowance
  const allowance = await casinoToken.allowance(user.address, casinoVaultAddress);
  console.log(`Allowance for vault: ${ethers.formatEther(allowance)} CST`);

  // Step 3: User deposits tokens into the vault
  const depositAmount = ethers.parseEther("500"); // Deposit 500 tokens
  console.log(`\nStep 3: Depositing ${ethers.formatEther(depositAmount)} tokens into the vault...`);
  
  const depositTx = await casinoVault.connect(user).deposit(depositAmount);
  await depositTx.wait();
  console.log(`Transaction hash: ${depositTx.hash}`);

  // Check final balances
  const finalUserTokenBalance = await casinoToken.balanceOf(user.address);
  const finalUserVaultBalance = await casinoVault.getBalance(user.address);

  console.log(`\nFinal Balances:`);
  console.log(`User token balance: ${ethers.formatEther(finalUserTokenBalance)} CST`);
  console.log(`User vault balance: ${ethers.formatEther(finalUserVaultBalance)} CST`);

  console.log("\nScenario completed successfully!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
