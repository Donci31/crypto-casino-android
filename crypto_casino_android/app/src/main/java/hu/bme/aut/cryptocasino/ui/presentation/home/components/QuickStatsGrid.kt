package hu.bme.aut.cryptocasino.ui.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.data.model.stats.QuickStatsResponse
import hu.bme.aut.cryptocasino.data.util.FormatUtils
import hu.bme.aut.cryptocasino.ui.theme.ThemeColors

@Composable
fun QuickStatsGrid(stats: QuickStatsResponse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Stats",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(320.dp)
        ) {
            item {
                StatCard(
                    label = "Games Played",
                    value = stats.totalGamesPlayed.toString(),
                    icon = Icons.Default.Casino,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                StatCard(
                    label = "Win Rate",
                    value = "${(stats.winRate * 100).toInt()}%",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                StatCard(
                    label = "Total Winnings",
                    value = FormatUtils.formatCurrency(stats.totalWinnings),
                    subtitle = "CST",
                    icon = Icons.Default.AttachMoney,
                    color = ThemeColors.amber
                )
            }

            item {
                StatCard(
                    label = "Biggest Win",
                    value = FormatUtils.formatCurrency(stats.biggestWin),
                    subtitle = "CST",
                    icon = Icons.Default.EmojiEvents,
                    color = ThemeColors.draculaGold
                )
            }
        }
    }
}