package me.lauriichan.minecraft.pluginbase;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.lauriichan.laylib.command.ArgumentRegistry;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.localization.source.AnnotationMessageSource;
import me.lauriichan.laylib.localization.source.EnumMessageSource;
import me.lauriichan.laylib.localization.source.IMessageDefinition;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.ExtensionPoolImpl.ConditionMapImpl;
import me.lauriichan.minecraft.pluginbase.command.argument.LoggerArgumentProvider;
import me.lauriichan.minecraft.pluginbase.command.argument.UUIDArgument;
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.minecraft.pluginbase.extension.IConditionMap;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.minecraft.pluginbase.extension.IExtensionPool;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PagedInventoryRegistry;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.minecraft.pluginbase.message.IMessageExtension;
import me.lauriichan.minecraft.pluginbase.message.provider.SimpleMessageProviderFactory;
import me.lauriichan.minecraft.pluginbase.resource.ResourceManager;
import me.lauriichan.minecraft.pluginbase.resource.source.FileDataSource;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;
import me.lauriichan.minecraft.pluginbase.resource.source.PathDataSource;
import me.lauriichan.minecraft.pluginbase.util.BukkitSimpleLogger;

public abstract class BasePlugin<T extends BasePlugin<T>> extends JavaPlugin {

    private volatile BukkitSimpleLogger logger;
    private volatile int state = 0;

    private volatile Path jarRoot;
    private volatile ResourceManager resourceManager;

    private volatile ArgumentRegistry argumentRegistry;
    private volatile MessageManager messageManager;

    private volatile ConditionMapImpl conditionMap;
    
    private volatile ConfigManager configManager;

    private volatile PagedInventoryRegistry pagedInventoryRegistry;

    @Override
    public final void onLoad() {
        if (state != 0 && state != 4) {
            return;
        }
        loadLogger();
        loadResourceRoot();
        loadResourceManager();
        state = 1;
        try {
            onCoreLoad();
        } catch (final Throwable throwable) {
            logger.error("Failed to load core part", throwable);
        }
        try {
            onPluginLoad();
        } catch (final Throwable throwable) {
            logger.error("Failed to load plugin part", throwable);
        }
    }

    private final void loadLogger() {
        if (logger != null && logger.getHandle() == getLogger()) {
            return;
        }
        logger = new BukkitSimpleLogger(getLogger());
    }

    private final void loadResourceRoot() {
        if (jarRoot != null) {
            return;
        }
        final File jarFile = getFile();
        URI uri;
        try {
            uri = new URI(("jar:file:/" + jarFile.getAbsolutePath().replace('\\', '/').replace(" ", "%20") + "!/").replace("//", "/"));
        } catch (final URISyntaxException e) {
            logger.warning("Failed to build resource uri", e);
            logger.warning("Falling back to jar uri, could cause problems");
            uri = jarFile.toURI();
        }
        Path path = null;
        try {
            FileSystems.getFileSystem(uri).close();
        } catch (final Exception exp) {
            if (!(exp instanceof NullPointerException || exp instanceof FileSystemNotFoundException)) {
                logger.warning("Something went wrong while closing the file system", exp);
            }
        }
        if (path == null) {
            try {
                path = FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath("/");
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to resolve jar root!", e);
            }
        }
        jarRoot = path;
    }

    private final void loadResourceManager() {
        if (resourceManager != null) {
            return;
        }
        resourceManager = new ResourceManager(this);
        resourceManager.setDefault("jar");
        resourceManager.register("jar", (plugin, path) -> new PathDataSource(plugin.jarRoot().resolveSibling(path)));
        resourceManager.register("data", (plugin, path) -> new FileDataSource(new File(plugin.getDataFolder(), path)));
    }

    @Override
    public final void onEnable() {
        if (state != 1 && state != 4) {
            return;
        }
        final boolean ready = state == 4;
        state = 2;
        try {
            onCoreEnable();
        } catch (final Throwable throwable) {
            logger.error("Failed to enable core part", throwable);
        }
        try {
            onPluginEnable();
        } catch (final Throwable throwable) {
            logger.error("Failed to enable plugin part", throwable);
        }
        try {
            onCorePostEnable();
        } catch (final Throwable throwable) {
            logger.error("Failed to post-enable core part", throwable);
        }
        if (ready) {
            onReady();
        }
    }

