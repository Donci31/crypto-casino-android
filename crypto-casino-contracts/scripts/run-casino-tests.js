import ethers from "hardhat";

async function main() {
  console.log("Starting Casino System test script...");

  const [owner, player1, player2, gameAddress] = await ethers.getSigners();

  console.log(`Owner address: ${owner.address}`);
  console.log(`Player1 address: ${player1.address}`);
  console.log(`Game address: ${gameAddress.address}`);

  const CasinoToken = await ethers.getContractFactory("CasinoToken");
  const casinoToken = await CasinoToken.deploy();
  await casinoToken.waitForDeployment();
  console.log(`CasinoToken deployed to: ${await casinoToken.getAddress()}`);

  const CasinoWallet = await ethers.getContractFactory("CasinoWallet");
  const casinoWallet = await CasinoWallet.deploy(
    await casinoToken.getAddress()
  );
  await casinoWallet.waitForDeployment();
  console.log(`CasinoWallet deployed to: ${await casinoWallet.getAddress()}`);

  const printEvents = async (tx) => {
    const receipt = await tx.wait();
    console.log("\nEvents emitted:");
    for (const event of receipt.logs) {
      try {
        let parsedLog;
        try {
          parsedLog = casinoToken.interface.parseLog({
            topics: event.topics,
            data: event.data,
          });
        } catch (e) {
          parsedLog = casinoWallet.interface.parseLog({
            topics: event.topics,
            data: event.data,
          });
        }

        console.log(`Event: ${parsedLog.name}`);
        for (const [key, value] of Object.entries(parsedLog.args)) {
          if (isNaN(key)) {
            if (typeof value === "bigint") {
              console.log(`  ${key}: ${ethers.formatUnits(value, 18)}`);
            } else {
              console.log(`  ${key}: ${value.toString()}`);
            }
          }
        }
      } catch (error) {
        console.log(`Event: Unable to parse - ${event.topics[0]}`);
      }
    }
    console.log("");
  };

  console.log("\n=== Authorizing Game ===");
  const authTx = await casinoWallet.authorizeGame(gameAddress.address);
  await printEvents(authTx);

  const isAuthorized = await casinoWallet.authorizedGames(gameAddress.address);
  console.log(`Game authorized: ${isAuthorized}`);

  console.log("\n=== Player1 Purchases Tokens ===");
  const purchaseTx = await casinoToken.connect(player1).purchaseTokens({
    value: ethers.parseEther("1.0"),
  });
  await printEvents(purchaseTx);

  const player1TokenBalance = await casinoToken.balanceOf(player1.address);
  console.log(
    `Player1's token balance: ${ethers.formatUnits(
      player1TokenBalance,
      18
    )} CST`
  );

  const approveTx = await casinoToken
    .connect(player1)
    .approve(await casinoWallet.getAddress(), player1TokenBalance);
  await approveTx.wait();
  console.log("Player1 approved CasinoWallet to spend tokens");

  console.log("\n=== Player1 Deposits Tokens ===");
  const depositAmount = ethers.parseUnits("500", 18);
  const depositTx = await casinoWallet.connect(player1).deposit(depositAmount);
  await printEvents(depositTx);

  const walletBalance = await casinoWallet.getBalance(player1.address);
  console.log(
    `Player1's wallet balance: ${ethers.formatUnits(walletBalance, 18)} CST`
  );

  console.log("\n=== Player1 Places a Bet ===");
  const betAmount = ethers.parseUnits("100", 18);
  const betTx = await casinoWallet
    .connect(gameAddress)
    .placeBet(player1.address, betAmount);
  await printEvents(betTx);

  console.log("\n=== Player1 Wins ===");
  const winAmount = ethers.parseUnits("250", 18);
  const winTx = await casinoWallet
    .connect(gameAddress)
    .payWinnings(player1.address, winAmount);
  await printEvents(winTx);

  console.log("\n=== Player1 Withdraws Tokens ===");
  const withdrawAmount = ethers.parseUnits("300", 18);
  const withdrawTx = await casinoWallet
    .connect(player1)
    .withdraw(withdrawAmount);
  await printEvents(withdrawTx);

  console.log("\n=== Player1 Exchanges Tokens for ETH ===");
  const exchangeAmount = ethers.parseUnits("200", 18);
  const exchangeTx = await casinoToken
    .connect(player1)
    .exchangeTokens(exchangeAmount);
  await printEvents(exchangeTx);

  console.log("\n=== Owner Withdraws ETH ===");
  const ownerWithdrawTx = await casinoToken.withdrawEth();
  await printEvents(ownerWithdrawTx);

  console.log("\n=== Final Balances ===");

  const finalWalletBalance = await casinoWallet.getBalance(player1.address);
  console.log(
    `Player1's final wallet balance: ${ethers.formatUnits(
      finalWalletBalance,
      18
    )} CST`
  );

  const finalTokenBalance = await casinoToken.balanceOf(player1.address);
  console.log(
    `Player1's final token balance: ${ethers.formatUnits(
      finalTokenBalance,
      18
    )} CST`
  );

  console.log("\nTest script completed!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
