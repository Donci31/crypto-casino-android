package hu.bme.aut.cryptocasino.ui.presentation.roulette

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.cryptocasino.ui.presentation.roulette.components.PrepareCommitmentCard

@Composable
fun RoulettePrepareScreen(
    serverSeedHash: String?,
    tempGameId: String?,
    clientSeed: String,
    totalBetAmount: java.math.BigDecimal,
    betCount: Int,
    isCommitting: Boolean,
    onProceedToCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Backend Commitment Received",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "The backend has committed to a result hash before seeing your client seed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrepareCommitmentCard(
                serverSeedHash = serverSeedHash,
                tempGameId = tempGameId,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Client Seed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = clientSeed,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Game Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Total Bet: ${totalBetAmount} tokens",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Number of Bets: $betCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onProceedToCommit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                enabled = !isCommitting,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                if (isCommitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text("COMMITTING TO BLOCKCHAIN...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("PROCEED TO COMMIT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "By proceeding, your client seed will be sent to create the game on blockchain.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}
