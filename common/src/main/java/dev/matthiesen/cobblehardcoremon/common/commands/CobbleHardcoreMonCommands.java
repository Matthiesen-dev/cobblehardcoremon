package dev.matthiesen.cobblehardcoremon.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.cobblehardcoremon.common.registry.PermissionsRegistry;
import dev.matthiesen.matthiesen_core.common.api.command.CoreCommand;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CobbleHardcoreMonCommands implements CoreCommand {
    public static final CobbleHardcoreMonCommands CMD = new CobbleHardcoreMonCommands();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        var setHealthLinkSelfCommand = CommandBuilder.create("setHealthLink")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SETHEALTHLINK_PERMISSION))
                .argument("value", StringArgumentType.word(), arg -> arg
                        .executes(this::healthLinkSelf)
                );

        var forceHealthLinkOtherCommand = CommandBuilder.create("forceHealthLink")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION))
                .argument("player", EntityArgument.player(), arg -> arg
                        .then(Commands.argument("value", StringArgumentType.word())
                                .executes(this::forceHealthLinkOther)
                        )
                );

        var rootCommand = CommandBuilder.create("hardcoremon")
                .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_ROOT_PERMISSION))
                .executes(this::help)
                .then(setHealthLinkSelfCommand)
                .then(forceHealthLinkOtherCommand);

        commandDispatcher.register(rootCommand.build());
    }

    public boolean StringToBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("yes");
    }

    public int healthLinkSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            var source = ctx.getSource();
            ServerPlayer targetPlayer = source.getPlayerOrException();
            String value = StringArgumentType.getString(ctx, "value");
            boolean booleanValue = StringToBoolean(value);

            PlayerData.setHealthLinkEnabled(targetPlayer.getUUID(), booleanValue);
            source.sendSystemMessage(Component.literal("Health link for your party has been set to: " + booleanValue));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }

    public int forceHealthLinkOther(CommandContext<CommandSourceStack> ctx) {
        try {
            var source = ctx.getSource();
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            String value = StringArgumentType.getString(ctx, "value");
            boolean booleanValue = StringToBoolean(value);

            PlayerData.setHealthLinkEnabled(targetPlayer.getUUID(), booleanValue);
            source.sendSystemMessage(Component.literal("Health link for " + targetPlayer.getName().getString() + "'s party has been set to: " + booleanValue));
            targetPlayer.sendSystemMessage(Component.literal("Your party's health link has been set to: " + booleanValue + " by " + source.getTextName()));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }

    public int help(CommandContext<CommandSourceStack> ctx) {
        try {
            ChatTableBuilder helpTable = new ChatTableBuilder("CobbleHardcoreMon Commands");

            if (PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SETHEALTHLINK_PERMISSION)) {
                helpTable.addRow("/hardcoremon setHealthLink", "Toggle health link for your party");
            }
            if (PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION)) {
                helpTable.addRow("/hardcoremon forceHealthLink <player>", "Force health link for another player's party");
            }

            ctx.getSource().sendSystemMessage(helpTable.build());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }
}
