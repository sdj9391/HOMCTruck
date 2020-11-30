package com.homc.homctruck.data.sourceremote

import com.homc.homctruck.data.contracts.TruckContract
import com.homc.homctruck.data.models.*
import com.homc.homctruck.restapi.AppApiService
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.parse
import com.homc.homctruck.utils.parseApiMessage
import java.net.HttpURLConnection

class TruckRemoteDataSource(private val api: AppApiService) : TruckContract {

    override suspend fun addNewTruck(truck: Truck): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.addNewTruck(truck)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun getTruckDetails(truckId: String): DataBound<Truck> {
        val data: Truck
        try {
            val response = api.getTruckDetails(truckId)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun getMyTruckList(verificationStatus: String?, truckNumberKeyword: String?): DataBound<MutableList<Truck>> {
        val data: MutableList<Truck>
        try {
            val response = api.getMyTruckList(verificationStatus, truckNumberKeyword)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun getTruckList(verificationStatus: String?, truckNumberKeyword: String?): DataBound<MutableList<Truck>> {
        val data: MutableList<Truck>
        try {
            val response = api.getTruckList(verificationStatus, truckNumberKeyword)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun updateTruckDetails(truckId: String, truck: Truck): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.updateTruckDetails(truckId, truck)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun deleteTruck(truckId: String): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.deleteTruck(truckId)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun addNewTruckRoute(truckRoute: TruckRoute): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.addNewTruckRoute(truckRoute)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun getMyTruckRouteList(truckNumberKeyword: String?): DataBound<MutableList<TruckRoute>> {
        val data: MutableList<TruckRoute>
        try {
            val response = api.getMyTruckRouteList(truckNumberKeyword)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun getMyPastTruckRouteList(truckNumberKeyword: String?): DataBound<MutableList<TruckRoute>> {
        val data: MutableList<TruckRoute>
        try {
            val response = api.getMyPastTruckRouteList(truckNumberKeyword)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun findTruckRouteList(
        truckType: String,
        fromCity: String,
        toCity: String,
        fromDate: Long,
        toDate: Long,
        truckNumberKeyword: String?
    ): DataBound<MutableList<TruckRoute>> {
        val data: MutableList<TruckRoute>
        try {
            val response =
                api.findTruckRouteList(truckType, fromCity, toCity, fromDate, toDate, truckNumberKeyword)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun updateTruckRouteDetails(
        truckRouteId: String,
        truckRoute: TruckRoute
    ): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.updateTruckRouteDetails(truckRouteId, truckRoute)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun deleteTruckRoute(truckRouteId: String): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.deleteTruckRoute(truckRouteId)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun addTruckRegistrationInfo(truckRegistrationInfo: TruckRegistrationInfo): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.addTruckRegistrationInfo(truckRegistrationInfo)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun getTruckRegistrationInfoList(): DataBound<MutableList<TruckRegistrationInfo>> {
        val data: MutableList<TruckRegistrationInfo>
        try {
            val response = api.getTruckRegistrationInfoList()
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun updateTruckRegistrationInfo(truckRegistrationInfoId: String, truckRegistrationInfo: TruckRegistrationInfo): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.updateTruckRegistrationInfo(truckRegistrationInfoId, truckRegistrationInfo)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun deleteTruckRegistrationInfo(truckRegistrationInfoId: String): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.deleteTruckRegistrationInfo(truckRegistrationInfoId)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val responseData = response.body()
                if (responseData == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = responseData
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }
}