# Test user registration
source ../setup/config.nu

print "=== Testing User Registration ===\n"

# Generate unique username and email
let timestamp = (date now | format date "%Y%m%d%H%M%S")
let username = $"testuser_($timestamp)"
let email = $"test_($timestamp)@example.com"
let password = "TestP@ss123!"

print $"Attempting to register user: ($username)"
print $"Email: ($email)\n"

try {
    let response = (
        http post $"($env.BASE_URL)/api/auth/register" {
            username: $username,
            email: $email,
            password: $password
        } --content-type application/json
    )

    print $"✅ Registration successful!"
    print $"User ID: ($response.id)"
    print $"Username: ($response.username)"
    print $"Email: ($response.email)\n"

    # Test duplicate registration (should fail)
    print "Testing duplicate registration (should fail)..."
    try {
        http post $"($env.BASE_URL)/api/auth/register" {
            username: $username,
            email: $email,
            password: $password
        } --content-type application/json

        print "❌ FAIL: Duplicate registration should have failed"
        exit 1
    } catch {
        print "✅ PASS: Duplicate registration correctly rejected\n"
    }

    print "✅ All registration tests passed!"
} catch {|e|
    print $"❌ FAIL: Registration failed - ($e)"
    exit 1
}
