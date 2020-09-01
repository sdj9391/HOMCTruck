package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.AuthenticationContract
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.DataBound
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(var dataSource: AuthenticationRemoteDataSource) :
    AuthenticationContract {
    override suspend fun createUserAccount(user: User): DataBound<User> {
        return dataSource.createUserAccount(user)
    }

    override suspend fun getUserDetails(userId: String): DataBound<User> {
        return dataSource.getUserDetails(userId)
    }
}