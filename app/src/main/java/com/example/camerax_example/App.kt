package com.example.camerax_example

import android.app.Application

class App : Application() {

    companion object {
        private lateinit var application: Application

        @JvmStatic
        fun getApp() = application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }
}