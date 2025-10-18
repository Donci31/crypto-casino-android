import { describe, it } from "node:test";
import assert from "node:assert";
import { network } from "hardhat";
import { parseEther } from "viem";

type Spin = {
  bet: bigint;
  reels: readonly [bigint, bigint, bigint];
  winAmount: bigint;
  resolved: boolean;
};

describe("SlotMachine Contract", () => {
  async function deploySlotMachineFixture() {
    const { viem } = await network.connect();
    const [owner, player1, player2] = await viem.getWalletClients();

    const casinoToken = await viem.deployContract("CasinoToken", []);
    const casinoVault = await viem.deployContract("CasinoVault", [
      casinoToken.address,
    ]);

    const minBet = parseEther("10");
    const maxBet = parseEther("1000");
    const houseEdge = 100n;

    const slotMachine = await viem.deployContract("SlotMachine", [
      casinoVault.address,
      minBet,
      maxBet,
      houseEdge,
    ]);

    await casinoVault.write.authorizeGame([slotMachine.address]);

    const publicClient = await viem.getPublicClient();

    return {
      casinoToken,
      casinoVault,
      slotMachine,
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
      const { slotMachine, casinoVault, minBet, maxBet, houseEdge } =
        await deploySlotMachineFixture();

      const vaultAddress = (await slotMachine.read.vault()) as `0x${string}`;
      assert.strictEqual(vaultAddress.toLowerCase(), casinoVault.address.toLowerCase());
      assert.strictEqual(await slotMachine.read.minBet(), minBet);
      assert.strictEqual(await slotMachine.read.maxBet(), maxBet);
      assert.strictEqual(await slotMachine.read.houseEdge(), houseEdge);
    });
  });

  describe("Spinning", () => {
    it("Should allow player to spin with valid bet", async () => {
      const { slotMachine, casinoToken, casinoVault, owner, player1 } =
        await deploySlotMachineFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      await slotMachine.write.spinForPlayer(
        [player1.account.address, betAmount],
        { account: owner.account }
      );

      const spin = (await slotMachine.read.getSpinState([1n])) as Spin;

      assert.strictEqual(spin.bet, betAmount);
      assert.strictEqual(spin.resolved, true);
      assert.strictEqual(spin.reels.length, 3);
    });

    it("Should reject bet below minBet", async () => {
      const { slotMachine, casinoToken, casinoVault, owner, player1 } =
        await deploySlotMachineFixture();

      const betAmount = parseEther("5");
      await setupPlayerWithTokens(casinoToken, casinoVault, player1, betAmount);

      await assert.rejects(
        slotMachine.write.spinForPlayer([player1.account.address, betAmount], {
          account: owner.account,
        })
      );
    });

    it("Should generate reels with values 0-9", async () => {
      const { slotMachine, casinoToken, casinoVault, owner, player1 } =
        await deploySlotMachineFixture();

      const betAmount = parseEther("100");
      await setupPlayerWithTokens(
        casinoToken,
        casinoVault,
        player1,
        betAmount * 5n
      );

      for (let i = 0; i < 5; i++) {
        await slotMachine.write.spinForPlayer(
          [player1.account.address, betAmount],
          {
            account: owner.account,
          }
        );

        const spin = (await slotMachine.read.getSpinState([BigInt(i + 1)])) as Spin;

        assert.ok(spin.reels[0] < 10n);
        assert.ok(spin.reels[1] < 10n);
        assert.ok(spin.reels[2] < 10n);
      }
    });
  });

  describe("Configuration Updates", () => {
    it("Should allow owner to update game config", async () => {
      const { slotMachine, owner } = await deploySlotMachineFixture();

      const newMinBet = parseEther("20");
      const newMaxBet = parseEther("2000");
      const newHouseEdge = 200n;

      await slotMachine.write.updateGameConfig(
        [newMinBet, newMaxBet, newHouseEdge],
        {
          account: owner.account,
        }
      );

      assert.strictEqual(await slotMachine.read.minBet(), newMinBet);
      assert.strictEqual(await slotMachine.read.maxBet(), newMaxBet);
      assert.strictEqual(await slotMachine.read.houseEdge(), newHouseEdge);
    });
  });

  describe("Pause Functionality", () => {
    it("Should allow owner to pause and unpause", async () => {
      const { slotMachine, owner } = await deploySlotMachineFixture();

      await slotMachine.write.pause([], { account: owner.account });
      assert.strictEqual(await slotMachine.read.paused(), true);

      await slotMachine.write.unpause([], { account: owner.account });
      assert.strictEqual(await slotMachine.read.paused(), false);
    });
  });
});
