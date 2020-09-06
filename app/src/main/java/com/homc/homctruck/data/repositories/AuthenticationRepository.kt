package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.AuthenticationContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.ResponsePostalAddress
import com.homc.homctruck.data.models.User
import com.homc.homctruck.data.sourceremote.AuthenticationRemoteDataSource
import com.homc.homctruck.restapi.DataBound
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(var dataSource: AuthenticationRemoteDataSource) :
    AuthenticationContract {
    override suspend fun addNewUser(user: User): DataBound<ApiMessage> {
        return dataSource.addNewUser(user)
    }

    override suspend fun getUserDetails(userId: String): DataBound<User> {
        return dataSource.getUserDetails(userId)
    }

    override suspend fun getUserList(): DataBound<MutableList<User>> {
        return dataSource.getUserList()
    }

    override suspend fun updateUserDetails(userId: String, user: User): DataBound<ApiMessage> {
        return dataSource.updateUserDetails(userId, user)
    }

    override suspend fun getPostalAddress(pinCode: String): DataBound<ResponsePostalAddress> {
        return dataSource.getPostalAddress(pinCode)
    }
}