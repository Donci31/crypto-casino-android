package hu.bme.aut.crypto_casino_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.data.model.transaction.TransactionType
import hu.bme.aut.crypto_casino_android.ui.theme.Amber
import hu.bme.aut.crypto_casino_android.ui.theme.Bet
import hu.bme.aut.crypto_casino_android.ui.theme.OnSurfaceVariant
import hu.bme.aut.crypto_casino_android.ui.theme.Purple
import hu.bme.aut.crypto_casino_android.ui.theme.Success
import hu.bme.aut.crypto_casino_android.ui.theme.SurfaceVariant
import hu.bme.aut.crypto_casino_android.ui.theme.Warning
import hu.bme.aut.crypto_casino_android.ui.theme.Win
import java.time.format.DateTimeFormatter

@Composable
fun BlockchainTransactionItem(
    transaction: BlockchainTransaction,
    onClick: (BlockchainTransaction) -> Unit
) {
    val (icon, color, label) = when (transaction.eventType) {
        TransactionType.DEPOSIT -> Triple(
            Icons.Default.ArrowDownward,
            Success,
            "Deposit"
        )
        TransactionType.WITHDRAWAL -> Triple(
            Icons.Default.ArrowUpward,
            Warning,
            "Withdrawal"
        )
        TransactionType.BET -> Triple(
            Icons.Default.Casino,
            Bet,
            "Bet"
        )
        TransactionType.WIN -> Triple(
            Icons.Default.EmojiEvents,
            Win,
            "Win"
        )
        TransactionType.TOKEN_PURCHASED -> Triple(
            Icons.Default.ShoppingCart,
            Purple,
            "Token Purchased"
        )
        TransactionType.TOKEN_EXCHANGED -> Triple(
            Icons.Default.SwapHoriz,
            Amber,
            "Token Exchanged"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(transaction) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
            }

            // Transaction details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = transaction.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )

                Text(
                    text = "Tx: ${transaction.txHash.take(8)}...${transaction.txHash.takeLast(8)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            // Amount and balance
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${transaction.amount} CST",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Balance: ${transaction.newBalance} CST",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}
