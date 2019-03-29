package com.example.inject

import com.example.MainActivity

//@Component(modules = [ViewModelModule::class, MainModule::class])
interface AppComponent {
    fun inject(target: MainActivity)
}
