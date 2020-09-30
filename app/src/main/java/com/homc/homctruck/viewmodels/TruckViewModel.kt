package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.data.repositories.TruckRepository
import com.homc.homctruck.restapi.DataBound
import kotlinx.coroutines.launch
import javax.inject.Inject


class TruckViewModel
@Inject constructor(var app: Application, private val repository: TruckRepository) :
    ViewModel() {

    fun addNewTruck(truck: Truck): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.addNewTruck(truck)

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

    fun getMyTruckList(): MutableLiveData<DataBound<MutableList<Truck>>> {
        val liveData = MutableLiveData<DataBound<MutableList<Truck>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getMyTruckList()

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

    fun updateTruckDetails(truckId: String, truck: Truck): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.updateTruckDetails(truckId, truck)

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


    fun deleteTruck(truckId: String): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.deleteTruck(truckId)

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

    fun addNewTruckRoute(truckRoute: TruckRoute): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.addNewTruckRoute(truckRoute)

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

    fun getMyTruckRouteList(): MutableLiveData<DataBound<MutableList<TruckRoute>>> {
        val liveData = MutableLiveData<DataBound<MutableList<TruckRoute>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getMyTruckRouteList()

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

    fun getMyPastTruckRouteList(): MutableLiveData<DataBound<MutableList<TruckRoute>>> {
        val liveData = MutableLiveData<DataBound<MutableList<TruckRoute>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getMyPastTruckRouteList()

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

    fun updateTruckRouteDetails(
        truckRouteId: String,
        truckRoute: TruckRoute
    ): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.updateTruckRouteDetails(truckRouteId, truckRoute)

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


    fun deleteTruckRoute(truckRouteId: String): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.deleteTruckRoute(truckRouteId)

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