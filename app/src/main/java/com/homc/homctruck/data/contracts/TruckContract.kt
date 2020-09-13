package com.homc.homctruck.data.contracts

import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.ResponsePostalAddress
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.User
import com.homc.homctruck.restapi.DataBound

interface TruckContract {
    suspend fun addNewTruck(truck: Truck): DataBound<ApiMessage>
    suspend fun getTruckDetails(truckId: String): DataBound<Truck>
    suspend fun getUserTruckList(): DataBound<MutableList<Truck>>
    suspend fun updateTruckDetails(truckId: String, truck: Truck): DataBound<ApiMessage>
    suspend fun deleteTruck(truckId: String): DataBound<ApiMessage>
}