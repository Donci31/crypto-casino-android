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

enum BetType {
  STRAIGHT = 0,
  RED = 1,
  BLACK = 2,
  ODD = 3,
  EVEN = 4,
  LOW = 5,
  HIGH = 6,
  DOZEN_FIRST = 7,
  DOZEN_SECOND = 8,
  DOZEN_THIRD = 9,
  COLUMN_FIRST = 10,
  COLUMN_SECOND = 11,
  COLUMN_THIRD = 12,
}

type Bet = {
  betType: number;
  amount: bigint;
  number: number;
};

describe("Roulette Contract", () => {
  async function deployRouletteFixture() {
    const { viem } = await network.connect();
    const [owner, player1, player2] = await viem.getWalletClients();

    const casinoToken = await viem.deployContract("CasinoToken", []);
    const casinoVault = await viem.deployContract("CasinoVault", [
      casinoToken.address,
    ]);

    const minBet = parseEther("10");
    const maxBet = parseEther("1000");
    const houseEdge = 100n;

    const roulette = await viem.deployContract("Roulette", [
      casinoVault.address,
      minBet,
      maxBet,
      houseEdge,
    ]);

    await casinoVault.write.authorizeGame([roulette.address]);

    const publicClient = await viem.getPublicClient();

    return {
      casinoToken,
      casinoVault,
      roulette,
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
      const { roulette, casinoVault, minBet, maxBet, houseEdge } =
        await deployRouletteFixture();

      const vaultAddress = (await roulette.read.vault()) as `0x${string}`;
      assert.strictEqual(
        vaultAddress.toLowerCase(),
        casinoVault.address.toLowerCase()
      );
      assert.strictEqual(await roulette.read.minBet(), minBet);
      assert.strictEqual(await roulette.read.maxBet(), maxBet);
      assert.strictEqual(await roulette.read.houseEdge(), houseEdge);
    });

    it("Should reject invalid vault address", async () => {
      const { viem } = await network.connect();
      const minBet = parseEther("10");
      const maxBet = parseEther("1000");
      const houseEdge = 100n;

      await assert.rejects(
        viem.deployContract("Roulette", [
          "0x0000000000000000000000000000000000000000",
          minBet,
          maxBet,
          houseEdge,
        ])
      );
    });

    it("Should reject invalid minBet (0)", async () => {
      const { viem } = await network.connect();
      const casinoToken = await viem.deployContract("CasinoToken", []);
      const casinoVault = await viem.deployContract("CasinoVault", [
        casinoToken.address,
      ]);

      await assert.rejects(
        viem.deployContract("Roulette", [casinoVault.address, 0n, parseEther("1000"), 100n])
      );
    });

    it("Should reject maxBet not greater than minBet", async () => {
      const { viem } = await network.connect();
      const casinoToken = await viem.deployContract("CasinoToken", []);
      const casinoVault = await viem.deployContract("CasinoVault", [
        casinoToken.address,
      ]);

      await assert.rejects(
        viem.deployContract("Roulette", [
          casinoVault.address,
          parseEther("100"),
          parseEther("100"),
          100n,
        ])
      );
    });

    it("Should reject house edge above 20%", async () => {
      const { viem } = await network.connect();
      const casinoToken = await viem.deployContract("CasinoToken", []);
      const casinoVault = await viem.deployContract("CasinoVault", [
        casinoToken.address,
      ]);

      await assert.rejects(
        viem.deployContract("Roulette", [
          casinoVault.address,
          parseEther("10"),
          parseEther("1000"),
          2001n,
        ])
      );
    });
  });

  describe("Game Creation", () => {
    it("Should create a game with single bet", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("random_server_seed_123"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client_seed_456"));

      const bets: Bet[] = [
        { betType: BetType.RED, amount: betAmount, number: 0 },
      ];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const [player] = (await roulette.read.getGame([1n])) as any;
      assert.strictEqual(player.toLowerCase(), player1.account.address.toLowerCase());
    });

    it("Should create a game with multiple bets", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const totalAmount = parseEther("300");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, totalAmount);

      const serverSeed = keccak256(toBytes("seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [
        { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 7 },
        { betType: BetType.RED, amount: parseEther("100"), number: 0 },
        { betType: BetType.ODD, amount: parseEther("100"), number: 0 },
      ];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const gameBets = (await roulette.read.getGameBets([1n])) as Bet[];
      assert.strictEqual(gameBets.length, 3);
    });

    it("Should reject game with no bets", async () => {
      const { roulette, owner, player1 } = await deployRouletteFixture();

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [];

      await assert.rejects(
        roulette.write.createGame(
          [player1.account.address, serverSeedHash, bets, clientSeed],
          { account: owner.account }
        )
      );
    });

    it("Should reject game with too many bets (>20)", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const totalAmount = parseEther("300");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, totalAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = Array.from({ length: 21 }, () => ({
        betType: BetType.RED,
        amount: parseEther("10"),
        number: 0,
      }));

      await assert.rejects(
        roulette.write.createGame(
          [player1.account.address, serverSeedHash, bets, clientSeed],
          { account: owner.account }
        )
      );
    });

    it("Should reject bet below minBet", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("5");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await assert.rejects(
        roulette.write.createGame(
          [player1.account.address, serverSeedHash, bets, clientSeed],
          { account: owner.account }
        )
      );
    });

    it("Should reject bet above maxBet", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1, maxBet } =
        await deployRouletteFixture();

      const betAmount = maxBet + parseEther("1");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await assert.rejects(
        roulette.write.createGame(
          [player1.account.address, serverSeedHash, bets, clientSeed],
          { account: owner.account }
        )
      );
    });

    it("Should reject insufficient balance", async () => {
      const { roulette, owner, player1 } = await deployRouletteFixture();

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [
        { betType: BetType.RED, amount: parseEther("100"), number: 0 },
      ];

      await assert.rejects(
        roulette.write.createGame(
          [player1.account.address, serverSeedHash, bets, clientSeed],
          { account: owner.account }
        )
      );
    });

    it("Should reject invalid STRAIGHT number (>36)", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [
        { betType: BetType.STRAIGHT, amount: betAmount, number: 37 },
      ];

      await assert.rejects(
        roulette.write.createGame(
          [player1.account.address, serverSeedHash, bets, clientSeed],
          { account: owner.account }
        )
      );
    });
  });

  describe("Game Settlement", () => {
    it("Should settle game correctly", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("winning_seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client_seed"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const winningNumber = await roulette.read.calculateWinningNumber([
        serverSeed,
        clientSeed,
        player1.account.address,
        1n,
      ]);

      await roulette.write.settleGame([1n, serverSeed], { account: owner.account });

      const game = (await roulette.read.getGame([1n])) as any;
      assert.strictEqual(game[7], true);
      assert.strictEqual(game[5], winningNumber);
    });

    it("Should reject settlement with wrong server seed", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("correct_seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const wrongSeed = keccak256(toBytes("wrong_seed"));

      await assert.rejects(
        roulette.write.settleGame([1n, wrongSeed], { account: owner.account })
      );
    });

    it("Should reject double settlement", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await roulette.write.settleGame([1n, serverSeed], { account: owner.account });

      await assert.rejects(
        roulette.write.settleGame([1n, serverSeed], { account: owner.account })
      );
    });

    it("Should reject settlement of non-existent game", async () => {
      const { roulette, owner } = await deployRouletteFixture();
      const serverSeed = keccak256(toBytes("seed"));

      await assert.rejects(
        roulette.write.settleGame([999n, serverSeed], { account: owner.account })
      );
    });
  });

  describe("Bet Type Validation - STRAIGHT", () => {
    it("Should win on correct STRAIGHT number", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 7 };
      const winningNumber = 7;

      const won = await roulette.read.checkBetWin([bet, winningNumber]);
      assert.strictEqual(won, true);
    });

    it("Should lose on incorrect STRAIGHT number", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 7 };
      const winningNumber = 8;

      const won = await roulette.read.checkBetWin([bet, winningNumber]);
      assert.strictEqual(won, false);
    });

    it("Should allow STRAIGHT bet on 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 0 };
      const winningNumber = 0;

      const won = await roulette.read.checkBetWin([bet, winningNumber]);
      assert.strictEqual(won, true);
    });
  });

  describe("Bet Type Validation - RED/BLACK", () => {
    it("Should win on RED for red numbers", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.RED, amount: parseEther("100"), number: 0 };

      const redNumbers = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36];

      for (const num of redNumbers) {
        const won = await roulette.read.checkBetWin([bet, num]);
        assert.strictEqual(won, true, `Should win on RED for number ${num}`);
      }
    });

    it("Should lose on RED for black numbers and 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.RED, amount: parseEther("100"), number: 0 };

      const blackAndZero = [0, 2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35];

      for (const num of blackAndZero) {
        const won = await roulette.read.checkBetWin([bet, num]);
        assert.strictEqual(won, false, `Should lose on RED for number ${num}`);
      }
    });

    it("Should win on BLACK for black numbers", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.BLACK, amount: parseEther("100"), number: 0 };

      const blackNumbers = [2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35];

      for (const num of blackNumbers) {
        const won = await roulette.read.checkBetWin([bet, num]);
        assert.strictEqual(won, true, `Should win on BLACK for number ${num}`);
      }
    });

    it("Should lose on BLACK for 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.BLACK, amount: parseEther("100"), number: 0 };
      const won = await roulette.read.checkBetWin([bet, 0]);
      assert.strictEqual(won, false);
    });
  });

  describe("Bet Type Validation - ODD/EVEN", () => {
    it("Should win on ODD for odd numbers", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.ODD, amount: parseEther("100"), number: 0 };

      for (let i = 1; i <= 35; i += 2) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, true, `Should win on ODD for number ${i}`);
      }
    });

    it("Should lose on ODD for 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.ODD, amount: parseEther("100"), number: 0 };
      const won = await roulette.read.checkBetWin([bet, 0]);
      assert.strictEqual(won, false);
    });

    it("Should win on EVEN for even numbers (not 0)", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.EVEN, amount: parseEther("100"), number: 0 };

      for (let i = 2; i <= 36; i += 2) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, true, `Should win on EVEN for number ${i}`);
      }
    });

    it("Should lose on EVEN for 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.EVEN, amount: parseEther("100"), number: 0 };
      const won = await roulette.read.checkBetWin([bet, 0]);
      assert.strictEqual(won, false);
    });
  });

  describe("Bet Type Validation - LOW/HIGH", () => {
    it("Should win on LOW for 1-18", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.LOW, amount: parseEther("100"), number: 0 };

      for (let i = 1; i <= 18; i++) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, true, `Should win on LOW for number ${i}`);
      }
    });

    it("Should lose on LOW for 19-36 and 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.LOW, amount: parseEther("100"), number: 0 };

      const won0 = await roulette.read.checkBetWin([bet, 0]);
      assert.strictEqual(won0, false);

      for (let i = 19; i <= 36; i++) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, false, `Should lose on LOW for number ${i}`);
      }
    });

    it("Should win on HIGH for 19-36", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.HIGH, amount: parseEther("100"), number: 0 };

      for (let i = 19; i <= 36; i++) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, true, `Should win on HIGH for number ${i}`);
      }
    });

    it("Should lose on HIGH for 1-18 and 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.HIGH, amount: parseEther("100"), number: 0 };

      const won0 = await roulette.read.checkBetWin([bet, 0]);
      assert.strictEqual(won0, false);

      for (let i = 1; i <= 18; i++) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, false, `Should lose on HIGH for number ${i}`);
      }
    });
  });

  describe("Bet Type Validation - DOZENS", () => {
    it("Should win on DOZEN_FIRST for 1-12", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.DOZEN_FIRST, amount: parseEther("100"), number: 0 };

      for (let i = 1; i <= 12; i++) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, true, `Should win on DOZEN_FIRST for number ${i}`);
      }
    });

    it("Should win on DOZEN_SECOND for 13-24", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.DOZEN_SECOND, amount: parseEther("100"), number: 0 };

      for (let i = 13; i <= 24; i++) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, true, `Should win on DOZEN_SECOND for number ${i}`);
      }
    });

    it("Should win on DOZEN_THIRD for 25-36", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.DOZEN_THIRD, amount: parseEther("100"), number: 0 };

      for (let i = 25; i <= 36; i++) {
        const won = await roulette.read.checkBetWin([bet, i]);
        assert.strictEqual(won, true, `Should win on DOZEN_THIRD for number ${i}`);
      }
    });

    it("Should lose on DOZENS for 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet1: Bet = { betType: BetType.DOZEN_FIRST, amount: parseEther("100"), number: 0 };
      const bet2: Bet = { betType: BetType.DOZEN_SECOND, amount: parseEther("100"), number: 0 };
      const bet3: Bet = { betType: BetType.DOZEN_THIRD, amount: parseEther("100"), number: 0 };

      assert.strictEqual(await roulette.read.checkBetWin([bet1, 0]), false);
      assert.strictEqual(await roulette.read.checkBetWin([bet2, 0]), false);
      assert.strictEqual(await roulette.read.checkBetWin([bet3, 0]), false);
    });
  });

  describe("Bet Type Validation - COLUMNS", () => {
    it("Should win on COLUMN_FIRST for numbers where n % 3 == 1", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.COLUMN_FIRST, amount: parseEther("100"), number: 0 };

      const columnFirst = [1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34];

      for (const num of columnFirst) {
        const won = await roulette.read.checkBetWin([bet, num]);
        assert.strictEqual(won, true, `Should win on COLUMN_FIRST for number ${num}`);
      }
    });

    it("Should win on COLUMN_SECOND for numbers where n % 3 == 2", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.COLUMN_SECOND, amount: parseEther("100"), number: 0 };

      const columnSecond = [2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35];

      for (const num of columnSecond) {
        const won = await roulette.read.checkBetWin([bet, num]);
        assert.strictEqual(won, true, `Should win on COLUMN_SECOND for number ${num}`);
      }
    });

    it("Should win on COLUMN_THIRD for numbers where n % 3 == 0 and n != 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet: Bet = { betType: BetType.COLUMN_THIRD, amount: parseEther("100"), number: 0 };

      const columnThird = [3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36];

      for (const num of columnThird) {
        const won = await roulette.read.checkBetWin([bet, num]);
        assert.strictEqual(won, true, `Should win on COLUMN_THIRD for number ${num}`);
      }
    });

    it("Should lose on COLUMNS for 0", async () => {
      const { roulette } = await deployRouletteFixture();

      const bet1: Bet = { betType: BetType.COLUMN_FIRST, amount: parseEther("100"), number: 0 };
      const bet2: Bet = { betType: BetType.COLUMN_SECOND, amount: parseEther("100"), number: 0 };
      const bet3: Bet = { betType: BetType.COLUMN_THIRD, amount: parseEther("100"), number: 0 };

      assert.strictEqual(await roulette.read.checkBetWin([bet1, 0]), false);
      assert.strictEqual(await roulette.read.checkBetWin([bet2, 0]), false);
      assert.strictEqual(await roulette.read.checkBetWin([bet3, 0]), false);
    });
  });

  describe("Payout Calculations", () => {
    it("Should return correct multiplier for STRAIGHT (36x)", async () => {
      const { roulette } = await deployRouletteFixture();
      const multiplier = await roulette.read.getPayoutMultiplier([BetType.STRAIGHT]);
      assert.strictEqual(multiplier, 36n);
    });

    it("Should return correct multiplier for RED/BLACK/ODD/EVEN/LOW/HIGH (2x)", async () => {
      const { roulette } = await deployRouletteFixture();

      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.RED]), 2n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.BLACK]), 2n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.ODD]), 2n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.EVEN]), 2n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.LOW]), 2n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.HIGH]), 2n);
    });

    it("Should return correct multiplier for DOZENS/COLUMNS (3x)", async () => {
      const { roulette } = await deployRouletteFixture();

      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.DOZEN_FIRST]), 3n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.DOZEN_SECOND]), 3n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.DOZEN_THIRD]), 3n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.COLUMN_FIRST]), 3n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.COLUMN_SECOND]), 3n);
      assert.strictEqual(await roulette.read.getPayoutMultiplier([BetType.COLUMN_THIRD]), 3n);
    });
  });

  describe("Multiple Bets Payout", () => {
    it("Should pay correctly for multiple winning bets", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const totalAmount = parseEther("300");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, totalAmount);

      const initialBalance = await casinoVault.read.getBalance([player1.account.address]);

      const serverSeed = keccak256(toBytes("seed_for_number_7"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const winningNumber = await roulette.read.calculateWinningNumber([
        serverSeed,
        clientSeed,
        player1.account.address,
        1n,
      ]);

      const bets: Bet[] = [
        { betType: BetType.STRAIGHT, amount: parseEther("100"), number: Number(winningNumber) },
        { betType: BetType.RED, amount: parseEther("100"), number: 0 },
        { betType: BetType.ODD, amount: parseEther("100"), number: 0 },
      ];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await roulette.write.settleGame([1n, serverSeed], { account: owner.account });

      const game = (await roulette.read.getGame([1n])) as any;
      const totalPayout = game[6];

      const finalBalance = await casinoVault.read.getBalance([player1.account.address]);

      assert.ok(totalPayout > 0n);
      assert.ok(finalBalance > initialBalance - totalAmount);
    });

    it("Should handle all bets losing correctly", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const totalAmount = parseEther("200");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, totalAmount);

      const initialBalance = await casinoVault.read.getBalance([player1.account.address]);

      const serverSeed = keccak256(toBytes("seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [
        { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 36 },
        { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 35 },
      ];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await roulette.write.settleGame([1n, serverSeed], { account: owner.account });

      const game = (await roulette.read.getGame([1n])) as any;
      const winningNumber = game[5];

      if (winningNumber !== 36 && winningNumber !== 35) {
        const totalPayout = game[6];
        const finalBalance = await casinoVault.read.getBalance([player1.account.address]);

        assert.strictEqual(totalPayout, 0n);
        assert.strictEqual(finalBalance, initialBalance - totalAmount);
      }
    });
  });

  describe("Fairness Verification", () => {
    it("Should verify game fairness correctly", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("verify_seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await roulette.write.settleGame([1n, serverSeed], { account: owner.account });

      const verifyResult = (await roulette.read.verifyFairness([
        1n,
        serverSeed,
      ])) as [boolean, number];
      const [isValid, winningNumber] = verifyResult;

      assert.strictEqual(isValid, true);

      const game = (await roulette.read.getGame([1n])) as any;
      assert.strictEqual(winningNumber, game[5]);
    });

    it("Should fail verification with wrong seed", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("correct_seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await roulette.write.settleGame([1n, serverSeed], { account: owner.account });

      const wrongSeed = keccak256(toBytes("wrong_seed"));
      const verifyResult = (await roulette.read.verifyFairness([
        1n,
        wrongSeed,
      ])) as [boolean, number];
      const [isValid] = verifyResult;

      assert.strictEqual(isValid, false);
    });

    it("Should reject verification before game is settled", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await assert.rejects(roulette.read.verifyFairness([1n, serverSeed]));
    });
  });

  describe("Configuration Updates", () => {
    it("Should allow owner to update game config", async () => {
      const { roulette, owner } = await deployRouletteFixture();

      const newMinBet = parseEther("20");
      const newMaxBet = parseEther("2000");
      const newHouseEdge = 200n;

      await roulette.write.updateGameConfig([newMinBet, newMaxBet, newHouseEdge], {
        account: owner.account,
      });

      assert.strictEqual(await roulette.read.minBet(), newMinBet);
      assert.strictEqual(await roulette.read.maxBet(), newMaxBet);
      assert.strictEqual(await roulette.read.houseEdge(), newHouseEdge);
    });

    it("Should reject invalid config updates", async () => {
      const { roulette, owner } = await deployRouletteFixture();

      await assert.rejects(
        roulette.write.updateGameConfig([0n, parseEther("1000"), 100n], {
          account: owner.account,
        })
      );

      await assert.rejects(
        roulette.write.updateGameConfig([parseEther("100"), parseEther("50"), 100n], {
          account: owner.account,
        })
      );

      await assert.rejects(
        roulette.write.updateGameConfig([parseEther("10"), parseEther("1000"), 2001n], {
          account: owner.account,
        })
      );
    });
  });

  describe("Pause Functionality", () => {
    it("Should allow owner to pause and unpause", async () => {
      const { roulette, owner } = await deployRouletteFixture();

      await roulette.write.pause([], { account: owner.account });
      assert.strictEqual(await roulette.read.paused(), true);

      await roulette.write.unpause([], { account: owner.account });
      assert.strictEqual(await roulette.read.paused(), false);
    });

    it("Should prevent game creation when paused", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      await roulette.write.pause([], { account: owner.account });

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await assert.rejects(
        roulette.write.createGame(
          [player1.account.address, serverSeedHash, bets, clientSeed],
          { account: owner.account }
        )
      );
    });

    it("Should prevent game settlement when paused", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeed = keccak256(toBytes("seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await roulette.write.pause([], { account: owner.account });

      await assert.rejects(
        roulette.write.settleGame([1n, serverSeed], { account: owner.account })
      );
    });
  });

  describe("Randomness and Determinism", () => {
    it("Should generate winning number within 0-36 range", async () => {
      const { roulette, player1 } = await deployRouletteFixture();

      const serverSeed = keccak256(toBytes("test_seed"));
      const clientSeed = keccak256(toBytes("client_seed"));

      for (let i = 0; i < 100; i++) {
        const result = await roulette.read.calculateWinningNumber([
          serverSeed,
          clientSeed,
          player1.account.address,
          BigInt(i),
        ]);
        assert.ok(result >= 0 && result <= 36);
      }
    });

    it("Should generate different results for different seeds", async () => {
      const { roulette, player1 } = await deployRouletteFixture();

      const serverSeed1 = keccak256(toBytes("seed1"));
      const serverSeed2 = keccak256(toBytes("seed2"));
      const clientSeed = keccak256(toBytes("client"));

      const result1 = await roulette.read.calculateWinningNumber([
        serverSeed1,
        clientSeed,
        player1.account.address,
        1n,
      ]);
      const result2 = await roulette.read.calculateWinningNumber([
        serverSeed2,
        clientSeed,
        player1.account.address,
        1n,
      ]);

      assert.notStrictEqual(result1, result2);
    });

    it("Should generate same result for same inputs (deterministic)", async () => {
      const { roulette, player1 } = await deployRouletteFixture();

      const serverSeed = keccak256(toBytes("consistent_seed"));
      const clientSeed = keccak256(toBytes("client_seed"));

      const result1 = await roulette.read.calculateWinningNumber([
        serverSeed,
        clientSeed,
        player1.account.address,
        1n,
      ]);
      const result2 = await roulette.read.calculateWinningNumber([
        serverSeed,
        clientSeed,
        player1.account.address,
        1n,
      ]);

      assert.strictEqual(result1, result2);
    });
  });

  describe("Multiple Players", () => {
    it("Should handle multiple players independently", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1, player2 } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);
      await setupPlayerWithTokens(casinoToken, casinoVault, player2, betAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      await roulette.write.createGame(
        [player2.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const player1LastGameId = await roulette.read.playerLastGame([
        player1.account.address,
      ]);
      const player2LastGameId = await roulette.read.playerLastGame([
        player2.account.address,
      ]);

      assert.strictEqual(player1LastGameId, 1n);
      assert.strictEqual(player2LastGameId, 2n);
    });
  });

  describe("Gas Cost Analysis", () => {
    it("Should measure gas cost for game creation with single bet", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1, publicClient } =
        await deployRouletteFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [{ betType: BetType.RED, amount: betAmount, number: 0 }];

      const txHash = await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const receipt = await publicClient.getTransactionReceipt({ hash: txHash });
      console.log(`Gas used for createGame (single bet): ${receipt.gasUsed}`);

      assert.ok(receipt.gasUsed > 0n);
    });

    it("Should measure gas cost for game creation with multiple bets", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1, publicClient } =
        await deployRouletteFixture();

      const totalAmount = parseEther("500");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, totalAmount);

      const serverSeedHash = keccak256(toBytes("hash"));
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [
        { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 7 },
        { betType: BetType.RED, amount: parseEther("100"), number: 0 },
        { betType: BetType.ODD, amount: parseEther("100"), number: 0 },
        { betType: BetType.DOZEN_FIRST, amount: parseEther("100"), number: 0 },
        { betType: BetType.COLUMN_FIRST, amount: parseEther("100"), number: 0 },
      ];

      const txHash = await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const receipt = await publicClient.getTransactionReceipt({ hash: txHash });
      console.log(`Gas used for createGame (5 bets): ${receipt.gasUsed}`);

      assert.ok(receipt.gasUsed > 0n);
    });

    it("Should measure gas cost for game settlement", async () => {
      const { roulette, casinoToken, casinoVault, owner, player1, publicClient } =
        await deployRouletteFixture();

      const totalAmount = parseEther("300");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, totalAmount);

      const serverSeed = keccak256(toBytes("seed"));
      const serverSeedHash = keccak256(
        encodeAbiParameters(parseAbiParameters("bytes32"), [serverSeed])
      );
      const clientSeed = keccak256(toBytes("client"));

      const bets: Bet[] = [
        { betType: BetType.STRAIGHT, amount: parseEther("100"), number: 7 },
        { betType: BetType.RED, amount: parseEther("100"), number: 0 },
        { betType: BetType.ODD, amount: parseEther("100"), number: 0 },
      ];

      await roulette.write.createGame(
        [player1.account.address, serverSeedHash, bets, clientSeed],
        { account: owner.account }
      );

      const txHash = await roulette.write.settleGame([1n, serverSeed], { account: owner.account });

      const receipt = await publicClient.getTransactionReceipt({ hash: txHash });
      console.log(`Gas used for settleGame (3 bets): ${receipt.gasUsed}`);

      assert.ok(receipt.gasUsed > 0n);
    });
  });
});
