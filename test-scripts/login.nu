source setup/config.nu

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
print "Logged in successfully"
