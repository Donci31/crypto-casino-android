# Test JWT token refresh
source ../setup/config.nu

print "=== Testing Token Refresh ===\n"

let username = "testuser"
let password = "TestP@ss123!"

print "Step 1: Login to get tokens"
let login_response = (
    http post $"($env.BASE_URL)/api/auth/login" {
        usernameOrEmail: $username,
        password: $password
    } --content-type application/json
)

let original_token = $login_response.token
let refresh_token = $login_response.refreshToken

print $"Original Token: ($original_token | str substring 0..20)..."
print $"Refresh Token: ($refresh_token | str substring 0..20)...\n"

print "Step 2: Refresh the token"
try {
    let refresh_response = (
        http post $"($env.BASE_URL)/api/auth/refresh" ''
        --headers [Authorization $"Bearer ($refresh_token)"]
    )

    let new_token = $refresh_response.token
    let new_refresh_token = $refresh_response.refreshToken

    if ($new_token | is-empty) {
        print "❌ FAIL: No new JWT token returned"
        exit 1
    }

    if ($new_refresh_token | is-empty) {
        print "❌ FAIL: No new refresh token returned"
        exit 1
    }

    print $"✅ Token refresh successful!"
    print $"New Token: ($new_token | str substring 0..20)..."
    print $"New Refresh Token: ($new_refresh_token | str substring 0..20)...\n"

    # Verify tokens are different
    if ($new_token == $original_token) {
        print "⚠️  WARNING: New token is same as original"
    } else {
        print "✅ PASS: New token is different from original"
    }

    if ($new_refresh_token == $refresh_token) {
        print "⚠️  WARNING: New refresh token is same as original"
    } else {
        print "✅ PASS: New refresh token is different from original\n"
    }

    # Test refresh with invalid token
    print "Step 3: Test refresh with invalid token (should fail)"
    try {
        http post $"($env.BASE_URL)/api/auth/refresh" ''
        --headers [Authorization $"Bearer invalid_token_123"]

        print "❌ FAIL: Refresh with invalid token should have failed"
        exit 1
    } catch {
        print "✅ PASS: Invalid token correctly rejected\n"
    }

    print "✅ All token refresh tests passed!"
} catch {|e|
    print $"❌ FAIL: Token refresh test failed - ($e)"
    exit 1
}
