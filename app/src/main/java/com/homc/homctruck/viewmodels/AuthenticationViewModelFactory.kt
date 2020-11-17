package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.data.repositories.AuthenticationRepository


class AuthenticationViewModelFactory(private val app: Application, private val repository: AuthenticationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AuthenticationViewModel(app, repository) as T
    }
}