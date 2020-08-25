package com.homc.homctruck.data.contracts

import com.homc.homctruck.data.models.User
import com.homc.homctruck.restapi.DataBound

interface AuthenticationContract {
    suspend fun loginUser(user: User): DataBound<User>
    suspend fun getUserDetails(userId: Long): DataBound<User>
}