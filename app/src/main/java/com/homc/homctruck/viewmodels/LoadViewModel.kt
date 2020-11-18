package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.data.repositories.LoadRepository
import com.homc.homctruck.restapi.DataBound
import kotlinx.coroutines.launch


class LoadViewModel(var app: Application, private val repository: LoadRepository) :
    ViewModel() {

    fun addNewLoad(load: Load): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.addNewLoad(load)

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

    fun getMyLoadList(materialKeyword: String? = null): MutableLiveData<DataBound<MutableList<Load>>> {
        val liveData = MutableLiveData<DataBound<MutableList<Load>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getMyLoadList(materialKeyword)

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

    fun getMyPastLoadList(materialKeyword: String? = null): MutableLiveData<DataBound<MutableList<Load>>> {
        val liveData = MutableLiveData<DataBound<MutableList<Load>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getMyPastLoadList(materialKeyword)

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

    fun findLoadList(
        fromCity: String, toCity: String, pickUpDate: Long, materialKeyword: String? = null
    ): MutableLiveData<DataBound<MutableList<Load>>> {
        val liveData = MutableLiveData<DataBound<MutableList<Load>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound =
                    repository.findLoadList(fromCity, toCity, pickUpDate, materialKeyword)

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

    fun updateLoadDetails(loadId: String, load: Load): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.updateLoadDetails(loadId, load)

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


    fun deleteLoad(loadId: String): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.deleteLoad(loadId)

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