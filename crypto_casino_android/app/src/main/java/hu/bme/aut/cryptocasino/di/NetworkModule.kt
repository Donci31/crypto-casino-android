package hu.bme.aut.cryptocasino.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.cryptocasino.BuildConfig
import hu.bme.aut.cryptocasino.data.api.AuthApi
import hu.bme.aut.cryptocasino.data.api.BlockchainTransactionApi
import hu.bme.aut.cryptocasino.data.api.DiceApiService
import hu.bme.aut.cryptocasino.data.api.RouletteApiService
import hu.bme.aut.cryptocasino.data.api.SlotMachineApiService
import hu.bme.aut.cryptocasino.data.api.StatsApi
import hu.bme.aut.cryptocasino.data.api.UserApi
import hu.bme.aut.cryptocasino.data.api.WalletApi
import hu.bme.aut.cryptocasino.data.interceptor.TokenRefreshInterceptor
import hu.bme.aut.cryptocasino.data.local.TokenManager
import hu.bme.aut.cryptocasino.data.util.LocalDateTimeAdapter
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
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }

    @Singleton
    @Provides
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val path = originalRequest.url.encodedPath

            val isAuthEndpoint =
                path.contains("/auth/login") ||
                    path.contains("/auth/register") ||
                    path.contains("/auth/refresh")

            if (isAuthEndpoint) {
                android.util.Log.d("AuthInterceptor", "Skipping Authorization header for auth endpoint: $path")
                return@Interceptor chain.proceed(originalRequest)
            }

            val token = runBlocking { tokenManager.getAccessToken.first() }
            android.util.Log.d(
                "AuthInterceptor",
                "Token retrieved: ${if (token.isNullOrEmpty()) "EMPTY/NULL" else "EXISTS (length=${token.length})"}",
            )
            val request = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                android.util.Log.d("AuthInterceptor", "Adding Authorization header for $path")
                request.addHeader("Authorization", "Bearer $token")
            } else {
                android.util.Log.w("AuthInterceptor", "NO TOKEN - Proceeding without Authorization header for $path")
            }
            chain.proceed(request.build())
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(tokenRefreshInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        val gson =
            GsonBuilder()
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
                .create()
        return GsonConverterFactory.create(gson)
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()

    @Singleton
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Singleton
    @Provides
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Singleton
    @Provides
    fun provideTransactionApi(retrofit: Retrofit): BlockchainTransactionApi = retrofit.create(BlockchainTransactionApi::class.java)

    @Provides
    @Singleton
    fun provideSlotMachineApiService(retrofit: Retrofit): SlotMachineApiService = retrofit.create(SlotMachineApiService::class.java)

    @Provides
    @Singleton
    fun provideWalletApiService(retrofit: Retrofit): WalletApi = retrofit.create(WalletApi::class.java)

    @Provides
    @Singleton
    fun provideDiceApiService(retrofit: Retrofit): DiceApiService = retrofit.create(DiceApiService::class.java)

    @Provides
    @Singleton
    fun provideRouletteApiService(retrofit: Retrofit): RouletteApiService = retrofit.create(RouletteApiService::class.java)

    @Provides
    @Singleton
    fun provideStatsApi(retrofit: Retrofit): StatsApi = retrofit.create(StatsApi::class.java)
}
