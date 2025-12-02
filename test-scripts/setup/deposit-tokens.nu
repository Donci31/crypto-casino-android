source config.nu

print "Step 1: Purchasing tokens with ETH..."
print "Buying 1000 CST (costs 1 ETH)\n"

let purchase_tx = (
    cast send $env.CASINO_TOKEN
    "purchaseTokens()"
    --value "1ether"
    --private-key $env.PRIVATE_KEY
    --rpc-url $env.RPC_URL
    --json
    | from json
)

print $"Tokens purchased!"
print $"TX Hash: ($purchase_tx.transactionHash)\n"

print "Step 2: Checking token balance..."
let balance_hex = (
    cast call $env.CASINO_TOKEN
    "balanceOf(address)"
    $env.WALLET_ADDRESS
    --rpc-url $env.RPC_URL
)

let balance = ($balance_hex | into int)
let balance_tokens = ($balance / 1000000000000000000)

print $"Token Balance: ($balance_tokens) CST\n"

print "Step 3: Approving CasinoVault to spend tokens..."
print "Approving 500 CST for deposit\n"

let approve_amount = "500000000000000000000"

let approve_tx = (
    cast send $env.CASINO_TOKEN
    "approve(address,uint256)"
    $env.CASINO_VAULT
    $approve_amount
    --private-key $env.PRIVATE_KEY
    --rpc-url $env.RPC_URL
    --json
    | from json
)

print $"Approval granted!"
print $"TX Hash: ($approve_tx.transactionHash)\n"

print "Step 4: Depositing tokens to CasinoVault..."
let deposit_tx = (
    cast send $env.CASINO_VAULT
    "deposit(uint256)"
    $approve_amount
    --private-key $env.PRIVATE_KEY
    --rpc-url $env.RPC_URL
    --json
    | from json
)

print $"Tokens deposited to vault!"
print $"TX Hash: ($deposit_tx.transactionHash)\n"

print "Step 5: Checking vault balance..."
let vault_balance_hex = (
    cast call $env.CASINO_VAULT
    "getBalance(address)"
    $env.WALLET_ADDRESS
    --rpc-url $env.RPC_URL
)

let vault_balance = ($vault_balance_hex | into int)
let vault_balance_tokens = ($vault_balance / 1000000000000000000)

print $"Vault Balance: ($vault_balance_tokens) CST\n"
print $"Wallet Balance: ($balance_tokens) CST"
