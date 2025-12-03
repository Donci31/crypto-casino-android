package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButtons(
    canSpin: Boolean,
    canClear: Boolean,
    onSpin: () -> Unit,
    onClearBets: () -> Unit,
    onClearResult: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onSpin,
            enabled = canSpin,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
        ) {
            Text(
                "SPIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onClearBets,
                enabled = canClear,
                modifier = Modifier.weight(1f),
            ) {
                Text("Clear Bets")
            }
            OutlinedButton(
                onClick = onClearResult,
                modifier = Modifier.weight(1f),
            ) {
                Text("New Game")
            }
        }
    }
}
