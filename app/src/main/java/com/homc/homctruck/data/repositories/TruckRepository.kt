package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.TruckContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.TruckRegistrationInfo
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.data.sourceremote.TruckRemoteDataSource
import com.homc.homctruck.restapi.DataBound

class TruckRepository(var dataSource: TruckRemoteDataSource) :
    TruckContract {
    override suspend fun addNewTruck(truck: Truck): DataBound<ApiMessage> {
        return dataSource.addNewTruck(truck)
    }

    override suspend fun getTruckDetails(truckId: String): DataBound<Truck> {
        return dataSource.getTruckDetails(truckId)
    }

    override suspend fun getMyTruckList(verificationStatus: String?, truckNumberKeyword: String?): DataBound<MutableList<Truck>> {
        return dataSource.getMyTruckList(verificationStatus, truckNumberKeyword)
    }

    override suspend fun getTruckList(verificationStatus: String?, truckNumberKeyword: String?): DataBound<MutableList<Truck>> {
        return dataSource.getTruckList(verificationStatus, truckNumberKeyword)
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

    override suspend fun getMyTruckRouteList(truckNumberKeyword: String?): DataBound<MutableList<TruckRoute>> {
        return dataSource.getMyTruckRouteList(truckNumberKeyword)
    }

    override suspend fun getMyPastTruckRouteList(truckNumberKeyword: String?): DataBound<MutableList<TruckRoute>> {
        return dataSource.getMyPastTruckRouteList(truckNumberKeyword)
    }

    override suspend fun findTruckRouteList(
        truckType: String,
        fromCity: String,
        toCity: String,
        fromDate: Long,
        toDate: Long,
        truckNumberKeyword: String?
    ): DataBound<MutableList<TruckRoute>> {
        return dataSource.findTruckRouteList(truckType, fromCity, toCity, fromDate, toDate, truckNumberKeyword)
    }

    override suspend fun updateTruckRouteDetails(
        truckRouteId: String, truckRoute: TruckRoute
    ): DataBound<ApiMessage> {
        return dataSource.updateTruckRouteDetails(truckRouteId, truckRoute)
    }

    override suspend fun deleteTruckRoute(truckRouteId: String): DataBound<ApiMessage> {
        return dataSource.deleteTruckRoute(truckRouteId)
    }

    override suspend fun addTruckRegistrationInfo(truckRegistrationInfo: TruckRegistrationInfo): DataBound<ApiMessage> {
        return dataSource.addTruckRegistrationInfo(truckRegistrationInfo)
    }

    override suspend fun getTruckRegistrationInfoList(): DataBound<MutableList<TruckRegistrationInfo>> {
        return dataSource.getTruckRegistrationInfoList()
    }

    override suspend fun updateTruckRegistrationInfo(
        truckRegistrationInfoId: String,
        truckRegistrationInfo: TruckRegistrationInfo
    ): DataBound<ApiMessage> {
        return dataSource.updateTruckRegistrationInfo(truckRegistrationInfoId, truckRegistrationInfo)
    }

    override suspend fun deleteTruckRegistrationInfo(truckRegistrationInfoId: String): DataBound<ApiMessage> {
        return dataSource.deleteTruckRegistrationInfo(truckRegistrationInfoId)
    }
}