package com.example

import com.vikingsen.inject.viewmodel.ViewModelModule
import dagger.Module


@ViewModelModule
@Module(includes = [ViewModelInject_VMModule::class])
abstract class VMModule

