package com.homc.homctruck.di.modules

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.repositories.LoadRepository
import com.homc.homctruck.data.repositories.TruckRepository
import com.homc.homctruck.viewmodels.*
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ViewModelModule {

    @Provides
    @IntoMap
    @ViewModelKey(AuthenticationViewModel::class)
    fun authenticationViewModel(app: Application, repo: AuthenticationRepository): ViewModel {
        return AuthenticationViewModel(app, repo)
    }

    @Provides
    @IntoMap
    @ViewModelKey(TruckViewModel::class)
    fun truckViewModel(app: Application, repo: TruckRepository): ViewModel {
        return TruckViewModel(app, repo)
    }

    @Provides
    @IntoMap
    @ViewModelKey(LoadViewModel::class)
    fun loadViewModel(app: Application, repo: LoadRepository): ViewModel {
        return LoadViewModel(app, repo)
    }

    @Provides
    fun getFactory(map: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>): ViewModelProvider.Factory {
        return ViewModelFactoryProvider(map)
    }
}