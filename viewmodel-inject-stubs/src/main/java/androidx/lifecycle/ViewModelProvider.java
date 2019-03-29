package androidx.lifecycle;

public class ViewModelProvider {
    public interface Factory {
        /**
         * Creates a new instance of the given {@code Class}.
         * <p>
         *
         * @param modelClass a {@code Class} whose instance is requested
         * @param <T>        The type parameter for the ViewModel.
         * @return a newly created ViewModel
         */
        <T extends ViewModel> T create(Class<T> modelClass);
    }

    abstract static class KeyedFactory implements Factory {
        /**
         * Creates a new instance of the given {@code Class}.
         *
         * @param key a key associated with the requested ViewModel
         * @param modelClass a {@code Class} whose instance is requested
         * @param <T>        The type parameter for the ViewModel.
         * @return a newly created ViewModel
         */
        public abstract <T extends ViewModel> T create(String key, Class<T> modelClass);

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            throw new UnsupportedOperationException("create(String, Class<?>) must be called on "
                    + "implementaions of KeyedFactory");
        }
    }
}
