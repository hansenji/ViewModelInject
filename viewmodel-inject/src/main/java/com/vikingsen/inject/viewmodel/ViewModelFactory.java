package com.vikingsen.inject.viewmodel;

import com.vikingsen.inject.viewmodel.savedstate.ViewModelSavedStateFactory;

import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final Map<Class<?>, AbstractViewModelFactory> factories;

    @Inject
    public ViewModelFactory(@NonNull Map<Class<?>, AbstractViewModelFactory> factories) {
        if (factories == null) throw new NullPointerException("factories == null");
        this.factories = factories;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        AbstractViewModelFactory factory = factories.get(modelClass);
        if (factory == null) {
            for (Class key : factories.keySet()) {
                if (modelClass.isAssignableFrom(key)) {
                    factory = factories.get(key);
                    break;
                }
            }
        }
        if (factory != null) {
            if (factory instanceof ViewModelBasicFactory) {
                try {
                    return (T) ((ViewModelBasicFactory) factory).create();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (factory instanceof ViewModelSavedStateFactory) {
                throw new IllegalStateException("ViewModels with a SavedStateHandle must use a SavedStateViewModelFactory");
            } else {
                throw new IllegalStateException("Invalid Factory Type " + factory.getClass());
            }
        }
        throw new IllegalStateException("Unknown model class " + modelClass.getName());
    }
}
