package hu.bme.aut.crypto_casino_android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.crypto_casino_android.data.api.AuthApi
import hu.bme.aut.crypto_casino_android.data.api.BlockchainTransactionApi
import hu.bme.aut.crypto_casino_android.data.api.UserApi
import hu.bme.aut.crypto_casino_android.data.api.WalletApi
import hu.bme.aut.crypto_casino_android.data.local.TokenManager
import hu.bme.aut.crypto_casino_android.data.local.WalletKeyManager
import hu.bme.aut.crypto_casino_android.data.repository.AuthRepository
import hu.bme.aut.crypto_casino_android.data.repository.BlockchainTransactionRepository
import hu.bme.aut.crypto_casino_android.data.repository.UserRepository
import hu.bme.aut.crypto_casino_android.data.repository.WalletRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepository(authApi, tokenManager)
    }

    @Singleton
    @Provides
    fun provideUserRepository(userApi: UserApi): UserRepository {
        return UserRepository(userApi)
    }

    @Singleton
    @Provides
    fun provideTransactionRepository(transactionApi: BlockchainTransactionApi): BlockchainTransactionRepository {
        return BlockchainTransactionRepository(transactionApi)
    }

    @Singleton
    @Provides
    fun provideWalletRepository(
        walletApiService: WalletApi,
        walletKeyManager: WalletKeyManager
    ): WalletRepository {
        return WalletRepository(walletApiService, walletKeyManager)
    }
}
