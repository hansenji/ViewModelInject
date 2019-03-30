package com.vikingsen.inject.viewmodel.savedstate;

import com.vikingsen.inject.viewmodel.AbstractViewModelFactory;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public interface ViewModelSavedStateFactory<T extends ViewModel> extends AbstractViewModelFactory {
    T create(SavedStateHandle savedStateHandle);
}
