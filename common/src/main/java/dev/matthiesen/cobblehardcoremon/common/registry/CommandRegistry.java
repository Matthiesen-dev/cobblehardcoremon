package dev.matthiesen.cobblehardcoremon.common.registry;

import dev.matthiesen.cobblehardcoremon.common.commands.CobbleHardcoreMonCommands;
import dev.matthiesen.matthiesen_core.common.registry.AbstractCommandRegistry;

public final class CommandRegistry extends AbstractCommandRegistry {
    private static final CommandRegistry INSTANCE = new CommandRegistry();

    public static void init() {
        INSTANCE.register(CobbleHardcoreMonCommands.CMD);
    }
}
