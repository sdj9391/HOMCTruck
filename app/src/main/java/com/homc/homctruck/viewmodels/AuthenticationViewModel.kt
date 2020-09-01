package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.restapi.DataBound
import kotlinx.coroutines.launch
import javax.inject.Inject


class AuthenticationViewModel
@Inject constructor(var app: Application, private val repository: AuthenticationRepository) :
    ViewModel() {
    fun loginUser(user: User): MutableLiveData<DataBound<User>> {
        val liveData = MutableLiveData<DataBound<User>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.createUserAccount(user)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.error, it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }

    fun getUserDetails(userId: String): MutableLiveData<DataBound<User>> {
        val liveData = MutableLiveData<DataBound<User>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getUserDetails(userId)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.error, it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }
}