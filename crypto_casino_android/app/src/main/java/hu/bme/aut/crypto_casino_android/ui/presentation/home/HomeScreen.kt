package hu.bme.aut.crypto_casino_android.ui.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToWallet: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSlotMachine: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crypto Casino") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Welcome message
            Text(
                text = "Welcome to Crypto Casino",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Select a game or action below",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Game and action grid
            val items = listOf(
                HomeItem(
                    title = "Slot Machine",
                    icon = Icons.Default.Casino,
                    backgroundColor = Color(0xFF6A1B9A), // Purple
                    onClick = onNavigateToSlotMachine
                ),
                HomeItem(
                    title = "Wallet",
                    icon = Icons.Default.AccountBalanceWallet,
                    backgroundColor = Color(0xFF1E88E5), // Blue
                    onClick = onNavigateToWallet
                ),
                HomeItem(
                    title = "Transactions",
                    icon = Icons.Default.History,
                    backgroundColor = Color(0xFF43A047), // Green
                    onClick = onNavigateToTransactions
                ),
                HomeItem(
                    title = "Exchange",
                    icon = Icons.Default.CurrencyExchange,
                    backgroundColor = Color(0xFFE53935), // Red
                    onClick = {}
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    HomeItemCard(item)
                }
            }
        }
    }
}

data class HomeItem(
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeItemCard(item: HomeItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { item.onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.backgroundColor.copy(alpha = 0.8f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }

            Text(
                text = item.title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
