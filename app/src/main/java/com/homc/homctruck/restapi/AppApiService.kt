package com.homc.homctruck.restapi

import com.homc.homctruck.data.models.*
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

    @GET("trucks/{truckId}")
    suspend fun getTruckDetails(@Path("truckId") truckId: String): Response<Truck>

    @POST("trucks")
    suspend fun addNewTruck(@Body truck: Truck): Response<ApiMessage>

    @GET("trucks")
    suspend fun getUserTruckList(): Response<MutableList<Truck>>

    @PUT("trucks/{truckId}")
    suspend fun updateTruckDetails(
        @Path("truckId") truckId: String,
        @Body truck: Truck
    ): Response<ApiMessage>

    @DELETE("trucks/{truckId}")
    suspend fun deleteTruck(@Path("truckId") truckId: String): Response<ApiMessage>

    @POST("truck_routes")
    suspend fun addNewTruckRoute(@Body truckRoute: TruckRoute): Response<ApiMessage>

    @GET("truck_routes")
    suspend fun getUserTruckRouteList(): Response<MutableList<TruckRoute>>

    @PUT("truck_routes/{truckRouteId}")
    suspend fun updateTruckRouteDetails(
        @Path("truckRouteId") truckRouteId: String,
        @Body truckRoute: TruckRoute
    ): Response<ApiMessage>

    @DELETE("truck_routes/{truckRouteId}")
    suspend fun deleteTruckRoute(@Path("truckRouteId") truckRouteId: String): Response<ApiMessage>

    @GET("loads/{loadId}")
    suspend fun getLoadDetails(@Path("loadId") loadId: String): Response<Load>

    @POST("loads")
    suspend fun addNewLoad(@Body load: Load): Response<ApiMessage>

    @GET("loads")
    suspend fun getUserLoadList(): Response<MutableList<Load>>

    @PUT("loads/{loadId}")
    suspend fun updateLoadDetails(
        @Path("loadId") loadId: String,
        @Body load: Load
    ): Response<ApiMessage>

    @DELETE("loads/{loadId}")
    suspend fun deleteLoad(@Path("loadId") loadId: String): Response<ApiMessage>
}

interface PostalApiService {
    @GET("pincode/{pinCode}")
    suspend fun getPostalAddress(@Path("pinCode") pinCode: String): Response<MutableList<ResponsePostalAddress>>
}
