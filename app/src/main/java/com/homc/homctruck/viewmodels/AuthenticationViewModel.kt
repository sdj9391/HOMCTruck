package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.ResponsePostalAddress
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.repositories.AuthenticationRepository
import com.homc.homctruck.restapi.DataBound
import kotlinx.coroutines.launch


class AuthenticationViewModel(
    var app: Application,
    private val repository: AuthenticationRepository
) :
    ViewModel() {



    fun addNewUser(user: User): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.addNewUser(user)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                        is DataBound.Retry -> {
                            liveData.value = DataBound.Retry(it.code)
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
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                        is DataBound.Retry -> {
                            liveData.value = DataBound.Retry(it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }

    fun getUserList(verificationStatus: String, userNameKeyword: String? = null): MutableLiveData<DataBound<MutableList<User>>> {
        val liveData = MutableLiveData<DataBound<MutableList<User>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getUserList(verificationStatus, userNameKeyword)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                        is DataBound.Retry -> {
                            liveData.value = DataBound.Retry(it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }

    fun updateUserDetails(userId: String, user: User): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.updateUserDetails(userId, user)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                        is DataBound.Retry -> {
                            liveData.value = DataBound.Retry(it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }

    fun getPostalAddress(pinCode: String): MutableLiveData<DataBound<ResponsePostalAddress>> {
        val liveData = MutableLiveData<DataBound<ResponsePostalAddress>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getPostalAddress(pinCode)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                        is DataBound.Retry -> {
                            liveData.value = DataBound.Retry(it.code)
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