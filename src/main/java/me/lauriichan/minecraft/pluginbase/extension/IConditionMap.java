package me.lauriichan.minecraft.pluginbase.extension;

public interface IConditionMap {

    boolean value(String property);

    void value(String property, boolean value);

    boolean set(String property);

    void unset(String property);

    boolean locked();

}
