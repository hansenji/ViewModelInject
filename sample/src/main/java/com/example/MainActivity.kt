package com.example

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

//    @Inject
//    lateinit var viewModelFactoryFactory: SavedStateViewModelFactory.Factory
//
//    private val viewModel by viewModels<MainViewModel> {
//        viewModelFactoryFactory.create(
//            this
////            , Bundle().apply {
////                putString("NAME", "Bob")
////            }
//        )
//    }
//
//    init {
//        Injector.init()
//        Injector.get().inject(this)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.main_activity)
//
//        val textView = findViewById<TextView>(R.id.textView)
//        textView.text = viewModel.getGreeting()
//    }
}

//class MainViewModel
//@AssistedInject constructor(
//    private val greeter: Greeter,
//    @Assisted val arg0: SavedStateHandle
//) : ViewModel() {
//
//    fun getGreeting(): String {
////        return greeter.sayHi(arg0["NAME"] ?: "EMPTY")
//        return greeter.sayHi("James")
//    }
//
//    @AssistedInject.Factory
////    interface Factory : ViewModelBasicFactory<MainViewModel>
//    interface Factory : ViewModelSavedStateFactory<MainViewModel>
//}