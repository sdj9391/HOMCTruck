package com.homc.homctruck.di.modules

import android.app.Application
import com.homc.homctruck.restapi.AppApiInstance
import com.homc.homctruck.restapi.AppApiService
import com.homc.homctruck.utils.AppConfig
import com.homc.homctruck.utils.TemporaryCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(var app: Application) {

    @Provides
    @Singleton
    internal fun provideApp(): Application {
        return app
    }

    @Provides
    internal fun provideSocialApiService(): AppApiService {
        return AppApiInstance.api
    }

    @Provides
    @Singleton
    internal fun provideHealthPoleConfig(): AppConfig {
        return AppConfig
    }

    @Provides
    internal fun providesTemporaryCache(): TemporaryCache {
        return TemporaryCache
    }
}
