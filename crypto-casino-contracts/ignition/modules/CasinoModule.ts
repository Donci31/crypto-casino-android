import { buildModule } from "@nomicfoundation/ignition-core";

const CasinoModule = buildModule("CasinoModule", (m) => {
  // Deploy CasinoToken
  const casinoToken = m.contract("CasinoToken");

  // Deploy CasinoWallet with the address of the CasinoToken
  const casinoVault = m.contract("CasinoVault", [casinoToken]);

  const slotMachine = m.contract("SlotMachine", [casinoVault, 10, 1000, 1]);

  return { casinoToken, casinoVault, slotMachine };
});

export default CasinoModule;
