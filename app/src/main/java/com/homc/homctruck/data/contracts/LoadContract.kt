package com.homc.homctruck.data.contracts

import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.restapi.DataBound

interface LoadContract {
    suspend fun addNewLoad(load: Load): DataBound<ApiMessage>
    suspend fun getLoadDetails(loadId: String): DataBound<Load>
    suspend fun getMyLoadList(): DataBound<MutableList<Load>>
    suspend fun getMyPastLoadList(): DataBound<MutableList<Load>>
    suspend fun updateLoadDetails(loadId: String, load: Load): DataBound<ApiMessage>
    suspend fun deleteLoad(loadId: String): DataBound<ApiMessage>
}