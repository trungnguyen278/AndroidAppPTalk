package com.avis.app.ptalk.di

import android.content.Context
import com.avis.app.ptalk.core.ble.BleClient
import com.avis.app.ptalk.core.ble.impl.PTalkBleClient
import com.avis.app.ptalk.core.network.AuthInterceptor
import com.avis.app.ptalk.core.network.IoTPlatformApi
import com.avis.app.ptalk.core.network.TokenManager
import com.avis.app.ptalk.core.mqtt.PTalkMqttClient
import com.avis.app.ptalk.domain.control.BleControlGateway
import com.avis.app.ptalk.domain.control.ControlGateway
import com.avis.app.ptalk.domain.service.DeviceControlService
import com.avis.app.ptalk.domain.data.local.repo.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBle(@ApplicationContext ctx: Context): BleClient = PTalkBleClient(ctx)

    @Provides
    @Singleton
    fun provideGateway(ble: BleClient): ControlGateway =
        BleControlGateway { addr -> ble.connect(addr) }

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext ctx: Context): TokenManager = TokenManager(ctx)

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://iot.yirlodt.io.vn/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideIoTPlatformApi(retrofit: Retrofit): IoTPlatformApi {
        return retrofit.create(IoTPlatformApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: IoTPlatformApi, tokenManager: TokenManager): AuthRepository {
        return AuthRepository(api, tokenManager)
    }

    @Provides
    @Singleton
    fun provideMqttClient(): PTalkMqttClient {
        return PTalkMqttClient()
    }

    @Provides
    @Singleton
    fun provideDeviceControlService(mqttClient: PTalkMqttClient): DeviceControlService {
        return DeviceControlService(mqttClient)
    }
}