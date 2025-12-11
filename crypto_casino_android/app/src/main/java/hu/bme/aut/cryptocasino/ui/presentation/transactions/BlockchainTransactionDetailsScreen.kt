package hu.bme.aut.cryptocasino.ui.presentation.transactions

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.paging.compose.collectAsLazyPagingItems
import hu.bme.aut.cryptocasino.data.model.transaction.BlockchainTransaction
import hu.bme.aut.cryptocasino.data.model.transaction.TransactionType
import hu.bme.aut.cryptocasino.data.util.FormatUtils
import hu.bme.aut.cryptocasino.ui.theme.ThemeColors
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockchainTransactionDetailScreen(
    transactionHash: String,
    blockNumber: Long,
    logIndex: Int,
    onNavigateBack: () -> Unit,
    parentEntry: NavBackStackEntry,
    viewModel: BlockchainTransactionsViewModel = hiltViewModel(parentEntry)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Get the paged transactions and find the one matching the composite key
    val transactions = viewModel.transactions.collectAsLazyPagingItems()
    val matchingTransaction = remember(transactionHash, blockNumber, logIndex, transactions.itemSnapshotList.items) {
        transactions.itemSnapshotList.items.find { tx ->
            tx.txHash == transactionHash &&
            tx.blockNumber == blockNumber &&
            tx.logIndex == logIndex
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (matchingTransaction != null) {
                TransactionDetailContent(matchingTransaction, snackbarHostState)
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun TransactionDetailContent(
    transaction: BlockchainTransaction,
    snackbarHostState: SnackbarHostState
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val (icon, color, label) = when (transaction.eventType) {
        TransactionType.DEPOSIT -> Triple(Icons.Default.ArrowDownward, ThemeColors.success, "Deposit")
        TransactionType.WITHDRAWAL -> Triple(Icons.Default.ArrowUpward, ThemeColors.warning, "Withdrawal")
        TransactionType.BET -> Triple(Icons.Default.Casino, ThemeColors.bet, "Bet")
        TransactionType.WIN -> Triple(Icons.Default.EmojiEvents, ThemeColors.win, "Win")
        TransactionType.TOKEN_PURCHASED -> Triple(Icons.Default.ShoppingCart, ThemeColors.purple, "Token Purchased")
        TransactionType.TOKEN_EXCHANGED -> Triple(Icons.Default.SwapHoriz, ThemeColors.amber, "Token Exchanged")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Transaction header with icon and amount
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction type
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                Text(
                    text = when (transaction.eventType) {
                        TransactionType.DEPOSIT, TransactionType.WIN, TransactionType.TOKEN_PURCHASED ->
                            "+${FormatUtils.formatAmount(transaction.amount)} CST"
                        TransactionType.WITHDRAWAL, TransactionType.BET, TransactionType.TOKEN_EXCHANGED ->
                            "-${FormatUtils.formatAmount(transaction.amount)} CST"
                    },
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                Spacer(modifier = Modifier.height(4.dp))

                // New balance
                Text(
                    text = "Balance: ${FormatUtils.formatAmount(transaction.newBalance)} CST",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Transaction Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Timestamp
                DetailRow(
                    label = "Date & Time",
                    value = transaction.timestamp.format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Block number
                DetailRow(
                    label = "Block Number",
                    value = "#${transaction.blockNumber}"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Log index
                DetailRow(
                    label = "Log Index",
                    value = transaction.logIndex.toString()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blockchain details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Blockchain Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Transaction hash
                CopyableDetailRow(
                    label = "Transaction Hash",
                    value = transaction.txHash,
                    onCopy = {
                        scope.launch {
                            clipboard.setClipEntry(ClipData.newPlainText("Transaction Hash", transaction.txHash).toClipEntry())
                            snackbarHostState.showSnackbar("Transaction hash copied")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // User address
                CopyableDetailRow(
                    label = "User Address",
                    value = transaction.userAddress,
                    onCopy = {
                        scope.launch {
                            clipboard.setClipEntry(ClipData.newPlainText("User Address", transaction.userAddress).toClipEntry())
                            snackbarHostState.showSnackbar("User address copied")
                        }
                    }
                )

                // Game address (if present)
                transaction.gameAddress?.let { gameAddress ->
                    Spacer(modifier = Modifier.height(12.dp))
                    CopyableDetailRow(
                        label = "Game Address",
                        value = gameAddress,
                        onCopy = {
                            scope.launch {
                                clipboard.setClipEntry(ClipData.newPlainText("Game Address", gameAddress).toClipEntry())
                                snackbarHostState.showSnackbar("Game address copied")
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CopyableDetailRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onCopy() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = FormatUtils.shortenAddress(value, 10, 8),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
