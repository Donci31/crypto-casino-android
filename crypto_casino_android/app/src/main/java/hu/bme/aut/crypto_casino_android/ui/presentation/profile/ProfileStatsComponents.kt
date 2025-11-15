package hu.bme.aut.crypto_casino_android.ui.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.crypto_casino_android.data.model.stats.GameStatsDto
import hu.bme.aut.crypto_casino_android.data.model.stats.UserStatsResponse
import hu.bme.aut.crypto_casino_android.data.util.FormatUtils
import java.math.BigDecimal

@Composable
fun GamingStatsSection(stats: UserStatsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gaming Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            StatsItemRow(
                icon = Icons.Default.Casino,
                label = "Total Games Played",
                value = stats.totalGamesPlayed.toString()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.TrendingUp,
                label = "Win Rate",
                value = "${(stats.winRate * 100).toInt()}%",
                valueColor = if (stats.winRate >= 0.5)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.EmojiEvents,
                label = "Biggest Win",
                value = "${FormatUtils.formatCurrency(stats.biggestWin)} CST",
                valueColor = Color(0xFFFFD700)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.AttachMoney,
                label = "Net Profit/Loss",
                value = "${if (stats.netProfitLoss >= BigDecimal.ZERO) "+" else ""}${FormatUtils.formatCurrency(stats.netProfitLoss)} CST",
                valueColor = if (stats.netProfitLoss >= BigDecimal.ZERO)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            if (stats.mostPlayedGame != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                StatsItemRow(
                    icon = Icons.Default.Casino,
                    label = "Most Played Game",
                    value = stats.mostPlayedGame
                )
            }
        }
    }
}

@Composable
fun GameBreakdownSection(gameStats: Map<String, GameStatsDto>) {
    if (gameStats.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Games Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            gameStats.entries.forEachIndexed { index, (gameName, stats) ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }

                GameStatsRow(
                    gameName = gameName,
                    stats = stats
                )
            }
        }
    }
}

@Composable
fun GameStatsRow(
    gameName: String,
    stats: GameStatsDto
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = gameName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${(stats.winRate * 100).toInt()}% Win",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (stats.winRate >= 0.5)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${stats.played} games • ${stats.wins} wins • ${stats.losses} losses",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun FinancialSummarySection(stats: UserStatsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Financial Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            StatsItemRow(
                icon = Icons.Default.TrendingUp,
                label = "Total Deposited",
                value = "${FormatUtils.formatCurrency(stats.totalDeposited)} CST"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.TrendingDown,
                label = "Total Withdrawn",
                value = "${FormatUtils.formatCurrency(stats.totalWithdrawn)} CST"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.AccountBalance,
                label = "Total Wagered",
                value = "${FormatUtils.formatCurrency(stats.totalWagered)} CST"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.AttachMoney,
                label = "Total Winnings",
                value = "${FormatUtils.formatCurrency(stats.totalWinnings)} CST",
                valueColor = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.Percent,
                label = "House Edge Paid",
                value = "${FormatUtils.formatCurrency(stats.houseEdgePaid)} CST"
            )
        }
    }
}

@Composable
fun StatsItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
