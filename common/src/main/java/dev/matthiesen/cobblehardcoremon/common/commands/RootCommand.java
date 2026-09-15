package dev.matthiesen.cobblehardcoremon.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.matthiesen.cobblehardcoremon.common.registry.PermissionsRegistry;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class RootCommand implements CoreCommand {
    public static final RootCommand CMD = new RootCommand();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        var rootCommand = CommandBuilder.create("hardcoremon")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_ROOT_PERMISSION));

        commandDispatcher.register(rootCommand.build());
    }
}
