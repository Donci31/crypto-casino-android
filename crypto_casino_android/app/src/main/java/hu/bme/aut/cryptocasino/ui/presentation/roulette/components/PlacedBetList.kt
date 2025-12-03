package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.ui.presentation.roulette.PlacedBet
import java.math.BigDecimal

@Composable
fun PlacedBetsList(
    bets: List<PlacedBet>,
    totalBetAmount: BigDecimal,
    onRemoveBet: (Int) -> Unit,
) {
    if (bets.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Placed Bets (${bets.size}/20)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Total: $totalBetAmount CST",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                bets.forEachIndexed { index, bet ->
                    BetItem(
                        bet = bet,
                        onRemove = { onRemoveBet(index) },
                    )
                    if (index < bets.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

