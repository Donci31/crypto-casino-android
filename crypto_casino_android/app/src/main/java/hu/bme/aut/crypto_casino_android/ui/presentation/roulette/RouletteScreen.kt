package hu.bme.aut.crypto_casino_android.ui.presentation.roulette

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.roulette.BetType
import hu.bme.aut.crypto_casino_android.ui.theme.DraculaGold
import java.math.BigDecimal

@Composable
fun RouletteScreen(
    viewModel: RouletteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BalanceCard(balance = uiState.balance)

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.balance == BigDecimal.ZERO) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "No Balance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You need to deposit tokens to your vault before playing. Go to the Wallet tab to purchase and deposit tokens.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                RouletteWheelDisplay(
                    winningNumber = uiState.winningNumber,
                    isSpinning = uiState.isSpinning
                )

                Spacer(modifier = Modifier.height(16.dp))

                ResultDisplay(
                    winningNumber = uiState.winningNumber,
                    totalPayout = uiState.totalPayout
                )

                Spacer(modifier = Modifier.height(16.dp))

                ChipSelector(
                    selectedChipValue = uiState.selectedChipValue,
                    onChipSelected = viewModel::selectChipValue,
                    minBet = uiState.minBet
                )

                Spacer(modifier = Modifier.height(16.dp))

                BettingArea(
                    onBetPlaced = viewModel::placeBet
                )

                Spacer(modifier = Modifier.height(16.dp))

                PlacedBetsList(
                    bets = uiState.placedBets,
                    totalBetAmount = uiState.totalBetAmount,
                    onRemoveBet = viewModel::removeBet
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.error != null) {
                    ErrorMessage(error = uiState.error!!)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                ActionButtons(
                    canSpin = uiState.placedBets.isNotEmpty() && !uiState.isSpinning,
                    canClear = uiState.placedBets.isNotEmpty() && !uiState.isSpinning,
                    onSpin = viewModel::spin,
                    onClearBets = viewModel::clearAllBets,
                    onClearResult = viewModel::clearResult
                )
            }
        }
    }
}

@Composable
fun BalanceCard(balance: BigDecimal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vault Balance", style = MaterialTheme.typography.titleMedium)
            Text(
                "$balance CST",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RouletteWheelDisplay(winningNumber: Int?, isSpinning: Boolean) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            rotation.animateTo(
                targetValue = 720f,
                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
            )
        } else {
            rotation.snapTo(0f)
        }
    }

    Card(
        modifier = Modifier
            .size(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B0000))
                    .border(8.dp, DraculaGold, CircleShape)
                    .rotate(rotation.value),
                contentAlignment = Alignment.Center
            ) {
                if (!isSpinning && winningNumber != null) {
                    Text(
                        text = winningNumber.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            if (isSpinning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(120.dp),
                    color = DraculaGold
                )
            }
        }
    }
}

@Composable
fun ResultDisplay(winningNumber: Int?, totalPayout: BigDecimal?) {
    if (winningNumber != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (totalPayout != null && totalPayout > BigDecimal.ZERO)
                    Color(0xFF4CAF50)
                else
                    Color(0xFFE53935)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Winning Number: $winningNumber",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (totalPayout != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (totalPayout > BigDecimal.ZERO) "You Won: $totalPayout CST" else "No Win",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChipSelector(
    selectedChipValue: BigDecimal,
    onChipSelected: (BigDecimal) -> Unit,
    minBet: BigDecimal
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Select Chip Value", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf(
                    BigDecimal("10"),
                    BigDecimal("25"),
                    BigDecimal("50"),
                    BigDecimal("100"),
                    BigDecimal("250"),
                    BigDecimal("500")
                )) { chipValue ->
                    ChipButton(
                        value = chipValue,
                        isSelected = chipValue == selectedChipValue,
                        onClick = { onChipSelected(chipValue) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChipButton(value: BigDecimal, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) Color(0xFFFFD700)
                else Color(0xFFFF6B6B)
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFFFFA000) else Color(0xFF8B0000),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BettingArea(onBetPlaced: (BetType, Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Betting Area", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Straight Bets (36x)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items((0..36).toList()) { number ->
                    StraightBetButton(
                        number = number,
                        onClick = { onBetPlaced(BetType.STRAIGHT, number) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Outside Bets", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutsideBetButton(
                    text = "RED",
                    modifier = Modifier.weight(1f),
                    color = Color.Red,
                    onClick = { onBetPlaced(BetType.RED, 0) }
                )
                OutsideBetButton(
                    text = "BLACK",
                    modifier = Modifier.weight(1f),
                    color = Color.Black,
                    onClick = { onBetPlaced(BetType.BLACK, 0) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutsideBetButton(
                    text = "ODD",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.ODD, 0) }
                )
                OutsideBetButton(
                    text = "EVEN",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.EVEN, 0) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutsideBetButton(
                    text = "LOW\n1-18",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.LOW, 0) }
                )
                OutsideBetButton(
                    text = "HIGH\n19-36",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.HIGH, 0) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Dozens (3x)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutsideBetButton(
                    text = "1st\n1-12",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.DOZEN_FIRST, 0) }
                )
                OutsideBetButton(
                    text = "2nd\n13-24",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.DOZEN_SECOND, 0) }
                )
                OutsideBetButton(
                    text = "3rd\n25-36",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.DOZEN_THIRD, 0) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Columns (3x)", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutsideBetButton(
                    text = "Col 1",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.COLUMN_FIRST, 0) }
                )
                OutsideBetButton(
                    text = "Col 2",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.COLUMN_SECOND, 0) }
                )
                OutsideBetButton(
                    text = "Col 3",
                    modifier = Modifier.weight(1f),
                    onClick = { onBetPlaced(BetType.COLUMN_THIRD, 0) }
                )
            }
        }
    }
}

@Composable
fun StraightBetButton(number: Int, onClick: () -> Unit) {
    val redNumbers = listOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
    val backgroundColor = when {
        number == 0 -> Color(0xFF4CAF50)
        number in redNumbers -> Color(0xFFE53935)
        else -> Color(0xFF212121)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun OutsideBetButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color ?: MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PlacedBetsList(
    bets: List<PlacedBet>,
    totalBetAmount: BigDecimal,
    onRemoveBet: (Int) -> Unit
) {
    if (bets.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Placed Bets (${bets.size}/20)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Total: $totalBetAmount CST",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                bets.forEachIndexed { index, bet ->
                    BetItem(
                        bet = bet,
                        onRemove = { onRemoveBet(index) }
                    )
                    if (index < bets.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BetItem(bet: PlacedBet, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(bet.displayText, style = MaterialTheme.typography.bodyMedium)
                Text("${bet.amount} CST", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove bet")
            }
        }
    }
}

@Composable
fun ErrorMessage(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
    ) {
        Text(
            text = error,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFC62828),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ActionButtons(
    canSpin: Boolean,
    canClear: Boolean,
    onSpin: () -> Unit,
    onClearBets: () -> Unit,
    onClearResult: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onSpin,
            enabled = canSpin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
        ) {
            Text(
                "SPIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onClearBets,
                enabled = canClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear Bets")
            }
            OutlinedButton(
                onClick = onClearResult,
                modifier = Modifier.weight(1f)
            ) {
                Text("New Game")
            }
        }
    }
}
