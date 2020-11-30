package com.homc.homctruck.data.contracts

import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Message
import com.homc.homctruck.restapi.DataBound

interface MessageContract {
    suspend fun addMessage(message: Message): DataBound<ApiMessage>
    suspend fun getMessageList(): DataBound<MutableList<Message>>
    suspend fun updateMessage(messageId: String, message: Message): DataBound<ApiMessage>
    suspend fun deleteMessage(messageId: String): DataBound<ApiMessage>
}