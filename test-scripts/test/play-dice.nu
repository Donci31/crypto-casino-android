source ../login.nu

let token = $env.CASINO_TOKEN

print $"Using token: ($token | str substring 0..20)...\n"

print "Getting dice configuration..."
let config = (
    http get $"($env.BASE_URL)/api/games/dice/config"
)

print $"Min Bet: ($config.minBet) CST"
print $"Max Bet: ($config.maxBet) CST"
print $"House Edge: ($config.houseEdge / 100)%"
print $"Active: ($config.active)\n"

print "Test 1: ROLL_UNDER 50 (bet: 100 CST)"
let game1 = (
    http post $"($env.BASE_URL)/api/games/dice/create"
    '{"betAmount": 100, "prediction": 50, "betType": "ROLL_UNDER", "clientSeed": "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"}'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game1.gameId)"
print $"Server Seed Hash: ($game1.serverSeedHash)"
print $"Prediction: Roll under ($game1.prediction)\n"

print "Settling game..."
let result1 = (
    http post $"($env.BASE_URL)/api/games/dice/settle/($game1.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Result: ($result1.result)"
print $"Payout: ($result1.payout) CST"
print $"Won: ($result1.won)"
print $"Server Seed: ($result1.serverSeed)\n"

print "Test 2: ROLL_OVER 50 (bet: 100 CST)"
let game2 = (
    http post $"($env.BASE_URL)/api/games/dice/create"
    '{"betAmount": 100, "prediction": 50, "betType": "ROLL_OVER", "clientSeed": "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"}'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game2.gameId)\n"

let result2 = (
    http post $"($env.BASE_URL)/api/games/dice/settle/($game2.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Result: ($result2.result)"
print $"Payout: ($result2.payout) CST"
print $"Won: ($result2.won)\n"

print "Test 3: EXACT 77 (bet: 50 CST)"
let game3 = (
    http post $"($env.BASE_URL)/api/games/dice/create"
    '{"betAmount": 50, "prediction": 77, "betType": "EXACT", "clientSeed": "0xfedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321"}'
    --content-type application/json
    --headers [Authorization $"Bearer ($token)"]
)

print $"Game created! ID: ($game3.gameId)\n"

let result3 = (
    http post $"($env.BASE_URL)/api/games/dice/settle/($game3.gameId)" ''
    --headers [Authorization $"Bearer ($token)"]
)

print $"Result: ($result3.result)"
print $"Payout: ($result3.payout) CST"
print $"Won: ($result3.won)\n"

