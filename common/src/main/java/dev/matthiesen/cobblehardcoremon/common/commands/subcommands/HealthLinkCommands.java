package dev.matthiesen.cobblehardcoremon.common.commands.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.cobblehardcoremon.common.registry.PermissionsRegistry;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class HealthLinkCommands {
    public static final CommandBuilder SET_HEALTH_LINK = CommandBuilder.create("setHealthLink")
            .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SETHEALTHLINK_PERMISSION))
            .argument("value", StringArgumentType.word(), arg -> arg
                    .executes(HealthLinkCommands::healthLinkSelf)
            );
    public static final CommandBuilder FORCE_HEALTH_LINK = CommandBuilder.create("forceHealthLink")
            .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION))
            .argument("player", EntityArgument.player(), arg -> arg
                    .then(Commands.argument("value", StringArgumentType.word())
                            .executes(HealthLinkCommands::forceHealthLinkOther)
                    )
            );

    public static void appendHelpInfo(ChatTableBuilder builder, CommandContext<CommandSourceStack> ctx) {
        boolean hasSetHealthLinkPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SETHEALTHLINK_PERMISSION);
        boolean hasForceHealthLinkPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION);

        if (PermissionsRegistry.hasAnyPermission(List.of(hasSetHealthLinkPermission, hasForceHealthLinkPermission))) {
            builder.addSection("Health Link Commands");
        }

        if (hasSetHealthLinkPermission) {
            builder.addRow("/hardcoremon setHealthLink", "Toggle health link for your party");
        }
        if (hasForceHealthLinkPermission) {
            builder.addRow("/hardcoremon forceHealthLink <player>", "Force health link for another player's party");
        }
    }

    public static boolean StringToBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("yes");
    }

    public static int healthLinkSelf(CommandContext<CommandSourceStack> ctx) {
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

    public static int forceHealthLinkOther(CommandContext<CommandSourceStack> ctx) {
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
}
