package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.data.model.roulette.BetType

@Composable
fun BettingArea(onBetPlaced: (BetType, Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Betting Area", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Straight Bets (36x)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items((0..36).toList()) { number ->
                    StraightBetButton(
                        number = number,
                        onClick = { onBetPlaced(BetType.STRAIGHT, number) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Outside Bets", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutsideBetButton(
                    text = "RED",
                    modifier = Modifier.weight(1f),
                    color = Color.Red,
                    onClick = { onBetPlaced(BetType.RED, 0) },
                )
                OutsideBetButton(
                    text = "BLACK",
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    onClick = { onBetPlaced(BetType.BLACK, 0) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutsideBetButton(
                    text = "ODD",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.ODD, 0) },
                )
                OutsideBetButton(
                    text = "EVEN",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.EVEN, 0) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutsideBetButton(
                    text = "LOW\n1-18",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.LOW, 0) },
                )
                OutsideBetButton(
                    text = "HIGH\n19-36",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.HIGH, 0) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Dozens (3x)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutsideBetButton(
                    text = "1st\n1-12",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.DOZEN_FIRST, 0) },
                )
                OutsideBetButton(
                    text = "2nd\n13-24",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.DOZEN_SECOND, 0) },
                )
                OutsideBetButton(
                    text = "3rd\n25-36",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.DOZEN_THIRD, 0) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Columns (3x)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutsideBetButton(
                    text = "Col 1",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.COLUMN_FIRST, 0) },
                )
                OutsideBetButton(
                    text = "Col 2",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.COLUMN_SECOND, 0) },
                )
                OutsideBetButton(
                    text = "Col 3",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.COLUMN_THIRD, 0) },
                )
            }
        }
    }
}

