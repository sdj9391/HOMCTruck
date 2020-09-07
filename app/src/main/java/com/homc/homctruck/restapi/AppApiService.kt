package com.homc.homctruck.restapi

import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.ResponsePostalAddress
import com.homc.homctruck.data.models.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Created by Admin on 1/3/2017.
 */
interface AppApiService {
    @GET("users/{userId}")
    suspend fun getUserDetail(@Path("userId") userId: String): Response<User>

    @POST("users")
    suspend fun addNewUserDetail(@Body user: User): Response<ApiMessage>

    @GET("users")
    suspend fun getUserList(): Response<MutableList<User>>

    @PUT("users/{userId}")
    suspend fun updateUserDetail(
        @Path("userId") userId: String,
        @Body user: User
    ): Response<ApiMessage>
}

interface PostalApiService {
    @GET("pincode/{pinCode}")
    suspend fun getPostalAddress(@Path("pinCode") pinCode: String): Response<MutableList<ResponsePostalAddress>>
}
