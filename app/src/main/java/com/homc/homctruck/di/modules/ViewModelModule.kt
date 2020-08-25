package com.homc.homctruck.di.modules

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.viewmodels.AuthenticationViewModel
import com.homc.homctruck.viewmodels.ViewModelFactoryProvider
import com.homc.homctruck.viewmodels.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ViewModelModule {

    @Provides
    @IntoMap
    @ViewModelKey(AuthenticationViewModel::class)
    fun authenticationViewModel(app: Application, clinicRepo: AuthenticationRepository): ViewModel {
        return AuthenticationViewModel(app, clinicRepo)
    }

    @Provides
    fun getFactory(map: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>): ViewModelProvider.Factory {
        return ViewModelFactoryProvider(map)
    }
}