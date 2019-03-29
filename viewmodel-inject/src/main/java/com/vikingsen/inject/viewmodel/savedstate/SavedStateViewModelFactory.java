package com.vikingsen.inject.viewmodel.savedstate;

import android.os.Bundle;

import com.vikingsen.inject.viewmodel.AbstractViewModelFactory;
import com.vikingsen.inject.viewmodel.ViewModelBasicFactory;

import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateVMFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

public class SavedStateViewModelFactory extends AbstractSavedStateVMFactory {

    private final Map<Class<?>, AbstractViewModelFactory<? extends ViewModel>> factories;

    SavedStateViewModelFactory(@NonNull Map<Class<?>, AbstractViewModelFactory<? extends ViewModel>> factories, @NonNull SavedStateRegistryOwner owner, @Nullable Bundle defaultArgs) {
        super(owner, defaultArgs);
        this.factories = factories;
    }

    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
        AbstractViewModelFactory factory = factories.get(modelClass);
        if (factory == null) {
            for (Class keyCls : factories.keySet()) {
                if (modelClass.isAssignableFrom(keyCls)) {
                    factory = factories.get(keyCls);
                    break;
                }
            }
        }
        if (factory != null) {
            try {
                return createViewModel(factory, handle);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Unknown model class " + modelClass);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T extends ViewModel> T createViewModel(@NonNull AbstractViewModelFactory factory, @NonNull SavedStateHandle handle) {
        if (factory instanceof ViewModelSavedStateFactory) {
            return (T) ((ViewModelSavedStateFactory) factory).create(handle);
        }
        if (factory instanceof ViewModelBasicFactory) {
            return (T) ((ViewModelBasicFactory) factory).create();
        }
        throw new IllegalStateException("Invalid Factory Type " + factory.getClass());
    }

    public static class Factory {
        private final Map<Class<?>, AbstractViewModelFactory<? extends ViewModel>> factories;

        @Inject
        public Factory(@NonNull Map<Class<?>, AbstractViewModelFactory<? extends ViewModel>> factories) {
            if (factories == null) throw new NullPointerException("factories == null");
            this.factories = factories;
        }

        public SavedStateViewModelFactory create(@NonNull SavedStateRegistryOwner owner, @Nullable Bundle defaultArgs) {
            return new SavedStateViewModelFactory(factories, owner, defaultArgs);
        }

        public SavedStateViewModelFactory create(@NonNull SavedStateRegistryOwner owner) {
            return create(owner, null);
        }
    }
}
