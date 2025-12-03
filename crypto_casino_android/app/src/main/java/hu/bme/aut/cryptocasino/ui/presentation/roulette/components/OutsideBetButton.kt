package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OutsideBetButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = color ?: MaterialTheme.colorScheme.primary,
            ),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

