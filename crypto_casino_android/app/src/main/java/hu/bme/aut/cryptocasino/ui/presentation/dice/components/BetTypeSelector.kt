package hu.bme.aut.cryptocasino.ui.presentation.dice.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.data.model.dice.BetType

@Composable
fun BetTypeSelector(
    selectedBetType: BetType,
    onBetTypeChange: (BetType) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bet Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BetType.entries.forEach { betType ->
                FilterChip(
                    selected = selectedBetType == betType,
                    onClick = { onBetTypeChange(betType) },
                    label = {
                        Text(
                            text =
                                when (betType) {
                                    BetType.ROLL_UNDER -> "Under"
                                    BetType.ROLL_OVER -> "Over"
                                    BetType.EXACT -> "Exact"
                                },
                        )
                    },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                        ),
                )
            }
        }
    }
}
