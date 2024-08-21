package com.example.attendsystem.MFS100_Operations

import android.app.Application
import android.util.Log
import com.mantra.mfs100.MFS100

class MFS100Application: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("MFS100Application", "Application class initialized")
        HandleMFS100Events
    }

    companion object {
        lateinit var instance: MFS100Application
            private set
    }



}