const hre = require("hardhat"); // ← this is the key to accessing Hardhat's ethers
const { ethers } = hre;

async function main() {
  const tokenAddress = "0x5fbdb2315678afecb367f032d93f642f64180aa3";
  const userAddress = "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266";

  const CasinoToken = await ethers.getContractFactory("CasinoToken");
  const token = await CasinoToken.attach(tokenAddress);

  const tokenBalance = await token.balanceOf(userAddress);
  const ethBalance = await ethers.provider.getBalance(userAddress);

  const otherEthBalance = await ethers.provider.getBalance("0x70997970C51812dc3A010C7d01b50e0d17dc79C8");
  const otherTokenBalance = await token.balanceOf("0x70997970C51812dc3A010C7d01b50e0d17dc79C8");

  console.log(`🎰 Other Token Balance: ${otherTokenBalance} CST`)
  console.log(`🎰 Other ETH Balance: ${otherEthBalance} CST`)

  console.log(`🎰 Token Balance: ${tokenBalance} CST`);
  console.log(`💰 ETH Balance: ${ethBalance} ETH`);
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
