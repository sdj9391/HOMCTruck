package com.homc.homctruck.data.sourceremote

import com.homc.homctruck.data.contracts.TruckContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Truck
import com.homc.homctruck.data.models.TruckRoute
import com.homc.homctruck.restapi.AppApiService
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.restapi.PostalApiService
import com.homc.homctruck.utils.parse
import com.homc.homctruck.utils.parseApiMessage
import java.net.HttpURLConnection
import javax.inject.Inject

class TruckRemoteDataSource(private val api: AppApiService) : TruckContract {

    override suspend fun addNewTruck(truck: Truck): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.addNewTruck(truck)
            val code = response.code()
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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

    override suspend fun getMyTruckRouteList(): DataBound<MutableList<TruckRoute>> {
        val data: MutableList<TruckRoute>
        try {
            val response = api.getMyTruckRouteList()
            val code = response.code()
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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

    override suspend fun getMyPastTruckRouteList(): DataBound<MutableList<TruckRoute>> {
        val data: MutableList<TruckRoute>
        try {
            val response = api.getMyPastTruckRouteList()
            val code = response.code()
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
        truckType: String, fromCity: String, toCity: String, fromDate: Long, toDate: Long
    ): DataBound<MutableList<TruckRoute>> {
        val data: MutableList<TruckRoute>
        try {
            val response =
                api.findTruckRouteList(truckType, fromCity, toCity, fromDate, toDate)
            val code = response.code()
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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
            if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return DataBound.Retry(code)
            }
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