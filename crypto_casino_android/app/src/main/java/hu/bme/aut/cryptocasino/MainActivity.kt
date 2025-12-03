package hu.bme.aut.cryptocasino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.aut.cryptocasino.ui.MainContent
import hu.bme.aut.cryptocasino.ui.theme.CryptocasinoandroidTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CryptocasinoandroidTheme {
                MainContent()
            }
        }
    }
}
