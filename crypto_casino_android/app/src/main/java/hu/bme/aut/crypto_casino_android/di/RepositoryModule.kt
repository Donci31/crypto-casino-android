package hu.bme.aut.crypto_casino_android.di

import dagger.Binds
import hu.bme.aut.crypto_casino_android.data.api.*
import hu.bme.aut.crypto_casino_android.data.repository.*
import hu.bme.aut.crypto_casino_android.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
}
