package com.homc.homctruck.restapi

import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Created by Admin on 1/3/2017.
 */
interface AppApiService {
    /*@GET("$API_VERSION/{identifier}/club_meetings/{meetingId}/club_meeting_agenda_items/{agendaItemId}/table_topic_speakers")
    suspend fun getTableTopicSpeakers(
        @Path("identifier") identifier: String,
        @Path("meetingId") meetingId: Long,
        @Path("agendaItemId") agendaItemId: Long
    ): Response<MutableList<User>>

    @PUT("$API_VERSION/{identifier}/club_meetings/{meetingId}/club_meeting_agenda_items/{agendaItemId}/table_topic_speakers/{speakerId}")
    suspend fun updateTableTopicSpeaker(
        @Path("identifier") identifier: String,
        @Path("meetingId") meetingId: Long,
        @Path("agendaItemId") agendaItemId: Long,
        @Path("speakerId") speakerId: Long,
        @Body agendaItems: User
    ): Response<User>

    @POST("users/{meetingId}")
    suspend fun addTableTopicSpeaker(
        @Path("identifier") identifier: String,
        @Path("meetingId") meetingId: Long,
        @Path("agendaItemId") agendaItemId: Long,
        @Body agendaItems: User
    ): Response<User>*/

    @GET("users/{userId}")
    suspend fun getUserDetail(@Path("userId") userId: String): Response<User>

    @POST("users")
    suspend fun addNewUserDetail(@Body user: User): Response<ApiMessage>
}
