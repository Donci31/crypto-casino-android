const BASE_URL = "http://localhost:8080"

source ../login.nu

let token = $env.CASINO_TOKEN

def main [game_id: int] {
    print $"🔍 Verifying fairness for game ID: ($game_id)\n"

    let game_status = (
        http get $"($BASE_URL)/api/games/dice/status/($game_id)"
        --headers [Authorization $"Bearer ($token)"]
    )

    print "📊 Game Details:"
    print $"   Prediction: ($game_status.prediction)"
    print $"   Bet Type: ($game_status.betType)"
    print $"   Result: ($game_status.result)"
    print $"   Payout: ($game_status.payout) CST"
    print $"   Settled: ($game_status.settled)\n"

    print "🔐 Provably Fair Seeds:"
    print $"   Server Seed Hash \(before game\): ($game_status.serverSeedHash)"
    print $"   Client Seed: ($game_status.clientSeed)"
    print $"   Server Seed \(revealed after\): ($game_status.serverSeed)\n"

    if ($game_status.settled) {
        print "✅ Game is settled and server seed has been revealed!"
        print "🔍 You can independently verify the result by:"
        print "   1. Hash the revealed server seed and compare with the hash shown before the game"
        print "   2. Combine server seed + client seed + player address + game ID"
        print "   3. Calculate keccak256 hash and take modulo 100 + 1"
        print $"   4. Result should be: ($game_status.result)\n"
    } else {
        print "⏳ Game not settled yet. Server seed not revealed."
    }
}

def verify_game [game_id: int, token: string] {
    
}
