package me.lauriichan.minecraft.pluginbase.command.bridge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.minecraft.pluginbase.util.ReflectionUtil;

final class BukkitCommandReflection {

    private static final MethodHandle pluginCommandConstructor = findConstructor(PluginCommand.class, String.class, Plugin.class);
    private static final MethodHandle craftServerGetCommandMap = findMethod(
        ClassUtil.findClass(ReflectionUtil.craftClassPath("CraftServer")), "getCommandMap", SimpleCommandMap.class);
    private static final MethodHandle commandMapGetCommands = findGetter(SimpleCommandMap.class, "knownCommands", Map.class);

    private BukkitCommandReflection() {
        throw new UnsupportedOperationException();
    }

    /*
     * Implementation
     */

    public static SimpleCommandMap getCommandMap() {
        try {
            return (SimpleCommandMap) craftServerGetCommandMap.invoke(Bukkit.getServer());
        } catch (final Throwable e) {
            return null;
        }
    }

    public static PluginCommand createCommand(final String name, final Plugin plugin) {
        try {
            return (PluginCommand) pluginCommandConstructor.invoke(name, plugin);
        } catch (final Throwable e) {
            return null;
        }
    }

    public static Map<String, Command> getCommands(final SimpleCommandMap map) {
        try {
            return (Map<String, Command>) commandMapGetCommands.invoke(map);
        } catch (final Throwable e) {
            return null;
        }
    }

    /*
     * Utils
     */

    private static MethodHandle findGetter(final Class<?> clazz, final String name, final Class<?> rtrnType) {
        try {
            return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).findGetter(clazz, name, rtrnType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private static MethodHandle findConstructor(final Class<?> clazz, final Class<?>... arguments) {
        try {
            return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).findConstructor(clazz,
                MethodType.methodType(void.class, arguments));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            return null;
        }
    }

    private static MethodHandle findMethod(final Class<?> clazz, final String name, final Class<?> rtrnType) {
        try {
            return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).findVirtual(clazz, name, MethodType.methodType(rtrnType));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            return null;
        }
    }

}
