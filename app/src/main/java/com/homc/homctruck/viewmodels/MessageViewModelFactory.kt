package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.homc.homctruck.data.repositories.MessageRepository

class MessageViewModelFactory(
    private val app: Application,
    private val repository: MessageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MessageViewModel(app, repository) as T
    }
}