package com.alexey.digitalhackfinal.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.alexey.digitalhackfinal.utils.EventData

abstract class BaseActivity : AppCompatActivity() {

    fun setupEventListener(lifecycleOwner: LifecycleOwner, viewModel: BaseViewModel) {
        viewModel.events.observe(lifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                onEvent(it)
            }
        })
    }

    open fun onEvent(eventData: EventData) {}

}