package hu.bme.aut.cryptocasino.ui.presentation.roulette.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

@Composable
fun ChipButton(
    value: BigDecimal,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Color(0xFFFFD700)
                    } else {
                        Color(0xFFFF6B6B)
                    },
                ).border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) Color(0xFFFFA000) else Color(0xFF8B0000),
                    shape = CircleShape,
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

