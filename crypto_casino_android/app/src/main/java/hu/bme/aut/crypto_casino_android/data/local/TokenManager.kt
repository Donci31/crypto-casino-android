package hu.bme.aut.crypto_casino_android.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("crypto_casino_prefs")

@Singleton
class TokenManager @Inject constructor(private val context: Context) {

    companion object {
        private const val TAG = "TokenManager"
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val getAccessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }

    val getRefreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        Log.d(TAG, "Saving access and refresh tokens")
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveAccessToken(accessToken: String) {
        Log.d(TAG, "Saving access token")
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun deleteTokens() {
        Log.d(TAG, "Deleting all tokens")
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    suspend fun getAccessTokenValue(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }.first()
    }

    suspend fun getRefreshTokenValue(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }.first()
    }
}
