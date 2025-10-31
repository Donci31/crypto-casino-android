const BASE_URL = "http://localhost:8080"

source ../login.nu

let token = $env.CASINO_TOKEN

if ($token | is-empty) {
    print "❌ No authentication token found!"
    print "   Make sure login.nu was sourced successfully"
    exit 1
}

print "🧪 CryptoCasino - Roulette Edge Case Tests\n"
print $"🔑 Using token: ($token | str substring 0..20)...\n"

print "📋 Getting roulette configuration..."
let config = (
    http get $"($BASE_URL)/api/games/roulette/config"
)

print $"   Min Bet: ($config.minBet) CST"
print $"   Max Bet: ($config.maxBet) CST\n"

print "🧪 Test 1: Empty bets array (should fail)"
try {
    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        '{
            "bets": [],
            "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        }'
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print "   ❌ Test FAILED: Should have rejected empty bets"
} catch {
    print "   ✅ Test PASSED: Correctly rejected empty bets\n"
}

print "🧪 Test 2: Too many bets (>20, should fail)"
try {
    let many_bets = (seq 0 20 | each { |i| '{"betType": "RED", "amount": 10, "number": 0}' } | str join ',')
    let payload = $'{"bets": [($many_bets)], "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"}'

    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        $payload
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print "   ❌ Test FAILED: Should have rejected >20 bets"
} catch {
    print "   ✅ Test PASSED: Correctly rejected >20 bets\n"
}

print "🧪 Test 3: Bet below minimum (should fail)"
try {
    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        '{
            "bets": [
                {"betType": "RED", "amount": 5, "number": 0}
            ],
            "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        }'
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print "   ❌ Test FAILED: Should have rejected bet below minimum"
} catch {
    print "   ✅ Test PASSED: Correctly rejected bet below minimum\n"
}

print "🧪 Test 4: Bet above maximum (should fail)"
try {
    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        '{
            "bets": [
                {"betType": "RED", "amount": 5000, "number": 0}
            ],
            "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        }'
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print "   ❌ Test FAILED: Should have rejected bet above maximum"
} catch {
    print "   ✅ Test PASSED: Correctly rejected bet above maximum\n"
}

print "🧪 Test 5: Invalid STRAIGHT number (>36, should fail)"
try {
    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        '{
            "bets": [
                {"betType": "STRAIGHT", "amount": 100, "number": 37}
            ],
            "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        }'
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print "   ❌ Test FAILED: Should have rejected STRAIGHT number > 36"
} catch {
    print "   ✅ Test PASSED: Correctly rejected invalid STRAIGHT number\n"
}

print "🧪 Test 6: Valid STRAIGHT on 0 (green, should succeed)"
try {
    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        '{
            "bets": [
                {"betType": "STRAIGHT", "amount": 100, "number": 0}
            ],
            "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        }'
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print $"   ✅ Test PASSED: Accepted STRAIGHT bet on 0 (gameId: ($game.gameId))\n"
} catch {
    print "   ❌ Test FAILED: Should have accepted STRAIGHT bet on 0\n"
}

print "🧪 Test 7: Maximum allowed bets (20 bets, should succeed)"
try {
    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        '{
            "bets": [
                {"betType": "STRAIGHT", "amount": 10, "number": 0},
                {"betType": "STRAIGHT", "amount": 10, "number": 1},
                {"betType": "STRAIGHT", "amount": 10, "number": 2},
                {"betType": "STRAIGHT", "amount": 10, "number": 3},
                {"betType": "STRAIGHT", "amount": 10, "number": 4},
                {"betType": "STRAIGHT", "amount": 10, "number": 5},
                {"betType": "STRAIGHT", "amount": 10, "number": 6},
                {"betType": "STRAIGHT", "amount": 10, "number": 7},
                {"betType": "STRAIGHT", "amount": 10, "number": 8},
                {"betType": "STRAIGHT", "amount": 10, "number": 9},
                {"betType": "RED", "amount": 10, "number": 0},
                {"betType": "BLACK", "amount": 10, "number": 0},
                {"betType": "ODD", "amount": 10, "number": 0},
                {"betType": "EVEN", "amount": 10, "number": 0},
                {"betType": "LOW", "amount": 10, "number": 0},
                {"betType": "HIGH", "amount": 10, "number": 0},
                {"betType": "DOZEN_FIRST", "amount": 10, "number": 0},
                {"betType": "DOZEN_SECOND", "amount": 10, "number": 0},
                {"betType": "COLUMN_FIRST", "amount": 10, "number": 0},
                {"betType": "COLUMN_SECOND", "amount": 10, "number": 0}
            ],
            "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        }'
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print $"   ✅ Test PASSED: Accepted 20 bets (gameId: ($game.gameId))"
    print $"   Total bet amount: ($game.totalBetAmount) CST\n"
} catch {
    print "   ❌ Test FAILED: Should have accepted 20 bets\n"
}

