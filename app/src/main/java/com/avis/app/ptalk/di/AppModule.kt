package com.avis.app.ptalk.di

import android.content.Context
import com.avis.app.ptalk.core.ble.BleClient
import com.avis.app.ptalk.core.ble.impl.PTalkBleClient
import com.avis.app.ptalk.domain.control.BleControlGateway
import com.avis.app.ptalk.domain.control.ControlGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Simplified AppModule for config-only app
 * No database, no auth, no websocket - just BLE
 */
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
}