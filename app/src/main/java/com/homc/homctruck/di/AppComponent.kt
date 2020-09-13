package com.homc.homctruck.di

import com.homc.homctruck.HomcTruckApp
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.views.fragments.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(app: HomcTruckApp)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: EditUserProfileFragment)
    fun inject(fragment: EditContractorProfileFragment)
    fun inject(fragment: UserTruckListFragment)
    fun inject(fragment: AddTruckFragment)
}