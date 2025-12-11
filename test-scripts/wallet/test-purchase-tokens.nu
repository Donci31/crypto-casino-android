# Test purchasing tokens with ETH
source ../setup/config.nu

print "=== Testing Token Purchase ===\n"

print "Step 1: Checking initial ETH balance..."
let eth_balance_wei = (
    cast balance $env.WALLET_ADDRESS
    --rpc-url $env.RPC_URL
)
let eth_balance = ($eth_balance_wei | into int)
let eth_balance_ether = ($eth_balance / 1000000000000000000)
print $"ETH Balance: ($eth_balance_ether) ETH\n"

print "Step 2: Checking initial token balance..."
let initial_token_balance_hex = (
    cast call $env.CASINO_TOKEN
    "balanceOf(address)"
    $env.WALLET_ADDRESS
    --rpc-url $env.RPC_URL
)
let initial_tokens = (($initial_token_balance_hex | into int) / 1000000000000000000)
print $"Initial Token Balance: ($initial_tokens) CST\n"

print "Step 3: Purchasing tokens (1 ETH = 1000 CST)..."
try {
    let purchase_tx = (
        cast send $env.CASINO_TOKEN
        "purchaseTokens()"
        --value "1ether"
        --private-key $env.PRIVATE_KEY
        --rpc-url $env.RPC_URL
        --json
        | from json
    )

    print $"✅ Tokens purchased successfully!"
    print $"TX Hash: ($purchase_tx.transactionHash)"
    print $"Block Number: ($purchase_tx.blockNumber)\n"

    print "Step 4: Verifying new token balance..."
    let new_token_balance_hex = (
        cast call $env.CASINO_TOKEN
        "balanceOf(address)"
        $env.WALLET_ADDRESS
        --rpc-url $env.RPC_URL
    )
    let new_tokens = (($new_token_balance_hex | into int) / 1000000000000000000)
    let tokens_gained = ($new_tokens - $initial_tokens)

    print $"New Token Balance: ($new_tokens) CST"
    print $"Tokens Gained: ($tokens_gained) CST\n"

    if ($tokens_gained == 1000) {
        print "✅ PASS: Correct amount of tokens received (1000 CST)"
    } else {
        print $"❌ FAIL: Expected 1000 CST, got ($tokens_gained) CST"
        exit 1
    }

    print "\n✅ All token purchase tests passed!"
} catch {|e|
    print $"❌ FAIL: Token purchase test failed - ($e)"
    exit 1
}
