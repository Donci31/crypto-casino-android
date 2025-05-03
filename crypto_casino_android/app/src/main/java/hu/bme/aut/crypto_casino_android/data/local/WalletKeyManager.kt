package hu.bme.aut.crypto_casino_android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.walletKeysDataStore: DataStore<Preferences> by preferencesDataStore("wallet_keys_prefs")

@Singleton
class WalletKeyManager @Inject constructor(private val context: Context) {

    companion object {
        private val PRIMARY_WALLET_KEY = stringPreferencesKey("primary_wallet_key")
        private const val WALLET_PREFIX = "wallet_key_"
    }

    val getAllWalletKeys: Flow<Map<String, String>> = context.walletKeysDataStore.data.map { preferences ->
        preferences.asMap()
            .filter { it.key.name.startsWith(WALLET_PREFIX) }
            .map { it.key.name.removePrefix(WALLET_PREFIX) to (it.value as String) }
            .toMap()
    }

    val getPrimaryWalletAddress: Flow<String?> = context.walletKeysDataStore.data.map { preferences ->
        preferences[PRIMARY_WALLET_KEY]
    }

    fun getWalletKey(address: String): Flow<String?> = context.walletKeysDataStore.data.map { preferences ->
        preferences[stringPreferencesKey(WALLET_PREFIX + address)]
    }

    suspend fun saveWalletKey(address: String, privateKey: String) {
        context.walletKeysDataStore.edit { preferences ->
            preferences[stringPreferencesKey(WALLET_PREFIX + address)] = privateKey
        }
    }

    suspend fun setPrimaryWallet(address: String) {
        context.walletKeysDataStore.edit { preferences ->
            preferences[PRIMARY_WALLET_KEY] = address
        }
    }

    suspend fun deleteWalletKey(address: String) {
        context.walletKeysDataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(WALLET_PREFIX + address))

            if (preferences[PRIMARY_WALLET_KEY] == address) {
                preferences.remove(PRIMARY_WALLET_KEY)
            }
        }
    }

    suspend fun clearAllWalletKeys() {
        context.walletKeysDataStore.edit { preferences ->
            preferences.asMap().keys
                .filter { it.name.startsWith(WALLET_PREFIX) || it.name == PRIMARY_WALLET_KEY.name }
                .forEach { preferences.remove(it) }
        }
    }
}
