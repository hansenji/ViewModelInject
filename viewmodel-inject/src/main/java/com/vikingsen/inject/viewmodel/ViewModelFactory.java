package com.vikingsen.inject.viewmodel;

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
            try {
                if (factory instanceof ViewModelBasicFactory) {
                    return (T) ((ViewModelBasicFactory) factory).create();
                } else {
                    throw new IllegalStateException("Invalid Factory Type " + factory.getClass());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Unknown model class " + modelClass.getName());
    }
}
