source setup/config.nu

let login_response = (
    http post $"($env.BASE_URL)/api/auth/login"
    '{"usernameOrEmail": "testuser", "password": "TestP@ss123!"}'
    --content-type application/json
)

let token = $login_response.token
let refreshToken = $login_response.refreshToken

if ($token | is-empty) {
    print "Login failed!"
    exit 1
}

$env.JWT_TOKEN = $token
$env.REFRESH_TOKEN = $refreshToken
print "Logged in successfully"
