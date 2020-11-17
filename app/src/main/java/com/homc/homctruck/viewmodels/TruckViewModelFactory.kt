package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.repositories.LoadRepository
import com.homc.homctruck.data.repositories.TruckRepository


class TruckViewModelFactory(private val app: Application, private val repository: TruckRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  TruckViewModel(app, repository) as T
    }
}