package me.lauriichan.minecraft.pluginbase.config;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;

@ExtensionPoint
public interface IConfigExtension extends IExtension {

    String path();

    IConfigHandler handler();

    default boolean isModified() {
        return false;
    }

    default void onLoad(final Configuration configuration) throws Exception {}

    default void onSave(final Configuration configuration) throws Exception {}

}
