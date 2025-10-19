const RPC_URL = "http://localhost:8545"
const CASINO_TOKEN = "0x5FbDB2315678afecb367f032d93F642f64180aa3"
const CASINO_VAULT = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512"
const PRIVATE_KEY = "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d"
const WALLET_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"

print "💰 CryptoCasino - Token Purchase & Deposit\n"

print "Step 1: Purchasing tokens with ETH..."
print "   Buying 1000 CST (costs 1 ETH)\n"

let purchase_tx = (
    cast send $CASINO_TOKEN
    "purchaseTokens()"
    --value "1ether"
    --private-key $PRIVATE_KEY
    --rpc-url $RPC_URL
    --json
    | from json
)

print $"   ✅ Tokens purchased!"
print $"   TX Hash: ($purchase_tx.transactionHash)\n"

print "Step 2: Checking token balance..."
let balance_hex = (
    cast call $CASINO_TOKEN
    "balanceOf(address)"
    $WALLET_ADDRESS
    --rpc-url $RPC_URL
)

let balance = ($balance_hex | into int)
let balance_tokens = ($balance / 1000000000000000000)

print $"   💰 Token Balance: ($balance_tokens) CST\n"

print "Step 3: Approving CasinoVault to spend tokens..."
print "   Approving 500 CST for deposit\n"

let approve_amount = "500000000000000000000"

let approve_tx = (
    cast send $CASINO_TOKEN
    "approve(address,uint256)"
    $CASINO_VAULT
    $approve_amount
    --private-key $PRIVATE_KEY
    --rpc-url $RPC_URL
    --json
    | from json
)

print $"   ✅ Approval granted!"
print $"   TX Hash: ($approve_tx.transactionHash)\n"

print "Step 4: Depositing tokens to CasinoVault..."
let deposit_tx = (
    cast send $CASINO_VAULT
    "deposit(uint256)"
    $approve_amount
    --private-key $PRIVATE_KEY
    --rpc-url $RPC_URL
    --json
    | from json
)

print $"   ✅ Tokens deposited to vault!"
print $"   TX Hash: ($deposit_tx.transactionHash)\n"

print "Step 5: Checking vault balance..."
let vault_balance_hex = (
    cast call $CASINO_VAULT
    "getBalance(address)"
    $WALLET_ADDRESS
    --rpc-url $RPC_URL
)

let vault_balance = ($vault_balance_hex | into int)
let vault_balance_tokens = ($vault_balance / 1000000000000000000)

print $"   🏦 Vault Balance: ($vault_balance_tokens) CST\n"

print "✨ Setup complete! You can now play dice games."
print $"   💰 Wallet Balance: ($balance_tokens) CST"
print $"   🏦 Vault Balance: ($vault_balance_tokens) CST"
