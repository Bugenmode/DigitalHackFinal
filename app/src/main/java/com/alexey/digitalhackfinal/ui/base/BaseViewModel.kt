package com.alexey.digitalhackfinal.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alexey.digitalhackfinal.utils.Event
import com.alexey.digitalhackfinal.utils.EventData

open class BaseViewModel : ViewModel() {

    val events = MutableLiveData<Event<EventData>>()

    fun sendEvent(code: String?, payload: Any? = null) {
        val eventData = EventData(eventCode = code, eventPayload = payload)

        events.value = Event(eventData)
    }

}