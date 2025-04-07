import { expect } from "chai";
import { ethers } from "hardhat";
import { CasinoToken, CasinoWallet } from "../typechain-types";
import { HardhatEthersSigner } from "@nomicfoundation/hardhat-ethers/signers";

describe("Casino Contracts", function () {
  let casinoToken: CasinoToken;
  let casinoWallet: CasinoWallet;
  let owner: HardhatEthersSigner;
  let user: HardhatEthersSigner;

  beforeEach(async function () {
    // Get signers
    [owner, user] = await ethers.getSigners();

    // Deploy CasinoToken
    const CasinoTokenFactory = await ethers.getContractFactory("CasinoToken");
    casinoToken = await CasinoTokenFactory.deploy();
    
    // Deploy CasinoWallet with CasinoToken address
    const CasinoWalletFactory = await ethers.getContractFactory("CasinoWallet");
    casinoWallet = await CasinoWalletFactory.deploy(await casinoToken.getAddress());
  });

  describe("CasinoToken", function () {
    it("Should mint initial supply to owner", async function () {
      const initialSupply = 1000000n * 10n ** 18n; // 1 million tokens with 18 decimals
      expect(await casinoToken.balanceOf(owner.address)).to.equal(initialSupply);
    });

    it("Should allow users to purchase tokens", async function () {
      const ethAmount = ethers.parseEther("1"); // 1 ETH
      const tokenAmount = ethAmount * 100n; // 100 tokens per 1 ETH
      
      await casinoToken.connect(user).purchaseTokens({ value: ethAmount });
      
      expect(await casinoToken.balanceOf(user.address)).to.equal(tokenAmount);
    });
  });

  describe("CasinoWallet", function () {
    it("Should register a new user", async function () {
      await casinoWallet.connect(user).registerUser();
      
      expect(await casinoWallet.isRegistered(user.address)).to.be.true;
    });

    it("Should allow user to deposit tokens", async function () {
      // Purchase tokens first
      const ethAmount = ethers.parseEther("1");
      await casinoToken.connect(user).purchaseTokens({ value: ethAmount });
      
      // Register user
      await casinoWallet.connect(user).registerUser();
      
      // Approve tokens for wallet contract
      const depositAmount = ethers.parseEther("50");
      await casinoToken.connect(user).approve(await casinoWallet.getAddress(), depositAmount);
      
      // Deposit tokens
      await casinoWallet.connect(user).deposit(depositAmount);
      
      // Check balance
      expect(await casinoWallet.connect(user).getBalance()).to.equal(depositAmount);
    });
  });
});
