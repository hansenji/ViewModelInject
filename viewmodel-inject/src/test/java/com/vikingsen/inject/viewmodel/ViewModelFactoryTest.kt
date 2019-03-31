package com.vikingsen.inject.viewmodel

import androidx.lifecycle.ViewModel
import com.vikingsen.inject.viewmodel.savedstate.ViewModelSavedStateFactory
import org.junit.Assert.assertSame
import org.junit.Test

class ViewModelFactoryTest {

    @Test
    fun viewModelFactoryInMapTest() {
        val expected = TestViewModel()
        val factories: Map<Class<*>, AbstractViewModelFactory> = mapOf(TestViewModel::class.java to ViewModelBasicFactory { expected })
        val factory = ViewModelFactory(factories)
        val actual = factory.create(TestViewModel::class.java)
        assertSame(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun viewModelFactoryNoInMapTest() {
        val factory = ViewModelFactory(mapOf())
        factory.create(TestViewModel::class.java)
    }

    @Test(expected = IllegalStateException::class)
    fun invalidFactoryTypeTest() {
        val factories: Map<Class<*>, AbstractViewModelFactory> = mapOf(TestViewModel::class.java to ViewModelSavedStateFactory { throw AssertionError() })
        val factory = ViewModelFactory(factories)
        factory.create(TestViewModel::class.java)
    }

    private class TestViewModel : ViewModel()
}