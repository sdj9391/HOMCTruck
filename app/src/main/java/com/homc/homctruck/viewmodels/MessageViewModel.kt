package com.homc.homctruck.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homc.homctruck.data.models.ApiMessage
import com.homc.homctruck.data.models.Message
import com.homc.homctruck.data.repositories.MessageRepository
import com.homc.homctruck.restapi.DataBound
import kotlinx.coroutines.launch


class MessageViewModel(var app: Application, private val repository: MessageRepository) :
    ViewModel() {

    fun addMessage(message: Message): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.addMessage(message)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }

    fun getMessageList(): MutableLiveData<DataBound<MutableList<Message>>> {
        val liveData = MutableLiveData<DataBound<MutableList<Message>>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.getMessageList()

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }

    fun updateMessageDetails(
        messageId: String,
        message: Message
    ): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.updateMessage(messageId, message)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }

    fun deleteMessage(messageId: String): MutableLiveData<DataBound<ApiMessage>> {
        val liveData = MutableLiveData<DataBound<ApiMessage>>()

        val job = viewModelScope.launch {
            try {
                liveData.value = DataBound.Loading()
                val dataBound = repository.deleteMessage(messageId)

                dataBound.let {
                    when (it) {
                        is DataBound.Success -> {
                            liveData.value = DataBound.Success(it.data)
                        }
                        is DataBound.Error -> {
                            liveData.value = DataBound.Error(it.message, it.code)
                        }
                    }
                }
            } catch (t: Throwable) {
                liveData.value = DataBound.Error(t.message, null)
            }
        }

        return liveData
    }
}