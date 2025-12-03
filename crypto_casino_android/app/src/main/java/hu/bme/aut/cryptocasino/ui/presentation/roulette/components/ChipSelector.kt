package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@Composable
fun ChipSelector(
    selectedChipValue: BigDecimal,
    onChipSelected: (BigDecimal) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Select Chip Value", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    listOf(
                        BigDecimal("10"),
                        BigDecimal("25"),
                        BigDecimal("50"),
                        BigDecimal("100"),
                        BigDecimal("250"),
                        BigDecimal("500"),
                    ),
                ) { chipValue ->
                    ChipButton(
                        value = chipValue,
                        isSelected = chipValue == selectedChipValue,
                        onClick = { onChipSelected(chipValue) },
                    )
                }
            }
        }
    }
}

