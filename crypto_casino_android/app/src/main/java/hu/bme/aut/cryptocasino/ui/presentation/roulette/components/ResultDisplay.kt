package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@Composable
fun ResultDisplay(
    winningNumber: Int?,
    totalPayout: BigDecimal?,
) {
    if (winningNumber != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (totalPayout != null && totalPayout > BigDecimal.ZERO) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFE53935)
                        },
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Winning Number: $winningNumber",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                if (totalPayout != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (totalPayout > BigDecimal.ZERO) "You Won: $totalPayout CST" else "No Win",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

