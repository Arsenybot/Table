package com.touchpanel.android

import android.app.Application

/**
 * Main application class initializing connection manager, data store,
 * and background services.
 */
class TouchPanelApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: TouchPanelApp
            private set
    }
}
