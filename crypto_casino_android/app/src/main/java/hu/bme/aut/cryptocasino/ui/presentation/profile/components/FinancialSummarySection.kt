package hu.bme.aut.cryptocasino.ui.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Percent
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

@Composable
fun FinancialSummarySection(stats: UserStatsResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Financial Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            StatsItemRow(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "Total Deposited",
                value = "${FormatUtils.formatCurrency(stats.totalDeposited)} CST",
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                label = "Total Withdrawn",
                value = "${FormatUtils.formatCurrency(stats.totalWithdrawn)} CST",
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.AccountBalance,
                label = "Total Wagered",
                value = "${FormatUtils.formatCurrency(stats.totalWagered)} CST",
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.AttachMoney,
                label = "Total Winnings",
                value = "${FormatUtils.formatCurrency(stats.totalWinnings)} CST",
                valueColor = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            StatsItemRow(
                icon = Icons.Default.Percent,
                label = "House Edge Paid",
                value = "${FormatUtils.formatCurrency(stats.houseEdgePaid)} CST",
            )
        }
    }
}
