package dev.matthiesen.cobblehardcoremon.common.commands.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.cobblehardcoremon.common.config.CobbleHardcoreMonConfig;
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
    public static final CommandBuilder GET_HEALTH_LINK = CommandBuilder.create("getHealthLink")
            .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_GETHEALTHLINK_PERMISSION))
            .executes(HealthLinkCommands::getHealthLinkSelf);
    public static final CommandBuilder FORCE_HEALTH_LINK = CommandBuilder.create("forceHealthLink")
            .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION))
            .argument("player", EntityArgument.player(), arg -> arg
                    .then(Commands.argument("value", StringArgumentType.word())
                            .executes(HealthLinkCommands::forceHealthLinkOther)
                    )
            );
    public static final CommandBuilder GET_HEALTH_LINK_OTHER = CommandBuilder.create("getHealthLinkOther")
            .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_GETHEALTHLINKOTHER_PERMISSION))
            .argument("player", EntityArgument.player(), arg -> arg
                    .executes(HealthLinkCommands::getHealthLinkOther)
            );

    public static void appendHelpInfo(ChatTableBuilder builder, CommandContext<CommandSourceStack> ctx) {
        boolean hasSetHealthLinkPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SETHEALTHLINK_PERMISSION);
        boolean hasGetHealthLinkPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_GETHEALTHLINK_PERMISSION);
        boolean hasForceHealthLinkPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_FORCEHEALTHLINK_PERMISSION);
        boolean hasGetHealthLinkOtherPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_GETHEALTHLINKOTHER_PERMISSION);
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;

        if (PermissionsRegistry.hasAnyPermission(List.of(
                hasSetHealthLinkPermission, hasForceHealthLinkPermission,
                hasGetHealthLinkPermission, hasGetHealthLinkOtherPermission
        ))) {
            builder.addSection(config.messages_healthLink_helpTitle.get());
        }

        if (hasSetHealthLinkPermission) {
            builder.addRow("/hardcoremon setHealthLink <value>", config.messages_healthLink_setHealthLinkText.get());
        }
        if (hasGetHealthLinkPermission) {
            builder.addRow("/hardcoremon getHealthLink", config.messages_healthLink_getHealthLinkText.get());
        }
        if (hasForceHealthLinkPermission) {
            builder.addRow("/hardcoremon forceHealthLink <player> <value>", config.messages_healthLink_forceHealthLinkText.get());
        }
        if (hasGetHealthLinkOtherPermission) {
            builder.addRow("/hardcoremon getHealthLinkOther <player>", config.messages_healthLink_getHealthLinkOtherText.get());
        }
    }

    public static boolean StringToBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("yes");
    }

    public static int getHealthLinkSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            var source = ctx.getSource();
            ServerPlayer targetPlayer = source.getPlayerOrException();
            boolean healthLinkEnabled = PlayerData.getPlayerDataEntry(targetPlayer.getUUID()).healthLinkEnabled();
            var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
            String message = config.messages_healthLink_getHealthLinkSelfResponse.get()
                    .replace("{status}", String.valueOf(healthLinkEnabled));
            source.sendSystemMessage(Component.literal(message));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }

    public static int healthLinkSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            var source = ctx.getSource();
            ServerPlayer targetPlayer = source.getPlayerOrException();
            String value = StringArgumentType.getString(ctx, "value");
            boolean booleanValue = StringToBoolean(value);
            var config = CobbleHardcoreMonConfig.SERVER_CONFIG;

            PlayerData.setHealthLinkEnabled(targetPlayer.getUUID(), booleanValue);
            String message = config.messages_healthLink_healthLinkSelfResponse.get()
                    .replace("{status}", String.valueOf(booleanValue));
            source.sendSystemMessage(Component.literal(message));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }

    public static int forceHealthLinkOther(CommandContext<CommandSourceStack> ctx) {
        try {
            var source = ctx.getSource();
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            String value = StringArgumentType.getString(ctx, "value");
            boolean booleanValue = StringToBoolean(value);
            var config = CobbleHardcoreMonConfig.SERVER_CONFIG;

            PlayerData.setHealthLinkEnabled(targetPlayer.getUUID(), booleanValue);
            String sourceMessage = config.messages_healthLink_forceHealthLinkOtherResponse.get()
                    .replace("{status}", String.valueOf(booleanValue))
                    .replace("{player}", targetPlayer.getName().getString());
            String targetMessage = config.messages_healthLink_forceHealthLinkOtherTarget.get()
                    .replace("{status}", String.valueOf(booleanValue))
                    .replace("{player}", source.getTextName());
            source.sendSystemMessage(Component.literal(sourceMessage));
            targetPlayer.sendSystemMessage(Component.literal(targetMessage));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }

    public static int getHealthLinkOther(CommandContext<CommandSourceStack> ctx) {
        try {
            var source = ctx.getSource();
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            boolean healthLinkEnabled = PlayerData.getPlayerDataEntry(targetPlayer.getUUID()).healthLinkEnabled();
            var config = CobbleHardcoreMonConfig.SERVER_CONFIG;

            String message = config.messages_healthLink_getHealthLinkOtherResponse.get()
                    .replace("{status}", String.valueOf(healthLinkEnabled))
                    .replace("{player}", targetPlayer.getName().getString());
            source.sendSystemMessage(Component.literal(message));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }
}
