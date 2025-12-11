# Test user login
source ../setup/config.nu

print "=== Testing User Login ===\n"

# Use default test user (should be created by setup-user.nu)
let username = "testuser"
let password = "TestP@ss123!"

print $"Attempting login with username: ($username)"

try {
    let response = (
        http post $"($env.BASE_URL)/api/auth/login" {
            usernameOrEmail: $username,
            password: $password
        } --content-type application/json
    )

    let token = $response.token
    let refreshToken = $response.refreshToken

    if ($token | is-empty) {
        print "❌ FAIL: No JWT token returned"
        exit 1
    }

    if ($refreshToken | is-empty) {
        print "❌ FAIL: No refresh token returned"
        exit 1
    }

    print $"✅ Login successful!"
    print $"Token (first 20 chars): ($token | str substring 0..20)..."
    print $"Refresh Token (first 20 chars): ($refreshToken | str substring 0..20)...\n"

    # Test invalid credentials
    print "Testing invalid credentials (should fail)..."
    try {
        http post $"($env.BASE_URL)/api/auth/login" {
            usernameOrEmail: $username,
            password: "WrongPassword123!"
        } --content-type application/json

        print "❌ FAIL: Login with invalid credentials should have failed"
        exit 1
    } catch {
        print "✅ PASS: Invalid credentials correctly rejected\n"
    }

    # Test with email instead of username
    print "Testing login with email..."
    let email_response = (
        http post $"($env.BASE_URL)/api/auth/login" {
            usernameOrEmail: "testuser@example.com",
            password: $password
        } --content-type application/json
    )

    if ($email_response.token | is-empty) {
        print "❌ FAIL: Login with email failed"
        exit 1
    }

    print "✅ PASS: Login with email successful\n"

    print "✅ All login tests passed!"
} catch {|e|
    print $"❌ FAIL: Login test failed - ($e)"
    exit 1
}
