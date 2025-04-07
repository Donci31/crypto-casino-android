import { buildModule } from "@nomicfoundation/ignition-core";

const CasinoModule = buildModule("CasinoModule", (m) => {
  // Deploy CasinoToken
  const casinoToken = m.contract("CasinoToken");

  // Deploy CasinoWallet with the address of the CasinoToken
  const casinoWallet = m.contract("CasinoWallet", [casinoToken]);

  return { casinoToken, casinoWallet };
});

export default CasinoModule;
