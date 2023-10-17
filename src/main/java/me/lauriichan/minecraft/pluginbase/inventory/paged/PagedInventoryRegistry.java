package me.lauriichan.minecraft.pluginbase.inventory.paged;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.ConditionConstant;

public final class PagedInventoryRegistry {

    private final Object2ObjectArrayMap<Class<? extends IPagedInventoryHandlerExtension>, IPagedInventoryHandlerExtension> handlers = new Object2ObjectArrayMap<>();

    public PagedInventoryRegistry(final BasePlugin<?> plugin) {
        if (!plugin.conditionMap().value(ConditionConstant.ENABLE_GUI)) {
            return;
        }
        plugin.extension(IPagedInventoryHandlerExtension.class, true).callInstances(handler -> {
            handlers.put(handler.getClass().asSubclass(IPagedInventoryHandlerExtension.class), handler);
        });
    }

    public <H extends IPagedInventoryHandlerExtension> H get(final Class<H> type) {
        final IPagedInventoryHandlerExtension extension = handlers.get(type);
        if (extension == null) {
            return null;
        }
        return type.cast(extension);
    }

    public boolean has(final Class<? extends IPagedInventoryHandlerExtension> type) {
        return handlers.containsKey(type);
    }

}
