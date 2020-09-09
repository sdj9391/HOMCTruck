package com.homc.homctruck.di

import com.homc.homctruck.HomcTruckApp
import com.homc.homctruck.di.modules.AppModule
import com.homc.homctruck.di.modules.ViewModelModule
import com.homc.homctruck.views.fragments.EditContractorProfileFragment
import com.homc.homctruck.views.fragments.EditUserProfileFragment
import com.homc.homctruck.views.fragments.LoginFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(app: HomcTruckApp)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: EditUserProfileFragment)
    fun inject(fragment: EditContractorProfileFragment)
}