package hu.bme.aut.crypto_casino_android.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.data.model.transaction.TransactionType
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.ui.components.BlockchainTransactionItem
import java.math.BigDecimal

@Composable
fun BlockchainTransactionsScreen(
    onTransactionClick: (String) -> Unit,
    viewModel: BlockchainTransactionsViewModel = hiltViewModel()
) {
    val transactionsState by viewModel.transactionsState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (transactionsState) {
            is ApiResult.Success -> {
                val transactions = (transactionsState as ApiResult.Success<List<BlockchainTransaction>>).data
                if (transactions.isEmpty()) {
                    EmptyTransactionsContent(onRefresh = { viewModel.getTransactions() })
                } else {
                    TransactionsContent(
                        transactions = transactions,
                        onTransactionClick = onTransactionClick
                    )
                }
            }
            is ApiResult.Error -> {
                ErrorContent(
                    message = (transactionsState as ApiResult.Error).exception.message ?: "Unknown error",
                    onRetry = { viewModel.getTransactions() }
                )
            }
            ApiResult.Loading, null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun TransactionsContent(
    transactions: List<BlockchainTransaction>,
    onTransactionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Blockchain Transactions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Transaction summary
        item {
            TransactionSummaryCard(transactions)
        }

        items(transactions) { transaction ->
            BlockchainTransactionItem(
                transaction = transaction,
                onClick = { onTransactionClick(it.txHash) }
            )
        }
    }
}

@Composable
fun TransactionSummaryCard(transactions: List<BlockchainTransaction>) {
    // Calculate summary information
    val totalDeposits = transactions
        .filter { it.eventType == TransactionType.DEPOSIT }
        .sumOf { it.amount }

    val totalWithdrawals = transactions
        .filter { it.eventType == TransactionType.WITHDRAWAL }
        .sumOf { it.amount }

    val totalBets = transactions
        .filter { it.eventType == TransactionType.BET }
        .sumOf { it.amount }

    val totalWins = transactions
        .filter { it.eventType == TransactionType.WIN }
        .sumOf { it.amount }

    val totalPurchased = transactions
        .filter { it.eventType == TransactionType.TOKEN_PURCHASED }
        .sumOf { it.amount }

    val totalExchanged = transactions
        .filter { it.eventType == TransactionType.TOKEN_EXCHANGED }
        .sumOf { it.amount }

    val netGaming = totalWins.subtract(totalBets)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transaction Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Display summary statistics
            SummaryRow("Total Deposits", totalDeposits)
            SummaryRow("Total Withdrawals", totalWithdrawals)
            SummaryRow("Total Bets", totalBets)
            SummaryRow("Total Wins", totalWins)
            SummaryRow("Net Gaming", netGaming)

            if (totalPurchased > BigDecimal.ZERO) {
                SummaryRow("Total Purchased", totalPurchased)
            }

            if (totalExchanged > BigDecimal.ZERO) {
                SummaryRow("Total Exchanged", totalExchanged)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: BigDecimal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "$value CST",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun EmptyTransactionsContent(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No blockchain transactions found",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading transactions: $message",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = "Retry")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
