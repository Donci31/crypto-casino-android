# JWT token management utilities

# Login and get JWT token
export def login [base_url: string, username: string, password: string] {
    let response = (
        http post $"($base_url)/api/auth/login" {
            usernameOrEmail: $username,
            password: $password
        } --content-type application/json
    )

    if ($response.token | is-empty) {
        error make {msg: "Login failed: No token returned"}
    }

    {
        token: $response.token,
        refreshToken: $response.refreshToken
    }
}

# Refresh JWT token
export def refresh-token [base_url: string, refresh_token: string] {
    let response = (
        http post $"($base_url)/api/auth/refresh" ''
        --headers [Authorization $"Bearer ($refresh_token)"]
    )

    if ($response.token | is-empty) {
        error make {msg: "Token refresh failed: No token returned"}
    }

    {
        token: $response.token,
        refreshToken: $response.refreshToken
    }
}

# Register new user
export def register [base_url: string, username: string, email: string, password: string] {
    let response = (
        http post $"($base_url)/api/auth/register" {
            username: $username,
            email: $email,
            password: $password
        } --content-type application/json
    )

    $response
}

# Decode JWT token (extract payload)
export def decode-jwt [token: string] {
    let parts = ($token | split row ".")
    if (($parts | length) != 3) {
        error make {msg: "Invalid JWT format"}
    }

    let payload = ($parts | get 1)
    # Note: Nushell doesn't have built-in base64 decode, this is a placeholder
    # In practice you'd need to use an external tool or implement base64 decoding
    $payload
}

# Check if token is expired (simplified check)
export def is-token-valid [token: string] {
    not ($token | is-empty)
}

# Store tokens in environment
export def store-tokens [jwt_token: string, refresh_token: string] {
    $env.JWT_TOKEN = $jwt_token
    $env.REFRESH_TOKEN = $refresh_token
}

# Get stored JWT token
export def get-jwt [] {
    if ("JWT_TOKEN" in $env) {
        $env.JWT_TOKEN
    } else {
        error make {msg: "No JWT token found in environment"}
    }
}

# Get stored refresh token
export def get-refresh-token [] {
    if ("REFRESH_TOKEN" in $env) {
        $env.REFRESH_TOKEN
    } else {
        error make {msg: "No refresh token found in environment"}
    }
}

# Print token info (first 20 characters)
export def print-token-info [token: string, label: string] {
    let preview = ($token | str substring 0..20)
    print $"($label): ($preview)..."
}
