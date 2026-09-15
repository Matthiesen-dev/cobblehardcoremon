package dev.matthiesen.cobblehardcoremon.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.cobblehardcoremon.common.registry.PermissionsRegistry;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public final class CobbleHardcoreMonCommands implements CoreCommand {
    public static final CobbleHardcoreMonCommands CMD = new CobbleHardcoreMonCommands();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        var setHealthLinkSelfCommand = CommandBuilder.create("setHealthLink")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SETHEALTHLINK_PERMISSION))
                .executes(this::healthLinkSelf);

        var forceHealthLinkOtherCommand = CommandBuilder.create("forceHealthLink")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION))
                .argument("player", EntityArgument.player(), arg -> arg
                        .executes(this::forceHealthLinkOther)
                );

        var rootCommand = CommandBuilder.create("hardcoremon")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_ROOT_PERMISSION))
                .executes(this::help)
                .then(setHealthLinkSelfCommand)
                .then(forceHealthLinkOtherCommand);

        commandDispatcher.register(rootCommand.build());
    }

    // TODO: Implement the actual command logic for this commands
    public int healthLinkSelf(CommandContext<CommandSourceStack> ctx) {
        return 1;
    }

    // TODO: Implement the actual command logic for this commands
    public int forceHealthLinkOther(CommandContext<CommandSourceStack> ctx) {
        return 1;
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        ChatTableBuilder helpTable = new ChatTableBuilder("CobbleHardcoreMon Commands");

        if (PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SETHEALTHLINK_PERMISSION)) {
            helpTable.addRow("/hardcoremon setHealthLink", "Toggle health link for your party");
        }
        if (PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION)) {
            helpTable.addRow("/hardcoremon forceHealthLink <player>", "Force health link for another player's party");
        }

        ctx.getSource().sendSystemMessage(helpTable.build());
        return 1;
    }
}
