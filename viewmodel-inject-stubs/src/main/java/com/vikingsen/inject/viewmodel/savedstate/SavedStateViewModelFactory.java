package com.vikingsen.inject.viewmodel.savedstate;

import androidx.lifecycle.AbstractSavedStateVMFactory;
import androidx.lifecycle.ViewModel;

public class SavedStateViewModelFactory extends AbstractSavedStateVMFactory {

    @Override
    public <T extends ViewModel> T create(String key, Class<T> modelClass) {
        throw new RuntimeException("STUB!");
    }

    public static class Factory {
    }
}
