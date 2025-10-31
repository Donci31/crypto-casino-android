const BASE_URL = "http://localhost:8080"

source ../login.nu

let token = $env.CASINO_TOKEN

def main [game_id: int] {
    print $"🔍 Verifying fairness for Roulette game ID: ($game_id)\n"

    let game_status = (
        http get $"($BASE_URL)/api/games/roulette/status/($game_id)"
        --headers [Authorization $"Bearer ($token)"]
    )

    print "📊 Game Details:"
    print $"   Game ID: ($game_status.gameId)"
    print $"   Bets placed: ($game_status.bets | length)"
    print $"   Winning Number: ($game_status.winningNumber)"
    print $"   Total Payout: ($game_status.totalPayout) CST"
    print $"   Settled: ($game_status.settled)\n"

    print "🎲 Bets Breakdown:"
    for bet in $game_status.bets {
        print $"   - ($bet.betType): ($bet.amount) CST on number ($bet.number)"
    }
    print ""

    print "🔐 Provably Fair Seeds:"
    print $"   Server Seed Hash \(before game\): ($game_status.serverSeedHash)"
    print $"   Client Seed: ($game_status.clientSeed)"
    print $"   Server Seed \(revealed after\): ($game_status.serverSeed)\n"

    if ($game_status.settled) {
        print "✅ Game is settled and server seed has been revealed!"
        print "🔍 You can independently verify the result by:"
        print "   1. Hash the revealed server seed and compare with the hash shown before the game"
        print "   2. Combine server seed + client seed + player address + game ID"
        print "   3. Calculate keccak256 hash and take modulo 37"
        print $"   4. Result should be: ($game_status.winningNumber) (0-36)\n"

        print "🎰 Winning Number Analysis:"
        let num = $game_status.winningNumber

        if ($num == 0) {
            print "   Number: 0 (Green)"
            print "   Color: GREEN (house advantage)"
        } else {
            let red_numbers = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36]
            let is_red = ($num in $red_numbers)

            let color = if $is_red { "RED" } else { "BLACK" }
            let parity = if (($num mod 2) == 1) { "ODD" } else { "EVEN" }
            let range = if ($num <= 18) { "LOW (1-18)" } else { "HIGH (19-36)" }

            let dozen = if ($num <= 12) {
                "FIRST DOZEN (1-12)"
            } else if ($num <= 24) {
                "SECOND DOZEN (13-24)"
            } else {
                "THIRD DOZEN (25-36)"
            }

            let column = if (($num mod 3) == 1) {
                "FIRST COLUMN"
            } else if (($num mod 3) == 2) {
                "SECOND COLUMN"
            } else {
                "THIRD COLUMN"
            }

            print $"   Number: ($num)"
            print $"   Color: ($color)"
            print $"   Parity: ($parity)"
            print $"   Range: ($range)"
            print $"   Dozen: ($dozen)"
            print $"   Column: ($column)"
        }
        print ""

        print "📊 Bet Results:"
        for bet in $game_status.bets {
            let bet_type = $bet.betType
            let won = (check_bet_win $num $bet_type ($bet.number))

            let status = if $won { "✅ WON" } else { "❌ LOST" }
            let multiplier = (get_multiplier $bet_type)

            print $"   ($bet_type) ($bet.amount) CST: ($status) (pays ($multiplier)x)"
        }

    } else {
        print "⏳ Game not settled yet. Server seed not revealed."
    }
}

def check_bet_win [num: int, bet_type: string, bet_number: int] {
    if ($bet_type == "STRAIGHT") {
        return ($num == $bet_number)
    } else if ($bet_type == "RED") {
        let red_numbers = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36]
        return ($num in $red_numbers)
    } else if ($bet_type == "BLACK") {
        let red_numbers = [1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36]
        return (($num != 0) and (not ($num in $red_numbers)))
    } else if ($bet_type == "ODD") {
        return (($num != 0) and (($num mod 2) == 1))
    } else if ($bet_type == "EVEN") {
        return (($num != 0) and (($num mod 2) == 0))
    } else if ($bet_type == "LOW") {
        return (($num >= 1) and ($num <= 18))
    } else if ($bet_type == "HIGH") {
        return (($num >= 19) and ($num <= 36))
    } else if ($bet_type == "DOZEN_FIRST") {
        return (($num >= 1) and ($num <= 12))
    } else if ($bet_type == "DOZEN_SECOND") {
        return (($num >= 13) and ($num <= 24))
    } else if ($bet_type == "DOZEN_THIRD") {
        return (($num >= 25) and ($num <= 36))
    } else if ($bet_type == "COLUMN_FIRST") {
        return (($num >= 1) and (($num mod 3) == 1))
    } else if ($bet_type == "COLUMN_SECOND") {
        return (($num >= 2) and (($num mod 3) == 2))
    } else if ($bet_type == "COLUMN_THIRD") {
        return (($num >= 3) and (($num mod 3) == 0))
    } else {
        return false
    }
}

def get_multiplier [bet_type: string] {
    if ($bet_type == "STRAIGHT") {
        return 36
    } else if ($bet_type in ["RED", "BLACK", "ODD", "EVEN", "LOW", "HIGH"]) {
        return 2
    } else if ($bet_type in ["DOZEN_FIRST", "DOZEN_SECOND", "DOZEN_THIRD", "COLUMN_FIRST", "COLUMN_SECOND", "COLUMN_THIRD"]) {
        return 3
    } else {
        return 0
    }
}
