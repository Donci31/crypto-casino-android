# Test depositing tokens to CasinoVault
source ../setup/config.nu

print "=== Testing Vault Deposit ===\n"

let deposit_amount_wei = "500000000000000000000"  # 500 CST
let deposit_amount_cst = 500

print "Step 1: Checking initial vault balance..."
let initial_vault_balance_hex = (
    cast call $env.CASINO_VAULT
    "getBalance(address)"
    $env.WALLET_ADDRESS
    --rpc-url $env.RPC_URL
)
let initial_vault_tokens = (($initial_vault_balance_hex | into int) / 1000000000000000000)
print $"Initial Vault Balance: ($initial_vault_tokens) CST\n"

print "Step 2: Checking wallet token balance..."
let wallet_balance_hex = (
    cast call $env.CASINO_TOKEN
    "balanceOf(address)"
    $env.WALLET_ADDRESS
    --rpc-url $env.RPC_URL
)
let wallet_tokens = (($wallet_balance_hex | into int) / 1000000000000000000)
print $"Wallet Token Balance: ($wallet_tokens) CST\n"

if ($wallet_tokens < $deposit_amount_cst) {
    print $"❌ FAIL: Insufficient tokens in wallet (need ($deposit_amount_cst) CST)"
    exit 1
}

print $"Step 3: Approving CasinoVault to spend ($deposit_amount_cst) CST..."
try {
    let approve_tx = (
        cast send $env.CASINO_TOKEN
        "approve(address,uint256)"
        $env.CASINO_VAULT
        $deposit_amount_wei
        --private-key $env.PRIVATE_KEY
        --rpc-url $env.RPC_URL
        --json
        | from json
    )

    print $"✅ Approval granted!"
    print $"TX Hash: ($approve_tx.transactionHash)\n"

    print $"Step 4: Depositing ($deposit_amount_cst) CST to vault..."
    let deposit_tx = (
        cast send $env.CASINO_VAULT
        "deposit(uint256)"
        $deposit_amount_wei
        --private-key $env.PRIVATE_KEY
        --rpc-url $env.RPC_URL
        --json
        | from json
    )

    print $"✅ Tokens deposited successfully!"
    print $"TX Hash: ($deposit_tx.transactionHash)"
    print $"Block Number: ($deposit_tx.blockNumber)\n"

    print "Step 5: Verifying new vault balance..."
    let new_vault_balance_hex = (
        cast call $env.CASINO_VAULT
        "getBalance(address)"
        $env.WALLET_ADDRESS
        --rpc-url $env.RPC_URL
    )
    let new_vault_tokens = (($new_vault_balance_hex | into int) / 1000000000000000000)
    let tokens_deposited = ($new_vault_tokens - $initial_vault_tokens)

    print $"New Vault Balance: ($new_vault_tokens) CST"
    print $"Tokens Deposited: ($tokens_deposited) CST\n"

    if ($tokens_deposited == $deposit_amount_cst) {
        print $"✅ PASS: Correct amount deposited (($deposit_amount_cst) CST)"
    } else {
        print $"❌ FAIL: Expected ($deposit_amount_cst) CST, got ($tokens_deposited) CST"
        exit 1
    }

    print "\n✅ All vault deposit tests passed!"
} catch {|e|
    print $"❌ FAIL: Vault deposit test failed - ($e)"
    exit 1
}
