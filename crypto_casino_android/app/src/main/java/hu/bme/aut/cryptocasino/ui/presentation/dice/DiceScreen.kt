package hu.bme.aut.cryptocasino.ui.presentation.dice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.BalanceCard
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.BetAmountControls
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.BetTypeSelector
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.ClientSeedInput
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.CommittedWaitingDisplay
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.DiceDisplay
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.PredictionSelector
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.PrepareCommitmentCard
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.ResultDisplay
import hu.bme.aut.cryptocasino.ui.presentation.dice.components.VerificationDisplay

@Composable
fun DiceScreen(viewModel: DiceViewModel = hiltViewModel()) {
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

                if (uiState.balance == java.math.BigDecimal.ZERO) {
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
                    DiceGamePhase.PREPARING, DiceGamePhase.COMMITTING -> {
                        DicePrepareScreen(
                            serverSeedHash = uiState.serverSeedHash,
                            tempGameId = uiState.tempGameId,
                            clientSeed = uiState.clientSeed,
                            betAmount = uiState.currentBet,
                            prediction = uiState.prediction,
                            betType = uiState.betType,
                            isCommitting = uiState.gamePhase == DiceGamePhase.COMMITTING,
                            onProceedToCommit = viewModel::proceedToCommit,
                        )
                    }

                    DiceGamePhase.COMMITTED_WAITING -> {
                        CommittedWaitingDisplay(
                            serverSeedHash = uiState.serverSeedHash,
                            clientSeed = uiState.clientSeed,
                            transactionHash = uiState.transactionHash,
                            blockNumber = uiState.blockNumber,
                            betAmount = uiState.currentBet,
                            prediction = uiState.prediction,
                            betType = uiState.betType,
                            onReveal = viewModel::proceedToReveal,
                        )
                    }

                    DiceGamePhase.VERIFICATION -> {
                        VerificationDisplay(
                            result = uiState.result,
                            won = uiState.won,
                            payout = uiState.payout,
                            serverSeedHash = uiState.serverSeedHash,
                            serverSeed = uiState.serverSeed,
                            clientSeed = uiState.clientSeed,
                            onContinue = viewModel::continueAfterVerification,
                        )
                    }

                    else -> {
                        DiceDisplay(
                            result = uiState.result,
                            isRolling = uiState.isCreatingGame || uiState.isSettling,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ResultDisplay(
                            result = uiState.result,
                            won = uiState.won,
                            payout = uiState.payout,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        BetTypeSelector(
                            selectedBetType = uiState.betType,
                            onBetTypeChange = viewModel::setBetType,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PredictionSelector(
                            prediction = uiState.prediction,
                            betType = uiState.betType,
                            onPredictionChange = viewModel::setPrediction,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BetAmountControls(
                            currentBet = uiState.currentBet,
                            minBet = uiState.minBet,
                            maxBet = uiState.maxBet,
                            onIncrease = viewModel::increaseBet,
                            onDecrease = viewModel::decreaseBet,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ClientSeedInput(
                            clientSeed = uiState.clientSeed,
                            onSeedChange = viewModel::updateClientSeed,
                            onGenerateNew = viewModel::generateNewClientSeed,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.clearResult()
                                viewModel.playGame()
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            enabled = uiState.gamePhase == DiceGamePhase.IDLE || uiState.gamePhase == DiceGamePhase.REVEALED,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            if (uiState.isCreatingGame || uiState.isSettling) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                )
                            } else {
                                Text("ROLL DICE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
