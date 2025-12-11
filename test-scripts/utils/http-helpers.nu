# HTTP request helper functions

# Authenticated GET request
export def auth-get [url: string, token: string] {
    http get $url --headers [Authorization $"Bearer ($token)"]
}

# Authenticated POST request with JSON body
export def auth-post [url: string, body: record, token: string] {
    http post $url $body
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
}

# Authenticated PUT request with JSON body
export def auth-put [url: string, body: record, token: string] {
    http put $url $body
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
}

# Authenticated DELETE request
export def auth-delete [url: string, token: string] {
    http delete $url --headers [Authorization $"Bearer ($token)"]
}

# Simple POST request without auth
export def post-json [url: string, body: record] {
    http post $url $body --content-type application/json
}

# Simple GET request without auth
export def get-json [url: string] {
    http get $url
}

# Pretty print JSON response
export def print-json [data: any] {
    $data | to json --indent 2 | print
}

# Check if response is successful (status 2xx)
export def is-success [response: record] {
    let status = $response.status
    ($status >= 200 and $status < 300)
}

# Extract error message from response
export def get-error [response: record] {
    if ("message" in $response) {
        $response.message
    } else if ("error" in $response) {
        $response.error
    } else {
        "Unknown error"
    }
}

# Print success message
export def print-success [message: string] {
    print $"✅ ($message)"
}

# Print failure message
export def print-fail [message: string] {
    print $"❌ ($message)"
}

# Print info message
export def print-info [message: string] {
    print $"ℹ️  ($message)"
}

# Print warning message
export def print-warning [message: string] {
    print $"⚠️  ($message)"
}
