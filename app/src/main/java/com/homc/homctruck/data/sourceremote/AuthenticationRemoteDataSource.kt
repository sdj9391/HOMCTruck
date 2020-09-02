package com.homc.homctruck.data.sourceremote

import com.homc.homctruck.data.contracts.AuthenticationContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.User
import com.homc.homctruck.restapi.AppApiService
import com.homc.homctruck.restapi.DataBound
import com.homc.homctruck.utils.parse
import com.homc.homctruck.utils.parseApiMessage
import javax.inject.Inject

class AuthenticationRemoteDataSource @Inject constructor(private val api: AppApiService) :
    AuthenticationContract {

    override suspend fun addNewUser(user: User): DataBound<ApiMessage> {
        val data: ApiMessage
        try {
            val response = api.addNewUserDetail(user)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val meetingDetails = response.body()
                if (meetingDetails == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = meetingDetails
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }

    override suspend fun getUserDetails(userId: String): DataBound<User> {
        val data: User
        try {
            val response = api.getUserDetail(userId)
            val code = response.code()
            if (!response.isSuccessful) {
                val message = parseApiMessage(response).message
                return if (message.isNullOrBlank()) {
                    DataBound.Error(null, parse(code))
                } else {
                    DataBound.Error(message, code)
                }
            } else {
                val meetingDetails = response.body()
                if (meetingDetails == null) {
                    val message = parseApiMessage(response).message
                    return DataBound.Error(message, code)
                } else {
                    data = meetingDetails
                }
            }
        } catch (t: Throwable) {
            throw t
        }

        return DataBound.Success(data)
    }
}