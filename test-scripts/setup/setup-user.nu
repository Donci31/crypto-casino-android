source config.nu

print "Registering new user..."
let register_response = (
    http post $"($env.BASE_URL)/api/auth/register"
    '{"username": "testuser", "email": "test@example.com", "password": "TestP@ss123!", "walletAddress": "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"}'
    --content-type application/json
)

if ($register_response | is-empty) {
    print "Registration failed!"
    exit 1
}

print $"User registered: ($register_response.user)\n"

print "Logging in..."
let login_response = (
    http post $"($env.BASE_URL)/api/auth/login"
    '{"usernameOrEmail": "testuser", "password": "TestP@ss123!"}'
    --content-type application/json
)

let token = $login_response.token

if ($token | is-empty) {
    print "Login failed!"
    exit 1
}

$env.CASINO_TOKEN = $token
print $"Logged in\n"

print "Adding wallet..."
let wallet_response = (
    http post $"($env.BASE_URL)/api/wallets"
    '{"address": "0x70997970C51812dc3A010C7d01b50e0d17dc79C8", "label": "My Test Wallet"}'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

let wallet_id = $wallet_response.id
print $"Wallet added! ID: ($wallet_id)"
print $"Address: ($wallet_response.address)\n"

print "Checking balance..."
let balance_response = (
    http get $"($env.BASE_URL)/api/wallets/($wallet_id)/balance"
    --headers [Authorization $"Bearer ($token)"]
)

print $"Token Balance: ($balance_response.balance) CST"
print $"Address: ($balance_response.address)\n"

