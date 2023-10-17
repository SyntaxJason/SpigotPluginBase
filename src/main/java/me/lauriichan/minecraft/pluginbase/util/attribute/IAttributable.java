package me.lauriichan.minecraft.pluginbase.util.attribute;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public interface IAttributable {

    default Object attr(final String key) {
        return null;
    }

    default <T> T attr(final String key, final Class<T> type) {
        return null;
    }

    default <M> M attrMap(final String key, final Function<Object, M> mapper) {
        final Object value = attr(key);
        if (value != null) {
            return mapper.apply(value);
        }
        return null;
    }

    default <T, M> M attrMap(final String key, final Class<T> type, final Function<T, M> mapper) {
        final T value = attr(key, type);
        if (value != null) {
            return mapper.apply(value);
        }
        return null;
    }

    default <T> T attrOrDefault(final String key, final Class<T> type, final T fallback) {
        return fallback;
    }

    default Class<?> attrClass(final String key) {
        return null;
    }

    default <T> Class<? extends T> attrClass(final String key, final Class<T> type) {
        return null;
    }

    default <T> Class<? extends T> attrClassOrDefault(final String key, final Class<T> type, final Class<? extends T> fallback) {
        return fallback;
    }

    default boolean attrHas(final String key) {
        return false;
    }

    default boolean attrHas(final String key, final Class<?> type) {
        return false;
    }

    default void attrSet(final String key, final Object object) {}

    default Object attrUnset(final String key) {
        return null;
    }

    default <T> T attrUnset(final String key, final Class<T> type) {
        return null;
    }

    default <T> T attrUnsetOrDefault(final String key, final Class<T> type, final T fallback) {
        return null;
    }

    default void attrClear() {}

    default int attrAmount() {
        return 0;
    }

    default Set<String> attrKeys() {
        return Collections.emptySet();
    }

}