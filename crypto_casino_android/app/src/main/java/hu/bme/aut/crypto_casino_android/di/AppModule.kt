package hu.bme.aut.crypto_casino_android.di

import android.content.Context
import hu.bme.aut.crypto_casino_android.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.crypto_casino_android.data.local.WalletKeyManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Singleton
    @Provides
    fun provideWalletKeyManager(@ApplicationContext context: Context): WalletKeyManager {
        return WalletKeyManager(context)
    }
}
