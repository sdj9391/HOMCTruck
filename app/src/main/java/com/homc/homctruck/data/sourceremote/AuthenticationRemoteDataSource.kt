package com.homc.homctruck.data.sourceremote

import com.homc.homctruck.data.contracts.AuthenticationContract
import com.homc.homctruck.data.models.User
import com.homc.homctruck.restapi.AppApiService
import com.homc.homctruck.restapi.DataBound
import javax.inject.Inject

class AuthenticationRemoteDataSource @Inject constructor(private val api: AppApiService) : AuthenticationContract {

    override suspend fun loginUser(user: User): DataBound<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserDetails(userId: Long): DataBound<User> {
        TODO("Not yet implemented")
    }
}