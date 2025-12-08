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

    const seedAmount = parseEther("1000000");
    await casinoToken.write.approve([casinoVault.address, seedAmount], {
      account: owner.account,
    });
    await casinoVault.write.seedHouseBalance([seedAmount], {
      account: owner.account,
    });

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

  // ============================================
  // 1. CORE FUNCTIONALITY TESTS
  // ============================================
  describe("1. Core Functionality", () => {
    describe("1.1 Deployment", () => {
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

    describe("1.2 Game Creation", () => {
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

      it("Should reject bet above maxBet", async () => {
        const { dice, casinoToken, casinoVault, owner, player1, maxBet } =
          await deployDiceFixture();

        const betAmount = maxBet + parseEther("1");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should reject insufficient balance", async () => {
        const { dice, owner, player1 } = await deployDiceFixture();

        const betAmount = parseEther("100");
        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should handle zero address player rejection", async () => {
        const { dice, owner } = await deployDiceFixture();

        const betAmount = parseEther("100");
        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [
              "0x0000000000000000000000000000000000000000",
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

    describe("1.3 Game Settlement", () => {
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

      it("Should reject double settlement of same game", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeed = keccak256(toBytes("seed"));
        const serverSeedHash = keccak256(
          encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
        );
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
          { account: owner.account }
        );

        await dice.write.settleGame([1n, serverSeed], { account: owner.account });

        await assert.rejects(
          dice.write.settleGame([1n, serverSeed], { account: owner.account })
        );
      });

      it("Should reject settlement of non-existent game", async () => {
        const { dice, owner } = await deployDiceFixture();
        const serverSeed = keccak256(toBytes("seed"));

        await assert.rejects(
          dice.write.settleGame([999n, serverSeed], { account: owner.account })
        );
      });
    });

    describe("1.4 Multiple Games", () => {
      it("Should track playerLastGame correctly across multiple games", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount * 3n);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
          { account: owner.account }
        );

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 60, 1, clientSeed],
          { account: owner.account }
        );

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 42, 2, clientSeed],
          { account: owner.account }
        );

        const lastGameId = await dice.read.playerLastGame([player1.account.address]);
        assert.strictEqual(lastGameId, 3n);

        const lastGame = (await dice.read.getPlayerLastGame([
          player1.account.address,
        ])) as DiceGame;
        assert.strictEqual(lastGame.prediction, 42);
        assert.strictEqual(lastGame.betType, 2);
      });

      it("Should handle multiple players independently", async () => {
        const { dice, casinoToken, casinoVault, owner, player1, player2 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);
        await setupPlayerWithTokens(casinoToken, casinoVault, player2, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
          { account: owner.account }
        );

        await dice.write.createGame(
          [player2.account.address, serverSeedHash, betAmount, 70, 1, clientSeed],
          { account: owner.account }
        );

        const player1LastGameId = await dice.read.playerLastGame([
          player1.account.address,
        ]);
        const player2LastGameId = await dice.read.playerLastGame([
          player2.account.address,
        ]);

        assert.strictEqual(player1LastGameId, 1n);
        assert.strictEqual(player2LastGameId, 2n);

        const player1Game = (await dice.read.getGame([
          player1LastGameId,
        ])) as DiceGame;
        const player2Game = (await dice.read.getGame([
          player2LastGameId,
        ])) as DiceGame;

        assert.strictEqual(player1Game.prediction, 50);
        assert.strictEqual(player2Game.prediction, 70);
      });
    });
  });

  // ============================================
  // 2. GAME LOGIC TESTS
  // ============================================
  describe("2. Game Logic", () => {
    describe("2.1 Bet Types", () => {
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

    describe("2.2 Bet Type Validations", () => {
      it("Should reject invalid ROLL_UNDER prediction (0)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 0, 0, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should reject invalid ROLL_UNDER prediction (1)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 1, 0, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should reject invalid ROLL_UNDER prediction (100)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 100, 0, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should reject invalid ROLL_OVER prediction (1)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 1, 1, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should reject invalid ROLL_OVER prediction (100)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 100, 1, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should allow valid prediction boundaries for ROLL_UNDER (2 and 99)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount * 2n);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 2, 0, clientSeed],
          { account: owner.account }
        );

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 99, 0, clientSeed],
          { account: owner.account }
        );

        const game1 = (await dice.read.getGame([1n])) as DiceGame;
        const game2 = (await dice.read.getGame([2n])) as DiceGame;

        assert.strictEqual(game1.prediction, 2);
        assert.strictEqual(game2.prediction, 99);
      });

      it("Should allow valid prediction boundaries for ROLL_OVER (2 and 99)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount * 2n);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 2, 1, clientSeed],
          { account: owner.account }
        );

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 99, 1, clientSeed],
          { account: owner.account }
        );

        const game1 = (await dice.read.getGame([1n])) as DiceGame;
        const game2 = (await dice.read.getGame([2n])) as DiceGame;

        assert.strictEqual(game1.prediction, 2);
        assert.strictEqual(game2.prediction, 99);
      });

      it("Should allow all valid predictions for EXACT (1-100)", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount * 3n);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 1, 2, clientSeed],
          { account: owner.account }
        );

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 2, clientSeed],
          { account: owner.account }
        );

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 100, 2, clientSeed],
          { account: owner.account }
        );

        const game1 = (await dice.read.getGame([1n])) as DiceGame;
        const game2 = (await dice.read.getGame([2n])) as DiceGame;
        const game3 = (await dice.read.getGame([3n])) as DiceGame;

        assert.strictEqual(game1.prediction, 1);
        assert.strictEqual(game2.prediction, 50);
        assert.strictEqual(game3.prediction, 100);
      });
    });

    describe("2.3 Payout Calculations", () => {
      it("Should calculate correct payout for ROLL_UNDER", async () => {
        const { dice } = await deployDiceFixture();
        const betAmount = parseEther("100");

        const payout = await dice.read.calculatePayout([betAmount, 50, 0]);
        const expectedGrossPayout = (betAmount * 100n) / 49n;
        const adjustment = (expectedGrossPayout * 100n) / 10000n;
        const expectedNetPayout = expectedGrossPayout - adjustment;

        assert.strictEqual(payout, expectedNetPayout);
      });

      it("Should calculate correct payout for ROLL_OVER", async () => {
        const { dice } = await deployDiceFixture();
        const betAmount = parseEther("100");

        const payout = await dice.read.calculatePayout([betAmount, 50, 1]);
        const expectedGrossPayout = (betAmount * 100n) / 50n;
        const adjustment = (expectedGrossPayout * 100n) / 10000n;
        const expectedNetPayout = expectedGrossPayout - adjustment;

        assert.strictEqual(payout, expectedNetPayout);
      });

      it("Should calculate correct payout for EXACT", async () => {
        const { dice } = await deployDiceFixture();
        const betAmount = parseEther("100");

        const payout = await dice.read.calculatePayout([betAmount, 42, 2]);
        const expectedGrossPayout = betAmount * 100n;
        const adjustment = (expectedGrossPayout * 100n) / 10000n;
        const expectedNetPayout = expectedGrossPayout - adjustment;

        assert.strictEqual(payout, expectedNetPayout);
      });

      it("Should apply house edge correctly with different house edge values", async () => {
        const { dice, owner } = await deployDiceFixture();
        const betAmount = parseEther("100");

        const payoutBefore = await dice.read.calculatePayout([betAmount, 50, 0]);

        await dice.write.updateGameConfig([parseEther("10"), parseEther("1000"), 500n], {
          account: owner.account,
        });

        const payoutAfter = await dice.read.calculatePayout([betAmount, 50, 0]);

        assert.ok(payoutAfter < payoutBefore);
      });
    });
  });

  // ============================================
  // 3. FAIRNESS & RANDOMNESS TESTS
  // ============================================
  describe("3. Commit-Reveal and Fairness", () => {
    describe("3.1 Fairness Verification", () => {
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

      it("Should fail fairness verification with wrong seed", async () => {
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

        await dice.write.settleGame([1n, serverSeed], { account: owner.account });

        const wrongSeed = keccak256(toBytes("wrong_seed"));
        const verifyResult = (await dice.read.verifyFairness([
          1n,
          wrongSeed,
        ])) as [boolean, number];
        const [isValid, result] = verifyResult;

        assert.strictEqual(isValid, false);
        assert.strictEqual(result, 0);
      });

      it("Should reject fairness verification before game is settled", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeed = keccak256(toBytes("seed"));
        const serverSeedHash = keccak256(
          encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
        );
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
          { account: owner.account }
        );

        await assert.rejects(dice.read.verifyFairness([1n, serverSeed]));
      });
    });

    describe("3.2 Randomness Properties", () => {
      it("Should generate result within 1-100 range", async () => {
        const { dice, player1 } = await deployDiceFixture();

        const serverSeed = keccak256(toBytes("test_seed"));
        const clientSeed = keccak256(toBytes("client_seed"));

        for (let i = 0; i < 100; i++) {
          const result = await dice.read.calculateResult([
            serverSeed,
            clientSeed,
            player1.account.address,
            BigInt(i),
          ]);
          assert.ok(result >= 1 && result <= 100);
        }
      });

      it("Should generate different results for different seeds", async () => {
        const { dice, player1 } = await deployDiceFixture();

        const serverSeed1 = keccak256(toBytes("seed1"));
        const serverSeed2 = keccak256(toBytes("seed2"));
        const clientSeed = keccak256(toBytes("client"));

        const result1 = await dice.read.calculateResult([
          serverSeed1,
          clientSeed,
          player1.account.address,
          1n,
        ]);
        const result2 = await dice.read.calculateResult([
          serverSeed2,
          clientSeed,
          player1.account.address,
          1n,
        ]);

        assert.notStrictEqual(result1, result2);
      });

      it("Should generate same result for same inputs (deterministic)", async () => {
        const { dice, player1 } = await deployDiceFixture();

        const serverSeed = keccak256(toBytes("consistent_seed"));
        const clientSeed = keccak256(toBytes("client_seed"));

        const result1 = await dice.read.calculateResult([
          serverSeed,
          clientSeed,
          player1.account.address,
          1n,
        ]);
        const result2 = await dice.read.calculateResult([
          serverSeed,
          clientSeed,
          player1.account.address,
          1n,
        ]);

        assert.strictEqual(result1, result2);
      });
    });
  });

  // ============================================
  // 4. SECURITY & EDGE CASES
  // ============================================
  describe("4. Security and Edge Cases", () => {
    describe("4.1 Pause Functionality", () => {
      it("Should allow owner to pause and unpause", async () => {
        const { dice, owner } = await deployDiceFixture();

        await dice.write.pause([], { account: owner.account });
        assert.strictEqual(await dice.read.paused(), true);

        await dice.write.unpause([], { account: owner.account });
        assert.strictEqual(await dice.read.paused(), false);
      });

      it("Should prevent game creation when paused", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        await dice.write.pause([], { account: owner.account });

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        await assert.rejects(
          dice.write.createGame(
            [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
            { account: owner.account }
          )
        );
      });

      it("Should prevent game settlement when paused", async () => {
        const { dice, casinoToken, casinoVault, owner, player1 } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeed = keccak256(toBytes("seed"));
        const serverSeedHash = keccak256(
          encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
        );
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
          { account: owner.account }
        );

        await dice.write.pause([], { account: owner.account });

        await assert.rejects(
          dice.write.settleGame([1n, serverSeed], { account: owner.account })
        );
      });
    });

    describe("4.2 Configuration Validation", () => {
      it("Should reject invalid minBet (0)", async () => {
        const { dice, owner } = await deployDiceFixture();

        await assert.rejects(
          dice.write.updateGameConfig([0n, parseEther("1000"), 100n], {
            account: owner.account,
          })
        );
      });

      it("Should reject maxBet less than or equal to minBet", async () => {
        const { dice, owner } = await deployDiceFixture();

        await assert.rejects(
          dice.write.updateGameConfig([parseEther("100"), parseEther("100"), 100n], {
            account: owner.account,
          })
        );

        await assert.rejects(
          dice.write.updateGameConfig([parseEther("100"), parseEther("50"), 100n], {
            account: owner.account,
          })
        );
      });

      it("Should reject house edge above 20%", async () => {
        const { dice, owner } = await deployDiceFixture();

        await assert.rejects(
          dice.write.updateGameConfig([parseEther("10"), parseEther("1000"), 2001n], {
            account: owner.account,
          })
        );
      });
    });
  });

  // ============================================
  // 5. ADMIN & CONFIGURATION
  // ============================================
  describe("5. Admin and Configuration", () => {
    describe("5.1 Configuration Updates", () => {
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

    describe("5.2 Gas Cost Analysis", () => {
      it("Should measure gas cost for game creation", async () => {
        const { dice, casinoToken, casinoVault, owner, player1, publicClient } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeedHash = keccak256(toBytes("hash"));
        const clientSeed = keccak256(toBytes("client"));

        const txHash = await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
          { account: owner.account }
        );

        const receipt = await publicClient.getTransactionReceipt({ hash: txHash });
        console.log(`Gas used for createGame: ${receipt.gasUsed}`);

        assert.ok(receipt.gasUsed > 0n);
      });

      it("Should measure gas cost for game settlement", async () => {
        const { dice, casinoToken, casinoVault, owner, player1, publicClient } =
          await deployDiceFixture();

        const betAmount = parseEther("100");
        await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

        const serverSeed = keccak256(toBytes("seed"));
        const serverSeedHash = keccak256(
          encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
        );
        const clientSeed = keccak256(toBytes("client"));

        await dice.write.createGame(
          [player1.account.address, serverSeedHash, betAmount, 50, 0, clientSeed],
          { account: owner.account }
        );

        const txHash = await dice.write.settleGame([1n, serverSeed], { account: owner.account });

        const receipt = await publicClient.getTransactionReceipt({ hash: txHash });
        console.log(`Gas used for settleGame: ${receipt.gasUsed}`);

        assert.ok(receipt.gasUsed > 0n);
      });
    });
  });
});
