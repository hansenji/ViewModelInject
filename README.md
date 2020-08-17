### Deprecated: Use [Hilt](https://dagger.dev/hilt/) and [Androidx Hilt](https://developer.android.com/training/dependency-injection/hilt-jetpack) ###

Assisted Injection for Android ViewModels
=========================================

This library is based on and depends heavily on [Assisted Inject](https://github.com/square/AssistedInject)

ViewModelInject supports [Dagger2](https://google.github.io/dagger/) and [SavedStateHandle](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate)

#### NOTE 
This is using alpha versions of the Androidx/JetPack libraries. Use at your own risk.

This Library will go 1.0 when Assisted Inject goes 1.0.

Usage
-----

#### ViewModel

Java
```java
class MyViewModel extends ViewModel {
    @ViewModelInject
    MyViewModel(Long foo, @Assisted SavedStateHandle savedStateHandle) {}
}
```
Kotlin
```kotlin
class MyViewModel
@ViewModelInject constructor(
    foo: Long, @Assisted savedStateHandle: SavedStateHandle
): ViewModel() {}
```

#### Module

In order to allow Dagger to use the generated factory, define an assisted dagger module anywhere in 
the same gradle module:

Java
```java
@ViewModelModule
@Module(includes = ViewModelInject_VMModule.class)
abstract class VMModule {}
``` 
Kotlin
```kotlin
@ViewModelModule
@Module(includes = [ViewModelInject_VMModule::class])
abstract class VMModule
``` 

The library will generate the `ViewModelInject_VMModule` for us.

#### Factory

Inside your Activity or Fragment inject one of the following factories:

If you are **not** using a `SavedStateHandle` inject the `ViewModelFactory`

Java
```java
@Inject ViewModelFactory viewModelFactory;

public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                                .get(MainViewModel.class);
    // ...
}
```
Kotlin
```kotlin
@Inject 
lateinit var viewModelFactory: ViewModelFactory

public void onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel = ViewModelProviders.of(this, viewModelFactory)
                                .get(MainViewModel::class.java)
    // ...
}
```

If you are using a `SavedStateHandle` inject the `SavedStateViewModelFactory.Factory`

Java
```java
@Inject SavedStateViewModelFactory.Factory viewModelFactoryFactory;

fun onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyViewModel viewModel = ViewModelProviders.of(this, viewModelFactoryFactory.create(this, intent.getExtras()))
                                .get(MainViewModel.class);
    // ...
}
```
Kotlin
```kotlin
@Inject
lateinit var viewModelFactoryFactory: SavedStateViewModelFactory.Factory

fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val viewModel = ViewModelProviders.of(this, viewModelFactoryFactory.create(this, intent.getExtras()))
                                .get(MainViewModel::class.java)
    // ...
}
```

The `SavedStateViewModelFactory` handles both `ViewModels` with and without a `SavedStateHandle`. 
If you are using SavedStateHandles anywhere in your project it is recommended to always use the `SavedStateViewModelFactory`

For optimal build speed, all the referred `@Injecte`d types should be compiled with the Dagger processor.
This is also a requirement for incremental annotation processing with `kapt`.
Given that `SavedStateViewModelFactory.Factory` is an external dependency, you should provide an instance using a `@Provides` method.
For example, if you use `SavedStateViewModelFactory`:

Java
```java
@Module
public class AppViewModelFactoryFactoryModule {
    @Provides
    @Singleton
    public static SavedStateViewModelFactory.Factory provideViewModelFactoryFactory(
            Map<Class<?>, AbstractViewModelFactory> factories
    ) {
        return new SavedStateViewModelFactory.Factory(factories);
    }
}
```

Kotlin
```kotlin
@Module
object AppViewModelFactoryFactoryModule {
    @Provides
    @Singleton
    fun provideViewModelFactoryFactory(
            factories: Map<Class<*>, @JvmSuppressWildcards AbstractViewModelFactory>
    ): SavedStateViewModelFactory.Factory {
        return SavedStateViewModelFactory.Factory(factories)
    }
}
```

Download
--------
```groovy
implementation 'com.vikingsen.inject:viewmodel-inject:0.3.3'
annotationProcessor 'com.vikingsen.inject:viewmodel-inject-processor:0.3.3' // or `kapt` for Kotlin
```

For Snapshots include the following repository:
```groovy
repositories {
    // ...
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}
```

License
=======

    Copyright 2019 Jordan Hansen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
