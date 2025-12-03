package hu.bme.aut.cryptocasino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.data.model.transaction.BlockchainTransaction
import hu.bme.aut.cryptocasino.data.model.transaction.TransactionType
import hu.bme.aut.cryptocasino.data.util.FormatUtils
import hu.bme.aut.cryptocasino.ui.theme.Amber
import hu.bme.aut.cryptocasino.ui.theme.Bet
import hu.bme.aut.cryptocasino.ui.theme.Purple
import hu.bme.aut.cryptocasino.ui.theme.Success
import hu.bme.aut.cryptocasino.ui.theme.Warning
import hu.bme.aut.cryptocasino.ui.theme.Win
import java.time.format.DateTimeFormatter

@Composable
fun BlockchainTransactionItem(
    transaction: BlockchainTransaction,
    onClick: (BlockchainTransaction) -> Unit,
) {
    val (icon, color, label) =
        when (transaction.eventType) {
            TransactionType.DEPOSIT -> {
                Triple(
                    Icons.Default.ArrowDownward,
                    Success,
                    "Deposit",
                )
            }

            TransactionType.WITHDRAWAL -> {
                Triple(
                    Icons.Default.ArrowUpward,
                    Warning,
                    "Withdrawal",
                )
            }

            TransactionType.BET -> {
                Triple(
                    Icons.Default.Casino,
                    Bet,
                    "Bet",
                )
            }

            TransactionType.WIN -> {
                Triple(
                    Icons.Default.EmojiEvents,
                    Win,
                    "Win",
                )
            }

            TransactionType.TOKEN_PURCHASED -> {
                Triple(
                    Icons.Default.ShoppingCart,
                    Purple,
                    "Token Purchased",
                )
            }

            TransactionType.TOKEN_EXCHANGED -> {
                Triple(
                    Icons.Default.SwapHoriz,
                    Amber,
                    "Token Exchanged",
                )
            }
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick(transaction) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text =
                            transaction.timestamp.format(
                                DateTimeFormatter.ofPattern("MMM dd, HH:mm"),
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text =
                        when (transaction.eventType) {
                            TransactionType.DEPOSIT, TransactionType.WIN, TransactionType.TOKEN_PURCHASED -> {
                                "+${FormatUtils.formatAmount(transaction.amount)}"
                            }

                            TransactionType.WITHDRAWAL, TransactionType.BET, TransactionType.TOKEN_EXCHANGED -> {
                                "-${FormatUtils.formatAmount(transaction.amount)}"
                            }
                        },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                        when (transaction.eventType) {
                            TransactionType.DEPOSIT, TransactionType.WIN, TransactionType.TOKEN_PURCHASED -> Success
                            TransactionType.WITHDRAWAL, TransactionType.BET, TransactionType.TOKEN_EXCHANGED -> color
                        },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Tx:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = FormatUtils.shortenHash(transaction.txHash, 6, 4),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Balance:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${FormatUtils.formatAmount(transaction.newBalance)} CST",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
