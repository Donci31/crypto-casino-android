package hu.bme.aut.cryptocasino.ui.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.data.model.stats.UserStatsResponse
import hu.bme.aut.cryptocasino.data.util.FormatUtils
import hu.bme.aut.cryptocasino.ui.theme.ThemeColors
import java.math.BigDecimal

@Composable
fun GamingStatsSection(stats: UserStatsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gaming Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            StatsItemRow(
                icon = Icons.Default.Casino,
                label = "Total Games Played",
                value = stats.totalGamesPlayed.toString(),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "Win Rate",
                value = "${(stats.winRate * 100).toInt()}%",
                valueColor =
                    if (stats.winRate >= 0.5) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.EmojiEvents,
                label = "Biggest Win",
                value = "${FormatUtils.formatCurrency(stats.biggestWin)} CST",
                valueColor = ThemeColors.draculaGold,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.AttachMoney,
                label = "Net Profit/Loss",
                value = "${if (stats.netProfitLoss >= BigDecimal.ZERO) "+" else ""}${FormatUtils.formatCurrency(stats.netProfitLoss)} CST",
                valueColor =
                    if (stats.netProfitLoss >= BigDecimal.ZERO) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )

            if (stats.mostPlayedGame != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                StatsItemRow(
                    icon = Icons.Default.Casino,
                    label = "Most Played Game",
                    value = stats.mostPlayedGame,
                )
            }
        }
    }
}
