package com.vikingsen.inject.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

public interface ViewModelBasicFactory<T extends ViewModel> extends AbstractViewModelFactory<T> {
    @NonNull
    T create();
}
