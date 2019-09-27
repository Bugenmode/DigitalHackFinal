package com.alexey.digitalhackfinal.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.alexey.digitalhackfinal.utils.EventData

abstract class BaseFragment : Fragment() {

    fun setupEventListener(lifecycleOwner: LifecycleOwner, viewModel: BaseViewModel) {
        viewModel.events.observe(lifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                onEvent(it)
            }
        })
    }

    open fun onEvent(eventData: EventData) {}
}