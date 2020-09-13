package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.AuthenticationContract
import com.homc.homctruck.data.contracts.TruckContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.ResponsePostalAddress
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
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

    override suspend fun getUserTruckList(): DataBound<MutableList<Truck>> {
        return dataSource.getUserTruckList()
    }

    override suspend fun updateTruckDetails(truckId: String, truck: Truck): DataBound<ApiMessage> {
        return dataSource.updateTruckDetails(truckId, truck)
    }

    override suspend fun deleteTruck(truckId: String): DataBound<ApiMessage> {
        return dataSource.deleteTruck(truckId)
    }
}