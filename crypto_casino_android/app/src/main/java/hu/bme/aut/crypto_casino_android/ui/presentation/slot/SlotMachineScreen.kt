package hu.bme.aut.crypto_casino_android.ui.presentation.slot

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.slot.GameHistoryResponse
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotMachineScreen(
    viewModel: SlotMachineViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Slot Machine") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with balance
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Casino Tokens",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${uiState.balance} CST",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tabs
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Slot Machine") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("History") }
                )
            }

            when (uiState.selectedTab) {
                0 -> SlotMachineTab(
                    uiState = uiState,
                    onIncreaseBet = { viewModel.increaseBet() },
                    onDecreaseBet = { viewModel.decreaseBet() },
                    onSpin = { viewModel.spin() }
                )
                1 -> GameHistoryTab(gameHistory = uiState.gameHistory)
            }
        }
    }
}

@Composable
fun SlotMachineTab(
    uiState: SlotMachineUiState,
    onIncreaseBet: () -> Unit,
    onDecreaseBet: () -> Unit,
    onSpin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Slot machine display
        SlotMachineDisplay(
            reels = uiState.reels,
            isSpinning = uiState.isSpinning,
            spinCount = uiState.spinCount
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Result message
        uiState.lastSpin?.let { lastSpin ->
            val message = if (lastSpin.winAmount > BigDecimal.ZERO) {
                "You won ${lastSpin.winAmount} CST!"
            } else {
                "Better luck next time!"
            }

            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = if (lastSpin.winAmount > BigDecimal.ZERO) Color.Green else Color.Red,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bet controls
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bet Amount",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDecreaseBet,
                        enabled = !uiState.isSpinning && uiState.currentBet > uiState.minBet
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Bet")
                    }

                    Text(
                        text = "${uiState.currentBet} CST",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IconButton(
                        onClick = onIncreaseBet,
                        enabled = !uiState.isSpinning && uiState.currentBet < uiState.maxBet
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Bet")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSpin,
                    enabled = !uiState.isSpinning && uiState.balance >= uiState.currentBet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (uiState.isSpinning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("SPIN", fontSize = 18.sp)
                    }
                }

                // Error message
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Min/Max bet info
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Min: ${uiState.minBet} CST | Max: ${uiState.maxBet} CST",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SlotMachineDisplay(
    reels: List<Int>,
    isSpinning: Boolean,
    spinCount: Int
) {
    // Remember the spin count to trigger animations
    var lastSpinCount by remember { mutableIntStateOf(spinCount) }
    var animatingReels by remember { mutableStateOf(false) }

    // Update animation state when spin count changes
    LaunchedEffect(spinCount) {
        if (spinCount > lastSpinCount) {
            animatingReels = true
            // Reset after animation completes
            kotlinx.coroutines.delay(1000)
            animatingReels = false
            lastSpinCount = spinCount
        }
    }

    // Create spinning animation
    val infiniteTransition = rememberInfiniteTransition(label = "reelSpin")
    val animationValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reelSpin"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            reels.forEach { value ->
                ReelDisplay(
                    value = value,
                    isSpinning = isSpinning || animatingReels,
                    animationValue = animationValue
                )
            }
        }
    }
}

@Composable
fun ReelDisplay(
    value: Int,
    isSpinning: Boolean,
    animationValue: Float
) {
    // Display a different number based on animation if spinning
    val displayValue = if (isSpinning) {
        ((value + (animationValue * 10).toInt()) % 10)
    } else {
        value
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayValue.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = when (displayValue) {
                7 -> Color(0xFFFFD700) // Gold color for 7
                else -> Color.White
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GameHistoryTab(gameHistory: List<GameHistoryResponse>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recent Games",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (gameHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No game history yet. Start playing!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(gameHistory) { game ->
                    GameHistoryItem(game)
                }
            }
        }
    }
}

@Composable
fun GameHistoryItem(game: GameHistoryResponse) {
    // Create a formatter for the date
    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    val formattedDate = game.timestamp.format(dateTimeFormatter)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Spin #${game.spinId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Show reel values
                game.reels?.let { reels ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        reels.forEach { value ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = value.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (value == 7) Color(0xFFFFD700) else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Bet: ${game.betAmount} CST",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Win: ${game.winAmount} CST",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (game.winAmount > BigDecimal.ZERO) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
