package com.avis.app.ptalk.di

import android.content.Context
import androidx.room.Room
import com.avis.app.ptalk.domain.data.local.AppDatabase
import com.avis.app.ptalk.domain.data.local.dao.DeviceDao
import com.avis.app.ptalk.domain.data.local.repo.DeviceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ptalk_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(database: AppDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(dao: DeviceDao): DeviceRepository {
        return DeviceRepository(dao)
    }
}
