
package hu.bme.aut.crypto_casino_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import hu.bme.aut.crypto_casino_android.data.model.transaction.TransactionStatus
import hu.bme.aut.crypto_casino_android.data.model.transaction.TransactionType
import hu.bme.aut.crypto_casino_android.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit = {}
) {
    val (icon, color, label) = when (transaction.type) {
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
        TransactionType.PURCHASE -> Triple(
            Icons.Default.SwapHoriz,
            Info,
            "Purchase"
        )
        TransactionType.EXCHANGE -> Triple(
            Icons.Default.SwapHoriz,
            CasinoToken,
            "Exchange"
        )
        else -> Triple(
            Icons.Default.SwapHoriz,
            Color.Gray,
            "Unknown"
        )
    }

    val statusColor = when (transaction.status) {
        TransactionStatus.COMPLETED -> Success
        TransactionStatus.PENDING -> Warning
        TransactionStatus.FAILED -> Error
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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

                transaction.transactionTime?.let {
                    Text(
                        text = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                transaction.transactionHash?.let {
                    if (it.isNotEmpty()) {
                        Text(
                            text = "Tx: ${it.take(10)}...${it.takeLast(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            // Amount and status
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val amount = when (transaction.type) {
                    TransactionType.EXCHANGE, TransactionType.PURCHASE ->
                        "${transaction.ethereumAmount ?: 0.0} ETH"
                    else ->
                        "${transaction.casinoTokenAmount ?: 0.0} CST"
                }

                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                transaction.status?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
            }
        }
    }
}
