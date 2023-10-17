package me.lauriichan.minecraft.pluginbase.listener;

import org.bukkit.event.Listener;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public interface IListenerExtension extends IExtension, Listener {

}
