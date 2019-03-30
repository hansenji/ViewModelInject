package com.vikingsen.inject.viewmodel.processor

import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
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
    fun simpleTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel extends ViewModel {
                @ViewModelInject
                TestViewModel(Long foo) {
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes = ViewModelInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestViewModel_AssistedFactory", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelBasicFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestViewModel_AssistedFactory implements ViewModelBasicFactory<TestViewModel> {
                private final Provider<Long> foo;

                @Inject public TestViewModel_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public TestViewModel create() {
                    return new TestViewModel(foo.get());
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.ViewModelModule_TestModule", """
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
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun publicTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel extends ViewModel {
                @ViewModelInject
                TestViewModel(Long foo) {
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes =  ViewModelInject_TestModule.class)
            public abstract class TestModule {}
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.ViewModelModule_TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.AbstractViewModelFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.ClassKey;
            import dagger.multibindings.IntoMap;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            public abstract class ViewModelInject_TestModule {
                private ViewModelInject_TestModule() {}

                @Binds
                @IntoMap
                @ClassKey(TestViewModel.class)
                abstract AbstractViewModelFactory<TestViewModel> bind_test_TestViewModel(TestViewModel_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedModule)
    }

    @Test
    fun nestedTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class Outer {
                static class TestViewModel extends ViewModel {
                    @ViewModelInject
                    TestViewModel(Long foo) {
                    }
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes =  ViewModelInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.Outer${'$'}TestViewModel_AssistedFactory", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelBasicFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class Outer${'$'}TestViewModel_AssistedFactory implements ViewModelBasicFactory<Outer.TestViewModel> {
                private final Provider<Long> foo;

                @Inject public Outer${'$'}TestViewModel_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public Outer.TestViewModel create() {
                    return new Outer.TestViewModel(foo.get());
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.ViewModelModule_TestModule", """
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
                @ClassKey(Outer.TestViewModel.class)
                abstract AbstractViewModelFactory<Outer.TestViewModel> bind_test_Outer${'$'}TestViewModel(Outer${'$'}TestViewModel_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun simpleSavedStateTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
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
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes =  ViewModelInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestViewModel_AssistedFactory", """
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
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.ViewModelModule_TestModule", """
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
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun publicSavedStateTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
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
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes =  ViewModelInject_TestModule.class)
            public abstract class TestModule {}
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.ViewModelModule_TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.AbstractViewModelFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.ClassKey;
            import dagger.multibindings.IntoMap;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            public abstract class ViewModelInject_TestModule {
                private ViewModelInject_TestModule() {}

                @Binds
                @IntoMap
                @ClassKey(TestViewModel.class)
                abstract AbstractViewModelFactory<TestViewModel> bind_test_TestViewModel(TestViewModel_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedModule)
    }

    @Test
    fun nestedSavedStateTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class Outer {
                static class TestViewModel extends ViewModel {
                    @ViewModelInject
                    TestViewModel(Long foo, @Assisted SavedStateHandle savedStateHandle) {
                    }
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelModule;
            import dagger.Module;

            @ViewModelModule
            @Module(includes =  ViewModelInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.Outer${'$'}TestViewModel_AssistedFactory", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import com.vikingsen.inject.viewmodel.savedstate.ViewModelSavedStateFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class Outer${'$'}TestViewModel_AssistedFactory implements ViewModelSavedStateFactory<Outer.TestViewModel> {
                private final Provider<Long> foo;

                @Inject public Outer${'$'}TestViewModel_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public Outer.TestViewModel create(SavedStateHandle savedStateHandle) {
                    return new Outer.TestViewModel(foo.get(), savedStateHandle);
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.ViewModelModule_TestModule", """
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
                @ClassKey(Outer.TestViewModel.class)
                abstract AbstractViewModelFactory<Outer.TestViewModel> bind_test_Outer${'$'}TestViewModel(Outer${'$'}TestViewModel_AssistedFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputViewModel, inputModule))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun assistedParamFirstTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel extends ViewModel {
                @ViewModelInject
                TestViewModel(@Assisted SavedStateHandle savedStateHandle, Long foo) {
                }
            }
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestViewModel_AssistedFactory", """
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
                    return new TestViewModel(savedStateHandle, foo.get());
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputViewModel)
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory)
    }

    @Test
    fun differentNameSavedStateHandleTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel extends ViewModel {
                @ViewModelInject
                TestViewModel(Long foo, @Assisted SavedStateHandle handle) {
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputViewModel)
            .processedWith(ViewModelInjectProcessor())
            .failsToCompile()
            .withErrorContaining(
                """
                ViewModel injection only allows up to 1 @Assisted parameter of type SavedStateHandle.
                    Found:
                      [androidx.lifecycle.SavedStateHandle handle]
                    Expected:
                      [androidx.lifecycle.SavedStateHandle savedStateHandle]
            """.trimIndent()
            )
            .`in`(inputViewModel).onLine(11)
    }

    @Test
    fun typeDoesNotExtendViewModelTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel {
                @ViewModelInject
                TestViewModel(Long foo) {
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputViewModel)
            .processedWith(ViewModelInjectProcessor())
            .failsToCompile()
            .withErrorContaining(
                "@ViewModelInject-using types must be subtypes of ViewModel"
            )
            .`in`(inputViewModel).onLine(6)
    }

    @Test
    fun typeExtendsViewModelSubclassTest() {
        val inputViewModel = JavaFileObjects.forSourceString(
            "test.TestViewModel", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class BaseViewModel extends ViewModel {}

            class TestViewModel extends BaseViewModel {
                @ViewModelInject
                TestViewModel(Long foo, @Assisted SavedStateHandle savedStateHandle) {
                }
            }
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestViewModel_AssistedFactory", """
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
        """
        )

        assertAbout(javaSource())
            .that(inputViewModel)
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory)
    }

    @Test
    fun baseAndSubtypeInjectionTest() {
        val inputViewModel1 = JavaFileObjects.forSourceString(
            "test.TestViewModel1", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel1 extends ViewModel {
                @ViewModelInject
                TestViewModel1(Long foo, @Assisted SavedStateHandle savedStateHandle) {
                }
            }
        """
        )

        val inputViewModel2 = JavaFileObjects.forSourceString(
            "test.TestViewModel2", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import androidx.lifecycle.ViewModel;
            import com.squareup.inject.assisted.Assisted;
            import com.vikingsen.inject.viewmodel.ViewModelInject;

            class TestViewModel2 extends TestViewModel1 {
                @ViewModelInject
                TestViewModel2(Long foo, @Assisted SavedStateHandle savedStateHandle) {
                    super(foo, savedStateHandle);
                }
            }
        """
        )

        val expectedFactory1 = JavaFileObjects.forSourceString(
            "test.TestViewModel1_AssistedFactory", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import com.vikingsen.inject.viewmodel.savedstate.ViewModelSavedStateFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestViewModel1_AssistedFactory implements ViewModelSavedStateFactory<TestViewModel1> {
                private final Provider<Long> foo;

                @Inject public TestViewModel1_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public TestViewModel1 create(SavedStateHandle savedStateHandle) {
                    return new TestViewModel1(foo.get(), savedStateHandle);
                }
            }
        """
        )

        val expectedFactory2 = JavaFileObjects.forSourceString(
            "test.TestViewModel2_AssistedFactory", """
            package test;

            import androidx.lifecycle.SavedStateHandle;
            import com.vikingsen.inject.viewmodel.savedstate.ViewModelSavedStateFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestViewModel2_AssistedFactory implements ViewModelSavedStateFactory<TestViewModel2> {
                private final Provider<Long> foo;

                @Inject public TestViewModel2_AssistedFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public TestViewModel2 create(SavedStateHandle savedStateHandle) {
                    return new TestViewModel2(foo.get(), savedStateHandle);
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputViewModel1, inputViewModel2))
            .processedWith(ViewModelInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory1, expectedFactory2)
    }

    // TODO: 3/29/19 IMPLEMENT TESTS
//    @Test fun constructorMissingAssistedParametersFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.view.View;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class TestView extends View {
//        @InflationInject
//        TestView(Long foo) {
//          super(null);
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("""
//          Inflation injection requires Context and AttributeSet @Assisted parameters.
//              Found:
//                []
//              Expected:
//                [android.content.Context context, android.util.AttributeSet attrs]
//          """.trimIndent())
//            .`in`(inputView).onLine(9)
//    }
//
//    @Test fun constructorExtraAssistedParameterFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.content.Context;
//      import android.util.AttributeSet;
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class TestView extends View {
//        @InflationInject
//        TestView(@Assisted Context context, @Assisted AttributeSet attrs, @Assisted String hey, Long foo) {
//          super(context, attrs);
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("""
//          Inflation injection requires Context and AttributeSet @Assisted parameters.
//              Found:
//                [android.content.Context context, android.util.AttributeSet attrs, java.lang.String hey]
//              Expected:
//                [android.content.Context context, android.util.AttributeSet attrs]
//          """.trimIndent())
//            .`in`(inputView).onLine(12)
//    }
//
//    @Test fun constructorMissingContextFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.util.AttributeSet;
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class TestView extends View {
//        @InflationInject
//        TestView(@Assisted AttributeSet attrs, Long foo) {
//          super(null, attrs);
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("""
//          Inflation injection requires Context and AttributeSet @Assisted parameters.
//              Found:
//                [android.util.AttributeSet attrs]
//              Expected:
//                [android.content.Context context, android.util.AttributeSet attrs]
//          """.trimIndent())
//            .`in`(inputView).onLine(11)
//    }
//
//    @Test fun constructorMissingAttributeSetFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.content.Context;
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class TestView extends View {
//        @InflationInject
//        TestView(@Assisted Context context, Long foo) {
//          super(context, null);
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("""
//          Inflation injection requires Context and AttributeSet @Assisted parameters.
//              Found:
//                [android.content.Context context]
//              Expected:
//                [android.content.Context context, android.util.AttributeSet attrs]
//          """.trimIndent())
//            .`in`(inputView).onLine(11)
//    }
//
//    @Test fun constructorMissingProvidedParametersWarns() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.content.Context;
//      import android.util.AttributeSet;
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class TestView extends View {
//        @InflationInject
//        TestView(@Assisted Context context, @Assisted AttributeSet attrs) {
//          super(context, attrs);
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .compilesWithoutError()
//            .withWarningContaining("Inflation injection requires at least one non-@Assisted parameter.")
//            .`in`(inputView).onLine(12)
//        // .and().generatesNoFiles()
//    }
//
//    @Test fun privateConstructorFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class TestView extends View {
//        @InflationInject
//        private TestView(@Assisted Context context, @Assisted AttributeSet attrs, Long foo) {
//          super(context, attrs);
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("@InflationInject constructor must not be private.")
//            .`in`(inputView).onLine(10)
//    }
//
//    @Test fun nestedPrivateTypeFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class Outer {
//        private static class TestView extends View {
//          @InflationInject
//          TestView(@Assisted Context context, @Assisted AttributeSet attrs, Long foo) {
//            super(context, attrs);
//          }
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("@InflationInject-using types must not be private")
//            .`in`(inputView).onLine(9)
//    }
//
//    @Test fun nestedNonStaticFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class Outer {
//        class TestView extends View {
//          @InflationInject
//          TestView(@Assisted Context context, @Assisted AttributeSet attrs, Long foo) {
//            super(context, attrs);
//          }
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("Nested @InflationInject-using types must be static")
//            .`in`(inputView).onLine(9)
//    }
//
//    @Test fun multipleInflationInjectConstructorsFails() {
//        val inputView = JavaFileObjects.forSourceString("test.TestView", """
//      package test;
//
//      import android.view.View;
//      import com.squareup.inject.assisted.Assisted;
//      import com.squareup.inject.inflation.InflationInject;
//
//      class TestView extends View {
//        @InflationInject
//        TestView(@Assisted Context context, @Assisted AttributeSet attrs, Long foo) {
//          super(context, attrs);
//        }
//
//        @InflationInject
//        TestView(@Assisted Context context, @Assisted AttributeSet attrs, String foo) {
//          super(context, attrs);
//        }
//      }
//    """)
//
//        assertAbout(javaSource())
//            .that(inputView)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("Multiple @InflationInject-annotated constructors found.")
//            .`in`(inputView).onLine(8)
//    }
//
//    @Test fun moduleWithoutModuleAnnotationFails() {
//        val moduleOne = JavaFileObjects.forSourceString("test.OneModule", """
//      package test;
//
//      import com.squareup.inject.inflation.InflationModule;
//
//      @InflationModule
//      abstract class OneModule {}
//    """)
//
//        assertAbout(javaSource())
//            .that(moduleOne)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("@InflationModule must also be annotated as a Dagger @Module")
//            .`in`(moduleOne).onLine(7)
//    }
//
//    @Test fun moduleWithNoIncludesFails() {
//        val moduleOne = JavaFileObjects.forSourceString("test.OneModule", """
//      package test;
//
//      import com.squareup.inject.inflation.InflationModule;
//      import dagger.Module;
//
//      @InflationModule
//      @Module
//      abstract class OneModule {}
//    """)
//
//        assertAbout(javaSource())
//            .that(moduleOne)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("@InflationModule's @Module must include InflationInject_OneModule")
//            .`in`(moduleOne).onLine(9)
//    }
//
//    @Test fun moduleWithoutIncludeFails() {
//        val moduleOne = JavaFileObjects.forSourceString("test.OneModule", """
//      package test;
//
//      import com.squareup.inject.inflation.InflationModule;
//      import dagger.Module;
//
//      @InflationModule
//      @Module(includes = TwoModule.class)
//      abstract class OneModule {}
//
//      @Module
//      abstract class TwoModule {}
//    """)
//
//        assertAbout(javaSource())
//            .that(moduleOne)
//            .processedWith(InflationInjectProcessor())
//            .failsToCompile()
//            .withErrorContaining("@InflationModule's @Module must include InflationInject_OneModule")
//            .`in`(moduleOne).onLine(9)
//    }

    @Test
    fun multipleModulesFailsTest() {
        val moduleOne = JavaFileObjects.forSourceString("test.OneModule", """
      package test;

      import com.vikingsen.inject.viewmodel.ViewModelModule;
      import dagger.Module;

      @ViewModelModule
      @Module(includes = ViewModelInject_OneModule.class)
      abstract class OneModule {}
    """)
        val moduleTwo = JavaFileObjects.forSourceString("test.TwoModule", """
      package test;

      import com.vikingsen.inject.viewmodel.ViewModelModule;
      import dagger.Module;

      @ViewModelModule
      @Module(includes = ViewModelInject_TwoModule.class)
      abstract class TwoModule {}
    """)

        assertAbout(javaSources())
            .that(listOf(moduleOne, moduleTwo))
            .processedWith(ViewModelInjectProcessor())
            .failsToCompile()
            .withErrorContaining("Multiple @ViewModelModule-annotated modules found.")
            .`in`(moduleOne).onLine(9)
            .and()
            .withErrorContaining("Multiple @ViewModelModule-annotated modules found.")
            .`in`(moduleTwo).onLine(9)
    }
}