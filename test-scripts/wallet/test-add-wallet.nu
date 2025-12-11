# Test adding wallet to user account
source ../login.nu

print "=== Testing Add Wallet ===\n"

let token = $env.JWT_TOKEN
let wallet_address = $env.WALLET_ADDRESS

print $"Adding wallet: ($wallet_address)"

try {
    let response = (
        http post $"($env.BASE_URL)/api/wallets" {
            address: $wallet_address
        }
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )

    print $"✅ Wallet added successfully!"
    print $"Wallet ID: ($response.id)"
    print $"Address: ($response.address)"
    print $"Is Primary: ($response.isPrimary)\n"

    # List all wallets
    print "Fetching all wallets..."
    let wallets = (
        http get $"($env.BASE_URL)/api/wallets"
        --headers [Authorization $"Bearer ($token)"]
    )

    print $"Total wallets: ($wallets | length)"
    for wallet in $wallets {
        print $"  - ($wallet.address) (Primary: ($wallet.isPrimary))"
    }
    print ""

    # Try to add duplicate wallet (should fail)
    print "Testing duplicate wallet addition (should fail)..."
    try {
        http post $"($env.BASE_URL)/api/wallets" {
            address: $wallet_address
        }
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]

        print "❌ FAIL: Duplicate wallet should have been rejected"
        exit 1
    } catch {
        print "✅ PASS: Duplicate wallet correctly rejected\n"
    }

    print "✅ All wallet addition tests passed!"
} catch {|e|
    print $"❌ FAIL: Add wallet test failed - ($e)"
    exit 1
}
