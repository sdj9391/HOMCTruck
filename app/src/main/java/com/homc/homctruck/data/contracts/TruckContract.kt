package com.homc.homctruck.data.contracts

import com.homc.homctruck.data.models.*
import com.homc.homctruck.restapi.DataBound

interface TruckContract {
    suspend fun addNewTruck(truck: Truck): DataBound<ApiMessage>
    suspend fun getTruckDetails(truckId: String): DataBound<Truck>
    suspend fun getMyTruckList(): DataBound<MutableList<Truck>>
    suspend fun updateTruckDetails(truckId: String, truck: Truck): DataBound<ApiMessage>
    suspend fun deleteTruck(truckId: String): DataBound<ApiMessage>

    suspend fun addNewTruckRoute(truckRoute: TruckRoute): DataBound<ApiMessage>
    suspend fun getMyTruckRouteList(): DataBound<MutableList<TruckRoute>>
    suspend fun getMyPastTruckRouteList(): DataBound<MutableList<TruckRoute>>
    suspend fun updateTruckRouteDetails(truckRouteId: String, truckRoute: TruckRoute): DataBound<ApiMessage>
    suspend fun deleteTruckRoute(truckRouteId: String): DataBound<ApiMessage>
}