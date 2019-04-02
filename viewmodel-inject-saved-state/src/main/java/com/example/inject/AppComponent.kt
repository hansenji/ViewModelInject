package com.example.inject

import com.example.MainActivity
import com.example.VMModule
import dagger.Component

@Component(modules = [VMModule::class])
interface AppComponent {
    fun inject(target: MainActivity)
}
