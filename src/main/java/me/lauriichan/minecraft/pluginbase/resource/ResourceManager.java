package me.lauriichan.minecraft.pluginbase.resource;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public final class ResourceManager {

    private static final Pattern RESOURCE_PATTERN = Pattern.compile("^(?<type>[a-zA-Z]*):\\/\\/(?<path>.*)$");

    private final BasePlugin<?> plugin;
    private final Object2ObjectOpenHashMap<String, ISourceProvider> map = new Object2ObjectOpenHashMap<>();

    private volatile String defaultProvider;

    public ResourceManager(final BasePlugin<?> plugin) {
        this.plugin = plugin;
    }

    public String getDefault() {
        return defaultProvider;
    }

    public void setDefault(final String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public ISourceProvider getProvider(final String providerName) {
        return map.get(providerName);
    }

    public void register(final String type, final ISourceProvider provider) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type can't be null or blank!");
        }
        if (map.containsKey(type)) {
            throw new IllegalArgumentException("There is already a source provider for type '" + type + "'!");
        }
        map.put(type, Objects.requireNonNull(provider));
    }

    public IDataSource resolve(final String rawPath) {
        final int index = rawPath.indexOf("://");
        if (index == -1) {
            return resolve(defaultProvider, rawPath);
        }
        final Matcher match = RESOURCE_PATTERN.matcher(rawPath);
        if (!match.matches()) {
            return resolve(defaultProvider, rawPath.substring(index + 3));
        }
        final String type = match.group("type");
        final String path = match.group("path");
        if (type.isBlank()) {
            return resolve(defaultProvider, path);
        }
        return resolve(type, path);
    }

    private IDataSource resolve(final String type, final String path) {
        final ISourceProvider provider = map.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown resource type '" + type + "'!");
        }
        return provider.provide(plugin, path);
    }

}
