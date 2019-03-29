package com.example.inject

object Injector {
    private lateinit var appComponent: AppComponent

    @Synchronized
    fun init() {
//        appComponent = DaggerAppComponent.create()
    }

    @Synchronized
    fun get(): AppComponent {
        return appComponent
    }
}