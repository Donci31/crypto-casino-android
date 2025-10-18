import { describe, it } from "node:test";
import assert from "node:assert";
import { network } from "hardhat";
import {
  parseEther,
  keccak256,
  toBytes,
  encodeAbiParameters,
  parseAbiParameters,
} from "viem";

type DiceGame = {
  player: `0x${string}`;
  betAmount: bigint;
  prediction: number;
  betType: number;
  serverSeedHash: `0x${string}`;
  serverSeed: `0x${string}`;
  clientSeed: `0x${string}`;
  result: number;
  payout: bigint;
  settled: boolean;
  createdAt: bigint;
  settledAt: bigint;
};

describe("Dice Contract", () => {
  async function deployDiceFixture() {
    const { viem } = await network.connect();
    const [owner, player1, player2] = await viem.getWalletClients();

    const casinoToken = await viem.deployContract("CasinoToken", []);
    const casinoVault = await viem.deployContract("CasinoVault", [
      casinoToken.address,
    ]);

    const minBet = parseEther("10");
    const maxBet = parseEther("1000");
    const houseEdge = 100n;

    const dice = await viem.deployContract("Dice", [
      casinoVault.address,
      minBet,
      maxBet,
      houseEdge,
    ]);

    await casinoVault.write.authorizeGame([dice.address]);

    const publicClient = await viem.getPublicClient();

    return {
      casinoToken,
      casinoVault,
      dice,
      owner,
      player1,
      player2,
      publicClient,
      minBet,
      maxBet,
      houseEdge,
    };
  }

  async function setupPlayerWithTokens(
    casinoToken: any,
    casinoVault: any,
    player: any,
    amount: bigint
  ) {
    const ethAmount = amount / 1000n;
    await casinoToken.write.purchaseTokens({
      value: ethAmount,
      account: player.account,
    });
    await casinoToken.write.approve([casinoVault.address, amount], {
      account: player.account,
    });
    await casinoVault.write.deposit([amount], { account: player.account });
  }

  describe("Deployment", () => {
    it("Should deploy with correct initial values", async () => {
      const { dice, casinoVault, minBet, maxBet, houseEdge } =
        await deployDiceFixture();

      const vaultAddress = (await dice.read.vault()) as `0x${string}`;
      assert.strictEqual(
        vaultAddress.toLowerCase(),
        casinoVault.address.toLowerCase()
      );
      assert.strictEqual(await dice.read.minBet(), minBet);
      assert.strictEqual(await dice.read.maxBet(), maxBet);
      assert.strictEqual(await dice.read.houseEdge(), houseEdge);
    });

    it("Should reject invalid vault address", async () => {
      const { viem } = await network.connect();
      const minBet = parseEther("10");
      const maxBet = parseEther("1000");
      const houseEdge = 100n;

      await assert.rejects(
        viem.deployContract("Dice", [
          "0x0000000000000000000000000000000000000000",
          minBet,
          maxBet,
          houseEdge,
        ])
      );
    });
  });

  describe("Game Creation", () => {
    it("Should create a game with valid parameters", async () => {
      const { dice, casinoToken, casinoVault, owner, player1 } =
        await deployDiceFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("random_server_seed_123"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client_seed_456"));
      const prediction = 50;
      const betType = 0;

      await dice.write.createGame(
        [
          player1.account.address,
          serverSeedHash,
          betAmount,
          prediction,
          betType,
          clientSeed,
        ],
        { account: owner.account }
      );

      const game = (await dice.read.getGame([1n])) as DiceGame;

      assert.strictEqual(game.player.toLowerCase(), player1.account.address.toLowerCase());
      assert.strictEqual(game.betAmount, betAmount);
      assert.strictEqual(game.prediction, prediction);
      assert.strictEqual(game.settled, false);
    });

    it("Should reject bet below minBet", async () => {
      const { dice, casinoToken, casinoVault, owner, player1 } =
        await deployDiceFixture();

      const betAmount = parseEther("5");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      await assert.rejects(
        dice.write.createGame(
          [
            player1.account.address,
            serverSeedHash,
            betAmount,
            50,
            0,
            clientSeed,
          ],
          { account: owner.account }
        )
      );
    });
  });

  describe("Game Settlement", () => {
    it("Should settle game correctly", async () => {
      const { dice, casinoToken, casinoVault, owner, player1 } =
        await deployDiceFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("winning_seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client_seed"));

      await dice.write.createGame(
        [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
        { account: owner.account }
      );

      const result = await dice.read.calculateResult([
        serverSeed,
        clientSeed,
        player1.account.address,
        1n,
      ]);

      await dice.write.settleGame([1n, serverSeed], { account: owner.account });

      const game = (await dice.read.getGame([1n])) as DiceGame;
      assert.strictEqual(game.settled, true);
      assert.strictEqual(game.result, result);
      assert.strictEqual(game.serverSeed, serverSeed);
    });

    it("Should reject settlement with wrong server seed", async () => {
      const { dice, casinoToken, casinoVault, owner, player1 } =
        await deployDiceFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("correct_seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      await dice.write.createGame(
        [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
        { account: owner.account }
      );

      const wrongSeed = keccak256(toBytes("wrong_seed"));

      await assert.rejects(
        dice.write.settleGame([1n, wrongSeed], { account: owner.account })
      );
    });
  });

  describe("Bet Types", () => {
    it("Should correctly handle ROLL_UNDER wins and losses", async () => {
      const { dice } = await deployDiceFixture();

      const checkWin = await dice.read.checkWin([30, 50, 0]);
      assert.strictEqual(checkWin, true);

      const checkLoss = await dice.read.checkWin([70, 50, 0]);
      assert.strictEqual(checkLoss, false);
    });

    it("Should correctly handle ROLL_OVER wins and losses", async () => {
      const { dice } = await deployDiceFixture();

      const checkWin = await dice.read.checkWin([70, 50, 1]);
      assert.strictEqual(checkWin, true);

      const checkLoss = await dice.read.checkWin([30, 50, 1]);
      assert.strictEqual(checkLoss, false);
    });

    it("Should correctly handle EXACT wins and losses", async () => {
      const { dice } = await deployDiceFixture();

      const checkWin = await dice.read.checkWin([42, 42, 2]);
      assert.strictEqual(checkWin, true);

      const checkLoss = await dice.read.checkWin([41, 42, 2]);
      assert.strictEqual(checkLoss, false);
    });
  });

  describe("Fairness Verification", () => {
    it("Should verify game fairness correctly", async () => {
      const { dice, casinoToken, casinoVault, owner, player1 } =
        await deployDiceFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("verify_seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      await dice.write.createGame(
        [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
        { account: owner.account }
      );

      await dice.write.settleGame([1n, serverSeed], { account: owner.account });

      const verifyResult = (await dice.read.verifyFairness([
        1n,
        serverSeed,
      ])) as [boolean, number];
      const [isValid, result] = verifyResult;

      assert.strictEqual(isValid, true);

      const game = (await dice.read.getGame([1n])) as DiceGame;
      assert.strictEqual(result, game.result);
    });
  });

  describe("Configuration Updates", () => {
    it("Should allow owner to update game config", async () => {
      const { dice, owner } = await deployDiceFixture();

      const newMinBet = parseEther("20");
      const newMaxBet = parseEther("2000");
      const newHouseEdge = 200n;

      await dice.write.updateGameConfig([newMinBet, newMaxBet, newHouseEdge], {
        account: owner.account,
      });

      assert.strictEqual(await dice.read.minBet(), newMinBet);
      assert.strictEqual(await dice.read.maxBet(), newMaxBet);
      assert.strictEqual(await dice.read.houseEdge(), newHouseEdge);
    });
  });

  describe("Pause Functionality", () => {
    it("Should allow owner to pause and unpause", async () => {
      const { dice, owner } = await deployDiceFixture();

      await dice.write.pause([], { account: owner.account });
      assert.strictEqual(await dice.read.paused(), true);

      await dice.write.unpause([], { account: owner.account });
      assert.strictEqual(await dice.read.paused(), false);
    });
  });
});
