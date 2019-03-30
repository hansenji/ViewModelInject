package com.vikingsen.inject.viewmodel;

import androidx.lifecycle.ViewModel;

public interface ViewModelBasicFactory<T extends ViewModel> extends AbstractViewModelFactory {
    T create();
}
