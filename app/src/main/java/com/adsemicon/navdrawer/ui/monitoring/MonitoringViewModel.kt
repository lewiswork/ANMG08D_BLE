package com.adsemicon.navdrawer.ui.monitoring

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MonitoringViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is monitoring Fragment"
    }
    val text: LiveData<String> = _text
}