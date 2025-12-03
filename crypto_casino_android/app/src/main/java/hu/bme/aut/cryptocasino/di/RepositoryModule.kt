package hu.bme.aut.cryptocasino.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.cryptocasino.data.api.AuthApi
import hu.bme.aut.cryptocasino.data.api.BlockchainTransactionApi
import hu.bme.aut.cryptocasino.data.api.UserApi
import hu.bme.aut.cryptocasino.data.api.WalletApi
import hu.bme.aut.cryptocasino.data.local.TokenManager
import hu.bme.aut.cryptocasino.data.local.WalletKeyManager
import hu.bme.aut.cryptocasino.data.repository.AuthRepository
import hu.bme.aut.cryptocasino.data.repository.BlockchainTransactionRepository
import hu.bme.aut.cryptocasino.data.repository.UserRepository
import hu.bme.aut.cryptocasino.data.repository.WalletRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager,
    ): AuthRepository = AuthRepository(authApi, tokenManager)

    @Singleton
    @Provides
    fun provideUserRepository(userApi: UserApi): UserRepository = UserRepository(userApi)

    @Singleton
    @Provides
    fun provideTransactionRepository(transactionApi: BlockchainTransactionApi): BlockchainTransactionRepository =
        BlockchainTransactionRepository(transactionApi)

    @Singleton
    @Provides
    fun provideWalletRepository(
        walletApiService: WalletApi,
        walletKeyManager: WalletKeyManager,
    ): WalletRepository = WalletRepository(walletApiService, walletKeyManager)
}
