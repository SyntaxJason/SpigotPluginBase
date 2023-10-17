package me.lauriichan.minecraft.pluginbase;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

final class BasePluginListener implements Listener {

    private final BasePlugin<?> plugin;

    BasePluginListener(final BasePlugin<?> plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLoad(final ServerLoadEvent event) {
        plugin.onReady();
    }

}
