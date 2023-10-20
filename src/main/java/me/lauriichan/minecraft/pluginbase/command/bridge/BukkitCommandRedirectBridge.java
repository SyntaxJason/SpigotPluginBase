package me.lauriichan.minecraft.pluginbase.command.bridge;

import java.util.function.BiFunction;

import org.bukkit.command.CommandSender;

import me.lauriichan.laylib.command.CommandManager;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.minecraft.pluginbase.command.BukkitActor;
import me.lauriichan.minecraft.pluginbase.command.processor.IBukkitCommandProcessor;

final class BukkitCommandRedirectBridge<A extends BukkitActor<?>> extends BukkitCommandBridge<A> {

    private final String prefix;

    public BukkitCommandRedirectBridge(final IBukkitCommandProcessor processor, final CommandManager commandManager,
        final MessageManager messageManager, final String prefix,
        final BiFunction<CommandSender, MessageManager, A> actorBuilder) {
        super(processor, commandManager, messageManager, actorBuilder);
        this.prefix = prefix + ':';
    }

    @Override
    protected String getCommandName(String label, final String[] args, final boolean isForSuggestions) {
        while (label.startsWith("/")) {
            label = label.substring(1);
        }
        if (label.startsWith(prefix)) {
            label = label.substring(prefix.length());
        }
        return label;
    }

    @Override
    protected String[] getCommandArguments(final String[] args, final boolean isForSuggestions) {
        return args;
    }

}
