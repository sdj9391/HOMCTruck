package com.homc.homctruck.data.contracts

import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.User
import com.homc.homctruck.restapi.DataBound

interface AuthenticationContract {
    suspend fun addNewUser(user: User): DataBound<ApiMessage>
    suspend fun getUserDetails(userId: String): DataBound<User>
}