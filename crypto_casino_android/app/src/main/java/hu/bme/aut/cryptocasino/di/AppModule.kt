package hu.bme.aut.cryptocasino.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.cryptocasino.data.local.TokenManager
import hu.bme.aut.cryptocasino.data.local.WalletKeyManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideTokenManager(
        @ApplicationContext context: Context,
    ): TokenManager = TokenManager(context)

    @Singleton
    @Provides
    fun provideWalletKeyManager(
        @ApplicationContext context: Context,
    ): WalletKeyManager = WalletKeyManager(context)
}
