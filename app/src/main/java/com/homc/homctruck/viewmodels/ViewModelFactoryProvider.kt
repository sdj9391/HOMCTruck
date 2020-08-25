package com.homc.homctruck.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactoryProvider(val map : Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return (map[modelClass] ?: error("viewmodel map returned null for class type: $modelClass")) as T
    }
}