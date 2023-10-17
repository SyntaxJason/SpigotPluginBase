package me.lauriichan.minecraft.pluginbase.resource;

import me.lauriichan.minecraft.pluginbase.BasePlugin;
import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public interface ISourceProvider {

    /**
     * Provides a data source related to the path
     * 
     * @param  plugin the resource owner
     * @param  path   the path
     * 
     * @return        the data source
     */
    IDataSource provide(BasePlugin<?> plugin, String path);

}
