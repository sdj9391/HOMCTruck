package com.homc.homctruck.data.sourceremote

import com.homc.homctruck.data.contracts.LoadContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Load
import com.homc.homctruck.restapi.AppApiService
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.parse
import com.homc.homctruck.utils.parseApiMessage
import java.net.HttpURLConnection

class LoadRemoteDataSource(private val api: AppApiService) : LoadContract {

    override suspend fun addNewLoad(load: Load): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.addNewLoad(load)
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

    override suspend fun getLoadDetails(loadId: String): DataBound<Load> {
        val data: Load
        try {
            val response = api.getLoadDetails(loadId)
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

    override suspend fun getMyLoadList(materialKeyword: String?): DataBound<MutableList<Load>> {
        val data: MutableList<Load>
        try {
            val response = api.getMyLoadList(materialKeyword)
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

    override suspend fun getMyPastLoadList(materialKeyword: String?): DataBound<MutableList<Load>> {
        val data: MutableList<Load>
        try {
            val response = api.getMyPastLoadList(materialKeyword)
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

    override suspend fun findLoadList(
        fromCity: String, toCity: String, pickUpDate: Long, materialKeyword: String?
    ): DataBound<MutableList<Load>> {
        val data: MutableList<Load>
        try {
            val response = api.findLoadList(fromCity, toCity, pickUpDate, materialKeyword)
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

    override suspend fun updateLoadDetails(loadId: String, load: Load): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.updateLoadDetails(loadId, load)
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

    override suspend fun deleteLoad(loadId: String): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.deleteLoad(loadId)
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