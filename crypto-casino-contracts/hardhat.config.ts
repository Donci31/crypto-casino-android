import { HardhatUserConfig } from "hardhat/config";
import hardhatViem from "@nomicfoundation/hardhat-viem";
import hardhatIgnitionViem from "@nomicfoundation/hardhat-ignition-viem";
import hardhatNodeTestRunner from "@nomicfoundation/hardhat-node-test-runner";

const config: HardhatUserConfig = {
  plugins: [hardhatViem, hardhatIgnitionViem, hardhatNodeTestRunner],
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
    tests: {
      solidity: "./contracts",
      nodejs: "./test",
    },
    cache: "./cache",
    artifacts: "./artifacts",
  },
};

export default config;
