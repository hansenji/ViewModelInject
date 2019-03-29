package com.vikingsen.inject.viewmodel.processor

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.jupiter.api.Test

private const val GENERATED_TYPE = "javax.annotation.Generated" // TODO vary once JDK 9 works.
private const val GENERATED_ANNOTATION = """
@Generated(
  value = "com.vikingsen.inject.viewmodel.processor.ViewModelInjectProcessor",
  comments = "https://github.com/hansenji/ViewModelInject"
)
"""

class ViewModelInjectProcessorTest {

    @Test
    fun simpleSavedStateTest() {
        val inputViewModel = JavaFileObjects.forSourceString("test.TestViewModel", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel extends ViewModel {
                @ViewModelInject
                TestViewModel(Long foo, @Assisted SavedStateHandle savedStateHandle) {
                }
            }
        """)
        val inputModule = JavaFileObjects.forSourceString("test.TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes =  ViewModelInject_TestModule.class)
            abstract class TestModule {}
        """)

        val expectedFactory = JavaFileObjects.forSourceString("test.TestViewModel_AssistedFactory", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import com.vikingsen.inject.viewmodel.savedstate.ViewModelSavedStateFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestViewModel_AssistedFactory implements ViewModelSavedStateFactory<TestViewModel> {
                private final Provider<Long> foo;

                @Inject public TestViewModel_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public TestViewModel create(SavedStateHandle savedStateHandle) {
                    return new TestViewModel(foo.get(), savedStateHandle);
                }
            }
        """)

        val expectedModule = JavaFileObjects.forSourceString("test.ViewModelModule_TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.AbstractViewModelFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.ClassKey;
            import dagger.multibindings.IntoMap;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class ViewModelInject_TestModule {
                private ViewModelInject_TestModule() {}

                @Binds
                @IntoMap
                @ClassKey(TestViewModel.class)
                abstract AbstractViewModelFactory<TestViewModel> bind_test_TestViewModel(TestViewModel_AssistedFactory factory);
            }
        """)

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }
}