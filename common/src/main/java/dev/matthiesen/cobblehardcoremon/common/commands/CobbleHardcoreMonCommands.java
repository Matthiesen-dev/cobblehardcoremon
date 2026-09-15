package dev.matthiesen.cobblehardcoremon.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.cobblehardcoremon.common.commands.subcommands.HealthLinkCommands;
import dev.matthiesen.cobblehardcoremon.common.registry.PermissionsRegistry;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class CobbleHardcoreMonCommands implements CoreCommand {
    public static final CobbleHardcoreMonCommands CMD = new CobbleHardcoreMonCommands();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        var rootCommand = CommandBuilder.create("hardcoremon")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_ROOT_PERMISSION))
                .executes(this::help)
                .then(HealthLinkCommands.SET_HEALTH_LINK)
                .then(HealthLinkCommands.FORCE_HEALTH_LINK);

        commandDispatcher.register(rootCommand.build());
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        try {
            ChatTableBuilder helpTable = new ChatTableBuilder("CobbleHardcoreMon Commands");

            HealthLinkCommands.appendHelpInfo(helpTable, ctx);

            ctx.getSource().sendSystemMessage(helpTable.build());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }
}
