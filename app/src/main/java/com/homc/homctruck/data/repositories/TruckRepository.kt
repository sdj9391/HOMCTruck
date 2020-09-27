package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.TruckContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.data.sourceremote.TruckRemoteDataSource
import com.homc.homctruck.restapi.DataBound
import javax.inject.Inject

class TruckRepository @Inject constructor(var dataSource: TruckRemoteDataSource) :
    TruckContract {
    override suspend fun addNewTruck(truck: Truck): DataBound<ApiMessage> {
        return dataSource.addNewTruck(truck)
    }

    override suspend fun getTruckDetails(truckId: String): DataBound<Truck> {
        return dataSource.getTruckDetails(truckId)
    }

    override suspend fun getMyTruckList(): DataBound<MutableList<Truck>> {
        return dataSource.getMyTruckList()
    }

    override suspend fun updateTruckDetails(truckId: String, truck: Truck): DataBound<ApiMessage> {
        return dataSource.updateTruckDetails(truckId, truck)
    }

    override suspend fun deleteTruck(truckId: String): DataBound<ApiMessage> {
        return dataSource.deleteTruck(truckId)
    }

    override suspend fun addNewTruckRoute(truckRoute: TruckRoute): DataBound<ApiMessage> {
        return dataSource.addNewTruckRoute(truckRoute)
    }

    override suspend fun getMyTruckRouteList(): DataBound<MutableList<TruckRoute>> {
        return dataSource.getMyTruckRouteList()
    }

    override suspend fun updateTruckRouteDetails(
        truckRouteId: String, truckRoute: TruckRoute
    ): DataBound<ApiMessage> {
        return dataSource.updateTruckRouteDetails(truckRouteId, truckRoute)
    }

    override suspend fun deleteTruckRoute(truckRouteId: String): DataBound<ApiMessage> {
        return dataSource.deleteTruckRoute(truckRouteId)
    }
}