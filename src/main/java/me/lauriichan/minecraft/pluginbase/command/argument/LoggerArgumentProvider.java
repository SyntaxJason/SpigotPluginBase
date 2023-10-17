package me.lauriichan.minecraft.pluginbase.command.argument;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IProviderArgumentType;
import me.lauriichan.laylib.logger.ISimpleLogger;

public final class LoggerArgumentProvider implements IProviderArgumentType<ISimpleLogger> {

    private final ISimpleLogger logger;

    public LoggerArgumentProvider(final ISimpleLogger logger) {
        this.logger = logger;
    }

    @Override
    public ISimpleLogger provide(final Actor<?> actor) {
        return logger;
    }

}
