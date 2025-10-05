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

  const authorizeGame = m.call(casinoVault, "authorizeGame", [slotMachine]);

  return { casinoToken, casinoVault, slotMachine };
});

export default CasinoModule;
