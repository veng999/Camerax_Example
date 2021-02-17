package com.example.camerax_example.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.camerax_example.App
import com.example.camerax_example.MainViewModel
import java.security.Provider

class ViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        when(modelClass) {
            MainViewModel::class.java -> MainViewModel(App.getApp())
            else -> throw IllegalAccessException("Can't find $modelClass")
        } as T
}

