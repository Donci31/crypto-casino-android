import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";
import { parseUnits } from "viem";

const CasinoModule = buildModule("CasinoModule", (m) => {
  const casinoToken = m.contract("CasinoToken");

  const casinoVault = m.contract("CasinoVault", [casinoToken]);

  const minBetInTokenUnits = parseUnits("10", 18);
  const maxBetInTokenUnits = parseUnits("1000", 18);
  const houseEdgePercentage = 100;

  const slotMachine = m.contract("SlotMachine", [
    casinoVault,
    minBetInTokenUnits,
    maxBetInTokenUnits,
    houseEdgePercentage,
  ]);

  const diceGame = m.contract("DiceGame", [
    casinoVault,
    minBetInTokenUnits,
    maxBetInTokenUnits,
    houseEdgePercentage,
  ]);

  const roulette = m.contract("Roulette", [
    casinoVault,
    minBetInTokenUnits,
    maxBetInTokenUnits,
    houseEdgePercentage,
  ]);

  const authorizeSlotMachine = m.call(casinoVault, "authorizeGame", [slotMachine], { id: "AuthorizeSlotMachine" });
  const authorizeDiceGame = m.call(casinoVault, "authorizeGame", [diceGame], { id: "AuthorizeDiceGame" });
  const authorizeRoulette = m.call(casinoVault, "authorizeGame", [roulette], { id: "AuthorizeRoulette" });

  return { casinoToken, casinoVault, slotMachine, diceGame, roulette };
});

export default CasinoModule;
