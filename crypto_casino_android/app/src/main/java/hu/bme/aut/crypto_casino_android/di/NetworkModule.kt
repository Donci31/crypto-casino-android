package hu.bme.aut.crypto_casino_android.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.crypto_casino_android.BuildConfig
import hu.bme.aut.crypto_casino_android.data.api.AuthApi
import hu.bme.aut.crypto_casino_android.data.api.BlockchainTransactionApi
import hu.bme.aut.crypto_casino_android.data.api.SlotMachineApiService
import hu.bme.aut.crypto_casino_android.data.api.UserApi
import hu.bme.aut.crypto_casino_android.data.api.WalletApi
import hu.bme.aut.crypto_casino_android.data.local.TokenManager
import hu.bme.aut.crypto_casino_android.data.util.LocalDateTimeAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val token = runBlocking { tokenManager.getToken.first() }
            val request = chain.request().newBuilder()
            if (!token.isNullOrEmpty()) {
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()
        return GsonConverterFactory.create(gson)
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Singleton
    @Provides
    fun provideTransactionApi(retrofit: Retrofit): BlockchainTransactionApi {
        return retrofit.create(BlockchainTransactionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSlotMachineApiService(retrofit: Retrofit): SlotMachineApiService {
        return retrofit.create(SlotMachineApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWalletApiService(retrofit: Retrofit): WalletApi {
        return retrofit.create(WalletApi::class.java)
    }
}
