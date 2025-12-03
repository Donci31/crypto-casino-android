package hu.bme.aut.cryptocasino.ui.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.ui.theme.Amber
import hu.bme.aut.cryptocasino.ui.theme.Primary
import java.math.BigDecimal

@Composable
fun BalanceSummaryCard(
    walletBalance: BigDecimal,
    vaultBalance: BigDecimal
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BalanceItem(
                label = "Wallet Balance",
                amount = walletBalance,
                icon = Icons.Default.AccountBalanceWallet,
                color = Amber
            )

            Spacer(modifier = Modifier.width(16.dp))

            BalanceItem(
                label = "In Vault",
                amount = vaultBalance,
                icon = Icons.Default.Casino,
                color = Primary
            )
        }
    }
}