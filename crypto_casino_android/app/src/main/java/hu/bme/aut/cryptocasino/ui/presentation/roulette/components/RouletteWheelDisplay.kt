package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.cryptocasino.ui.theme.DraculaGold

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