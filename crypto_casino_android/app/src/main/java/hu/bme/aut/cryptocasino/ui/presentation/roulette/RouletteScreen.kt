package hu.bme.aut.cryptocasino.ui.presentation.roulette

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.ActionButtons
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.BalanceCard
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.BettingArea
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.ChipSelector
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.ErrorMessage
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.PlacedBetsList
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.PrepareCommitmentCard
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.ResultDisplay
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.RouletteCommittedWaitingDisplay
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.RouletteWheelDisplay
import java.math.BigDecimal

@Composable
fun RouletteScreen(viewModel: RouletteViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BalanceCard(balance = uiState.balance)

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.balance == BigDecimal.ZERO) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                        ) {
                            Text(
                                text = "No Balance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text =
                                    "You need to deposit tokens to your vault before playing." +
                                        " Go to the Wallet tab to purchase and deposit tokens.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (uiState.gamePhase) {
                    RouletteGamePhase.PREPARING, RouletteGamePhase.COMMITTING -> {
                        RoulettePrepareScreen(
                            serverSeedHash = uiState.serverSeedHash,
                            tempGameId = uiState.tempGameId,
                            clientSeed = uiState.clientSeed,
                            totalBetAmount = uiState.totalBetAmount,
                            betCount = uiState.placedBets.size,
                            isCommitting = uiState.gamePhase == RouletteGamePhase.COMMITTING,
                            onProceedToCommit = viewModel::proceedToCommit,
                        )
                    }

                    RouletteGamePhase.COMMITTED_WAITING -> {
                        RouletteCommittedWaitingDisplay(
                            serverSeedHash = uiState.serverSeedHash,
                            clientSeed = uiState.clientSeed,
                            transactionHash = uiState.transactionHash,
                            blockNumber = uiState.blockNumber,
                            placedBets = uiState.placedBets,
                            totalBetAmount = uiState.totalBetAmount,
                            onReveal = viewModel::proceedToReveal,
                        )
                    }

                    else -> {
                        RouletteWheelDisplay(
                            winningNumber = uiState.winningNumber,
                            isSpinning = uiState.isSpinning,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ResultDisplay(
                            winningNumber = uiState.winningNumber,
                            totalPayout = uiState.totalPayout,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ChipSelector(
                            selectedChipValue = uiState.selectedChipValue,
                            onChipSelected = viewModel::selectChipValue,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BettingArea(
                            onBetPlaced = viewModel::placeBet,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PlacedBetsList(
                            bets = uiState.placedBets,
                            totalBetAmount = uiState.totalBetAmount,
                            onRemoveBet = viewModel::removeBet,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.error != null) {
                            ErrorMessage(error = uiState.error!!)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        ActionButtons(
                            canSpin = uiState.placedBets.isNotEmpty() && uiState.gamePhase == RouletteGamePhase.IDLE,
                            canClear = uiState.placedBets.isNotEmpty() && uiState.gamePhase == RouletteGamePhase.IDLE,
                            onSpin = viewModel::spin,
                            onClearBets = viewModel::clearAllBets,
                            onClearResult = viewModel::clearResult,
                        )
                    }
                }
            }
        }
    }
}