    final void onReady() {
        if (state != 2) {
            return;
        }
        state = 3;
        logger.info("Server is ready, lets do the final touches!");
        try {
            onCoreReady();
        } catch (final Throwable throwable) {
            logger.error("Failed to ready core part", throwable);
        }
        try {
            onPluginReady();
        } catch (final Throwable throwable) {
            logger.error("Failed to ready plugin part", throwable);
        }
    }

    @Override
    public final void onDisable() {
        if (state != 2 && state != 3) {
            return;
        }
        state = 4;
        try {
            onPluginDisable();
        } catch (final Throwable throwable) {
            logger.error("Failed to disable plugin part", throwable);
        }
        try {
            onCoreDisable();
        } catch (final Throwable throwable) {
            logger.error("Failed to disable core part", throwable);
        }
    }

    /*
     * Utilities
     */

    public final IDataSource resource(final String path) {
        return resourceManager.resolve(path);
    }

    public final <E extends IExtension> IExtensionPool<E> extension(final Class<E> type, final boolean instantiate) {
        return new ExtensionPoolImpl<>(this, type, instantiate);
    }

    public final <E extends IExtension> IExtensionPool<E> extension(final Class<? extends IExtension> extensionType, final Class<E> type,
        final boolean instantiate) {
        return new ExtensionPoolImpl<>(this, extensionType, type, instantiate);
    }

    /*
     * Core
     */

    private final void onCoreLoad() throws Throwable {
        messageManager = new MessageManager();
        argumentRegistry = new ArgumentRegistry();
    }

    private final void onCoreEnable() throws Throwable {
        new BasePluginListener(this);
        setupConditionMap();
        registerMessages();
        setupArgumentRegistry();
        setupConfigs();
    }

    private final void onCorePostEnable() throws Throwable {
        registerListeners();
    }

    private final void setupConditionMap() {
        conditionMap = new ConditionMapImpl();
        onConditionMapSetup(conditionMap);
        conditionMap.lock();
    }

    private final void setupArgumentRegistry() {
        argumentRegistry.setProvider(new LoggerArgumentProvider(logger));
        argumentRegistry.registerArgumentType(UUIDArgument.class);
        onArgumentSetup(argumentRegistry);
    }
    
    private final void setupConfigs() {
        configManager = new ConfigManager(this);
        configManager.reload();
    }

    private final void registerListeners() {
        final IExtensionPool<IListenerExtension> pool = extension(IListenerExtension.class, true);
        final PluginManager pluginManager = getServer().getPluginManager();
        pool.callInstances(listener -> pluginManager.registerEvents(listener, this));
    }

    private final void registerMessages() {
        final IExtensionPool<IMessageExtension> pool = extension(IMessageExtension.class, false);
        final SimpleMessageProviderFactory factory = new SimpleMessageProviderFactory();
        pool.callClasses(extension -> {
            if (extension.isEnum()) {
                if (!IMessageDefinition.class.isAssignableFrom(extension)) {
                    return;
                }
                messageManager.register(new EnumMessageSource((Class) extension, factory));
                return;
            }
            messageManager.register(new AnnotationMessageSource(extension, factory));
        });
    }

    private final void onCoreReady() throws Throwable {
        pagedInventoryRegistry = new PagedInventoryRegistry(this);
    }

    private final void onCoreDisable() throws Throwable {
        HandlerList.unregisterAll();
        clearFields();
    }

    private final void clearFields() {
        argumentRegistry = null;
        messageManager = null;
    }

    /*
     * Abstraction
     */

    protected void onPluginLoad() throws Throwable {}

    protected void onPluginEnable() throws Throwable {}

    protected void onPluginReady() throws Throwable {}

    protected void onPluginDisable() throws Throwable {}

    protected void onConditionMapSetup(final IConditionMap conditionMap) {}

    protected void onArgumentSetup(final ArgumentRegistry registry) {}

    /*
     * Getter
     */

    public final Path jarRoot() {
        return jarRoot;
    }

    public final ISimpleLogger logger() {
        return logger;
    }

    public final MessageManager messageManager() {
        return messageManager;
    }

    public final ArgumentRegistry argumentRegistry() {
        return argumentRegistry;
    }

    public final IConditionMap conditionMap() {
        return conditionMap;
    }
    
    public final ConfigManager configManager() {
        return configManager;
    }

    public final PagedInventoryRegistry pagedInventoryRegistry() {
        return pagedInventoryRegistry;
    }

}
