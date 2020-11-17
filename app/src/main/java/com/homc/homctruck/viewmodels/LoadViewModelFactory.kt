package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.data.repositories.LoadRepository


class LoadViewModelFactory(private val app: Application, private val repository: LoadRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  LoadViewModel(app, repository) as T
    }
}