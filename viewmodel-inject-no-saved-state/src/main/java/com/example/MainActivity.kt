package com.example

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.example.inject.Injector
import com.vikingsen.inject.viewmodel.ViewModelFactory
import com.vikingsen.inject.viewmodel.ViewModelInject
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    init {
        Injector.init()
        Injector.get().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        findViewById<TextView>(R.id.textView).text = viewModel.getGreeting()
    }
}

class MainViewModel
@ViewModelInject constructor(
    private val greeter: Greeter
) : ViewModel() {
    fun getGreeting(): String {
        return greeter.sayHi("MainViewModel")
    }
}
