import { HardhatUserConfig } from "hardhat/config";
import hardhatViem from "@nomicfoundation/hardhat-viem";
import hardhatIgnitionViem from "@nomicfoundation/hardhat-ignition-viem";

const config: HardhatUserConfig = {
  plugins: [hardhatViem, hardhatIgnitionViem],
  solidity: {
    version: "0.8.26",
    settings: {
      optimizer: {
        enabled: true,
        runs: 200,
      },
    },
  },
  networks: {
    localhost: {
      type: "http",
      url: "http://127.0.0.1:8545",
    },
  },
  paths: {
    sources: "./contracts",
    tests: "./contracts",
    cache: "./cache",
    artifacts: "./artifacts",
  },
};

export default config;