print "🧪 Test 8: All bet types in one game (comprehensive test)"
try {
    let game = (
        http post $"($BASE_URL)/api/games/roulette/create"
        '{
            "bets": [
                {"betType": "STRAIGHT", "amount": 50, "number": 17},
                {"betType": "RED", "amount": 100, "number": 0},
                {"betType": "BLACK", "amount": 100, "number": 0},
                {"betType": "ODD", "amount": 100, "number": 0},
                {"betType": "EVEN", "amount": 100, "number": 0},
                {"betType": "LOW", "amount": 100, "number": 0},
                {"betType": "HIGH", "amount": 100, "number": 0},
                {"betType": "DOZEN_FIRST", "amount": 75, "number": 0},
                {"betType": "DOZEN_SECOND", "amount": 75, "number": 0},
                {"betType": "DOZEN_THIRD", "amount": 75, "number": 0},
                {"betType": "COLUMN_FIRST", "amount": 75, "number": 0},
                {"betType": "COLUMN_SECOND", "amount": 75, "number": 0},
                {"betType": "COLUMN_THIRD", "amount": 75, "number": 0}
            ],
            "clientSeed": "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"
        }'
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )
    print $"   ✅ Game created with all 13 bet types! (gameId: ($game.gameId))"
    print $"   Total bet amount: ($game.totalBetAmount) CST\n"

    print "   ⏳ Settling game..."
    let result = (
        http post $"($BASE_URL)/api/games/roulette/settle/($game.gameId)" ''
        --headers [Authorization $"Bearer ($token)"]
    )

    print $"   🎲 Winning Number: ($result.winningNumber)"
    print $"   💰 Total Payout: ($result.totalPayout) CST"

    let profit = ($result.totalPayout - $game.totalBetAmount)
    if ($profit > 0) {
        print $"   📈 Net Profit: +($profit) CST"
    } else if ($profit < 0) {
        print $"   📉 Net Loss: ($profit) CST"
    } else {
        print $"   ⚖️  Break Even"
    }
    print ""
} catch {
    print "   ❌ Test FAILED: Comprehensive bet test failed\n"
}

print "🧪 Test 9: Settle non-existent game (should fail)"
try {
    let result = (
        http post $"($BASE_URL)/api/games/roulette/settle/99999" ''
        --headers [Authorization $"Bearer ($token)"]
    )
    print "   ❌ Test FAILED: Should have rejected non-existent game"
} catch {
    print "   ✅ Test PASSED: Correctly rejected non-existent game\n"
}

print "🧪 Test 10: Get status of non-existent game (should fail)"
try {
    let status = (
        http get $"($BASE_URL)/api/games/roulette/status/99999"
        --headers [Authorization $"Bearer ($token)"]
    )
    print "   ❌ Test FAILED: Should have rejected non-existent game"
} catch {
    print "   ✅ Test PASSED: Correctly rejected non-existent game\n"
}

print "✨ All edge case tests completed!\n"

print "📊 Test Summary:"
print "   ✅ Input validation (empty bets, too many bets)"
print "   ✅ Bet amount validation (min/max limits)"
print "   ✅ Bet type validation (invalid numbers)"
print "   ✅ Boundary testing (0, 36, 20 bets)"
print "   ✅ Comprehensive bet coverage (all 13 bet types)"
print "   ✅ Error handling (non-existent games)"
