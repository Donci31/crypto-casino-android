package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessage(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
    ) {
        Text(
            text = error,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFC62828),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

