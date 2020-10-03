package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.LoadContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.data.sourceremote.LoadRemoteDataSource
import com.homc.homctruck.restapi.DataBound
import javax.inject.Inject

class LoadRepository @Inject constructor(var dataSource: LoadRemoteDataSource) :
    LoadContract {
    override suspend fun addNewLoad(load: Load): DataBound<ApiMessage> {
        return dataSource.addNewLoad(load)
    }

    override suspend fun getLoadDetails(loadId: String): DataBound<Load> {
        return dataSource.getLoadDetails(loadId)
    }

    override suspend fun getMyLoadList(): DataBound<MutableList<Load>> {
        return dataSource.getMyLoadList()
    }

    override suspend fun getMyPastLoadList(): DataBound<MutableList<Load>> {
        return dataSource.getMyPastLoadList()
    }

    override suspend fun findLoadList(
        toCity: String, fromCity: String, pickUpDate: Long
    ): DataBound<MutableList<Load>> {
        return dataSource.findLoadList(toCity, fromCity, pickUpDate)
    }

    override suspend fun updateLoadDetails(loadId: String, load: Load): DataBound<ApiMessage> {
        return dataSource.updateLoadDetails(loadId, load)
    }

    override suspend fun deleteLoad(loadId: String): DataBound<ApiMessage> {
        return dataSource.deleteLoad(loadId)
    }
}