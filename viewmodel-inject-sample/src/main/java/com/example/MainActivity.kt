package com.example

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.inject.Injector
import com.squareup.inject.assisted.Assisted
import com.vikingsen.inject.viewmodel.ViewModelInject
import com.vikingsen.inject.viewmodel.savedstate.SavedStateViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactoryFactory: SavedStateViewModelFactory.Factory

    private val viewModel by viewModels<MainViewModel> {
        viewModelFactoryFactory.create(
            this, Bundle().apply {
                putString("NAME", "Bob")
            }
        )
    }

    init {
        Injector.init()
        Injector.get().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val textView = findViewById<TextView>(R.id.textView)
        textView.text = viewModel.getGreeting()
    }
}

class MainViewModel
@ViewModelInject constructor(
    private val greeter: Greeter,
    @Assisted val savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun getGreeting(): String {
        return greeter.sayHi(savedStateHandle["NAME"] ?: "EMPTY")
    }
}