package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.AuthenticationContract
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.DataBound
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(var dataSource: AuthenticationRemoteDataSource) :
    AuthenticationContract {
    override suspend fun loginUser(user: User): DataBound<User> {
        return dataSource.loginUser(user)
    }

    override suspend fun getUserDetails(userId: Long): DataBound<User> {
        return dataSource.getUserDetails(userId)
    }
}