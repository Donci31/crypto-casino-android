// balance-checker.js
const hre = require("hardhat");

async function main() {
  // Addresses to check
  const addresses = [
    "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
    "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
  ];
  
  // Get deployed contract addresses
  // You'll need to replace these with the actual deployed contract addresses
  const casinoTokenAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const casinoVaultAddress = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";
  
  if (!casinoTokenAddress || !casinoVaultAddress) {
    console.error("Please set CASINO_TOKEN_ADDRESS and CASINO_VAULT_ADDRESS environment variables");
    return;
  }
  
  console.log(`CasinoToken address: ${casinoTokenAddress}`);
  console.log(`CasinoVault address: ${casinoVaultAddress}`);
  
  // Get contract instances
  const CasinoToken = await hre.ethers.getContractAt("CasinoToken", casinoTokenAddress);
  const CasinoVault = await hre.ethers.getContractAt("CasinoVault", casinoVaultAddress);
  
  // Get token decimals
  const decimals = await CasinoToken.decimals();
  
  console.log("\n=== BALANCE CHECK RESULTS ===");
  
  // Check balances for each address
  for (const address of addresses) {
    console.log(`\nAddress: ${address}`);
    
    // Get ETH balance
    const ethBalance = await hre.ethers.provider.getBalance(address);
    console.log(`ETH Balance: ${ethBalance} ETH`);
    
    // Get token balance
    const tokenBalance = await CasinoToken.balanceOf(address);
    console.log(`CasinoToken Balance: ${tokenBalance} CST`);
    
    // Get vault balance
    const vaultBalance = await CasinoVault.getBalance(address);
    console.log(`CasinoVault Balance: ${vaultBalance} CST`);
  }
}

// Execute the script
main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
