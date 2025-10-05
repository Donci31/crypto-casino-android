import { buildModule } from "@nomicfoundation/ignition-core";
import { ethers } from "ethers";


const CasinoModule = buildModule("CasinoModule", (m) => {
  const casinoToken = m.contract("CasinoToken");

  const casinoVault = m.contract("CasinoVault", [casinoToken]);

  const minBetInTokenUnits = ethers.parseUnits("10", 18);  // 10 tokens
  const maxBetInTokenUnits = ethers.parseUnits("1000", 18); // 1000 tokens
  const houseEdgePercentage = 100; // 1% (in basis points, where 10000 = 100%)

  const slotMachine = m.contract("SlotMachine", [
    casinoVault, 
    minBetInTokenUnits, 
    maxBetInTokenUnits, 
    houseEdgePercentage
  ]);

  const authorizeGame = m.call(casinoVault, "authorizeGame", [slotMachine]);

  return { casinoToken, casinoVault, slotMachine };
});

export default CasinoModule;
