package com.homc.homctruck.data.repositories

import com.homc.homctruck.data.contracts.MessageContract
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Message
import com.homc.homctruck.data.sourceremote.MessageRemoteDataSource
import com.homc.homctruck.restapi.DataBound

class MessageRepository(var dataSource: MessageRemoteDataSource) :
    MessageContract {
    override suspend fun addMessage(message: Message): DataBound<ApiMessage> {
        return dataSource.addMessage(message)
    }

    override suspend fun getMessageList(): DataBound<MutableList<Message>> {
        return dataSource.getMessageList()
    }

    override suspend fun updateMessage(messageId: String, message: Message): DataBound<ApiMessage> {
        return dataSource.updateMessage(messageId, message)
    }

    override suspend fun deleteMessage(messageId: String): DataBound<ApiMessage> {
        return dataSource.deleteMessage(messageId)
    }
}