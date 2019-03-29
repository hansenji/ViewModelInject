package com.vikingsen.inject.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        throw new RuntimeException("STUB!");
    }
}
