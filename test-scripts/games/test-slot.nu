# Test Slot Machine game
source ../login.nu

let token = $env.JWT_TOKEN

print "=== Testing Slot Machine ===\n"

print "Getting slot machine configuration..."
let config = (
    http get $"($env.BASE_URL)/api/games/slots/config"
    --headers [Authorization $"Bearer ($token)"]
)

print $"Min Bet: ($config.minBet) CST"
print $"Max Bet: ($config.maxBet) CST"
print $"Active: ($config.active)\n"

# Test 1: Minimum bet
print "Test 1: Spin with minimum bet"
let bet_amount = $config.minBet
print $"Bet: ($bet_amount) CST\n"

try {
    let spin1 = (
        http post $"($env.BASE_URL)/api/games/slots/spin" {
            betAmount: $bet_amount
        }
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )

    print $"✅ Spin successful!"
    print $"Reels: [($spin1.reel1), ($spin1.reel2), ($spin1.reel3)]"
    print $"Payout: ($spin1.payout) CST"
    print $"Won: ($spin1.won)"
    print $"Multiplier: ($spin1.multiplier)x"
    if ($spin1.won) {
        print $"Win Type: ($spin1.winType)"
    }
    print ""

    # Test 2: Higher bet
    print "Test 2: Spin with 100 CST bet"
    let spin2 = (
        http post $"($env.BASE_URL)/api/games/slots/spin" {
            betAmount: 100
        }
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )

    print $"Reels: [($spin2.reel1), ($spin2.reel2), ($spin2.reel3)]"
    print $"Payout: ($spin2.payout) CST"
    print $"Won: ($spin2.won)\n"

    # Test 3: Maximum bet
    print "Test 3: Spin with maximum bet"
    let max_bet = $config.maxBet
    print $"Bet: ($max_bet) CST\n"

    let spin3 = (
        http post $"($env.BASE_URL)/api/games/slots/spin" {
            betAmount: $max_bet
        }
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]
    )

    print $"Reels: [($spin3.reel1), ($spin3.reel2), ($spin3.reel3)]"
    print $"Payout: ($spin3.payout) CST"
    print $"Won: ($spin3.won)\n"

    # Test 4: Below minimum bet (should fail)
    print "Test 4: Spin below minimum bet (should fail)"
    try {
        http post $"($env.BASE_URL)/api/games/slots/spin" {
            betAmount: ($config.minBet - 1)
        }
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]

        print "❌ FAIL: Bet below minimum should have failed"
        exit 1
    } catch {
        print "✅ PASS: Below minimum bet correctly rejected\n"
    }

    # Test 5: Above maximum bet (should fail)
    print "Test 5: Spin above maximum bet (should fail)"
    try {
        http post $"($env.BASE_URL)/api/games/slots/spin" {
            betAmount: ($config.maxBet + 1)
        }
        --content-type application/json
        --headers [Authorization $"Bearer ($token)"]

        print "❌ FAIL: Bet above maximum should have failed"
        exit 1
    } catch {
        print "✅ PASS: Above maximum bet correctly rejected\n"
    }

    # Get game history
    print "Getting game history..."
    let history = (
        http get $"($env.BASE_URL)/api/games/slots/history"
        --headers [Authorization $"Bearer ($token)"]
    )

    print $"Total games played: ($history | length)"
    print "Recent games:"
    for game in ($history | first 3) {
        print $"  Game #($game.id): [($game.reel1), ($game.reel2), ($game.reel3)] - Payout: ($game.payout) CST (Won: ($game.won))"
    }
    print ""

    print "✅ All slot machine tests passed!"
} catch {|e|
    print $"❌ FAIL: Slot machine test failed - ($e)"
    exit 1
}
