source ../login.nu

let token = $env.CASINO_TOKEN

print $"Using token: ($token | str substring 0..20)...\n"

print "Getting roulette configuration..."
let config = (
    http get $"($env.BASE_URL)/api/games/roulette/config"
)

print $"Min Bet: ($config.minBet) CST"
print $"Max Bet: ($config.maxBet) CST"
print $"House Edge: ($config.houseEdge / 100)%"
print $"Active: ($config.active)\n"

print "Test 1: Single bet on RED (bet: 100 CST)"
let game1 = (
    http post $"($env.BASE_URL)/api/games/roulette/create"
    '{
        "bets": [
            {"betType": "RED", "amount": 100, "number": 0}
        ],
        "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
    }'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game1.gameId)"
print $"Server Seed Hash: ($game1.serverSeedHash)"
print $"Total Bet: ($game1.totalBetAmount) CST\n"

print "Settling game..."
let result1 = (
    http post $"($env.BASE_URL)/api/games/roulette/settle/($game1.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Winning Number: ($result1.winningNumber)"
print $"Total Payout: ($result1.totalPayout) CST"
print $"Server Seed: ($result1.serverSeed)\n"

print "Test 2: Multiple bets (STRAIGHT 7, RED, ODD) - Total: 300 CST"
let game2 = (
    http post $"($env.BASE_URL)/api/games/roulette/create"
    '{
        "bets": [
            {"betType": "STRAIGHT", "amount": 100, "number": 7},
            {"betType": "RED", "amount": 100, "number": 0},
            {"betType": "ODD", "amount": 100, "number": 0}
        ],
        "clientSeed": "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"
    }'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game2.gameId)"
print $"Bets placed: 3"
print $"Total Bet: ($game2.totalBetAmount) CST\n"

let result2 = (
    http post $"($env.BASE_URL)/api/games/roulette/settle/($game2.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Winning Number: ($result2.winningNumber)"
print $"Total Payout: ($result2.totalPayout) CST"

print "Test 3: All even-money bets (RED, ODD, LOW) - Total: 300 CST"
let game3 = (
    http post $"($env.BASE_URL)/api/games/roulette/create"
    '{
        "bets": [
            {"betType": "RED", "amount": 100, "number": 0},
            {"betType": "ODD", "amount": 100, "number": 0},
            {"betType": "LOW", "amount": 100, "number": 0}
        ],
        "clientSeed": "0xfedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321"
    }'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game3.gameId)\n"

let result3 = (
    http post $"($env.BASE_URL)/api/games/roulette/settle/($game3.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Winning Number: ($result3.winningNumber)"
print $"Total Payout: ($result3.totalPayout) CST\n"

print "Test 4: Dozens and Columns (DOZEN_FIRST, COLUMN_SECOND) - Total: 200 CST"
let game4 = (
    http post $"($env.BASE_URL)/api/games/roulette/create"
    '{
        "bets": [
            {"betType": "DOZEN_FIRST", "amount": 100, "number": 0},
            {"betType": "COLUMN_SECOND", "amount": 100, "number": 0}
        ],
        "clientSeed": "0x1111111111111111111111111111111111111111111111111111111111111111"
    }'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game4.gameId)\n"

let result4 = (
    http post $"($env.BASE_URL)/api/games/roulette/settle/($game4.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Winning Number: ($result4.winningNumber)"
print $"Total Payout: ($result4.totalPayout) CST"

print "Test 5: High roller STRAIGHT bet on 17 (bet: 500 CST)"
let game5 = (
    http post $"($env.BASE_URL)/api/games/roulette/create"
    '{
        "bets": [
            {"betType": "STRAIGHT", "amount": 500, "number": 17}
        ],
        "clientSeed": "0x2222222222222222222222222222222222222222222222222222222222222222"
    }'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game5.gameId)\n"

let result5 = (
    http post $"($env.BASE_URL)/api/games/roulette/settle/($game5.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Winning Number: ($result5.winningNumber)"
print $"Total Payout: ($result5.totalPayout) CST"

print "Test 6: Cover the board strategy (BLACK, EVEN, HIGH) - Total: 300 CST"
let game6 = (
    http post $"($env.BASE_URL)/api/games/roulette/create"
    '{
        "bets": [
            {"betType": "BLACK", "amount": 100, "number": 0},
            {"betType": "EVEN", "amount": 100, "number": 0},
            {"betType": "HIGH", "amount": 100, "number": 0}
        ],
        "clientSeed": "0x3333333333333333333333333333333333333333333333333333333333333333"
    }'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game6.gameId)\n"

let result6 = (
    http post $"($env.BASE_URL)/api/games/roulette/settle/($game6.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Winning Number: ($result6.winningNumber)"
print $"Total Payout: ($result6.totalPayout) CST"

