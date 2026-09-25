package dev.matthiesen.cobblehardcoremon.common.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import dev.matthiesen.cobblehardcoremon.common.config.CobbleHardcoreMonConfig;
import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.cobblehardcoremon.common.data.SoulLinkDataEntry;
import dev.matthiesen.cobblehardcoremon.common.handlers.SoulLink;
import dev.matthiesen.cobblehardcoremon.common.registry.PermissionsRegistry;
import dev.matthiesen.matthiesen_core.common.utility.chat.ChatTableBuilder;
import dev.matthiesen.matthiesen_core.common.utility.commands.CommandBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public final class SoulLinkCommands {
    public static final CommandBuilder ROOT = CommandBuilder.create("soulLink")
            .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SOULLINK_PERMISSION))
            .executes(SoulLinkCommands::getSoulLinkSelf)
            .then(CommandBuilder.create("invite")
                    .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SOULLINK_INVITE_PERMISSION))
                    .argument("player", EntityArgument.player(), arg -> arg.executes(SoulLinkCommands::inviteSoulLink)))
            .then(CommandBuilder.create("accept")
                    .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SOULLINK_ACCEPT_PERMISSION))
                    .argument("player", EntityArgument.player(), arg -> arg.executes(SoulLinkCommands::acceptSoulLink)))
            .then(CommandBuilder.create("decline")
                    .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SOULLINK_DECLINE_PERMISSION))
                    .argument("player", EntityArgument.player(), arg -> arg.executes(SoulLinkCommands::declineSoulLink)))
            .then(CommandBuilder.create("remove")
                    .requires(source -> PermissionsRegistry.checkPermission(source, PermissionsRegistry.COMMAND_SOULLINK_REMOVE_PERMISSION))
                    .executes(SoulLinkCommands::removeSoulLink));

    public static void appendHelpInfo(ChatTableBuilder builder, CommandContext<CommandSourceStack> ctx) {
        boolean hasRootPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SOULLINK_PERMISSION);
        boolean hasInvitePermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SOULLINK_INVITE_PERMISSION);
        boolean hasAcceptPermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SOULLINK_ACCEPT_PERMISSION);
        boolean hasDeclinePermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SOULLINK_DECLINE_PERMISSION);
        boolean hasRemovePermission = PermissionsRegistry.checkPermission(ctx.getSource(), PermissionsRegistry.COMMAND_SOULLINK_REMOVE_PERMISSION);
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;

        if (PermissionsRegistry.hasAnyPermission(List.of(
                hasRootPermission, hasInvitePermission, hasAcceptPermission, hasDeclinePermission, hasRemovePermission
        ))) {
            builder.addSection(config.messages_soulLink_helpTitle.get());
        }

        if (hasRootPermission) {
            builder.addRow("/hardcoremon soulLink", config.messages_soulLink_status.get());
        }
        if (hasInvitePermission) {
            builder.addRow("/hardcoremon soulLink invite <player>", config.messages_soulLink_invitePlayer.get());
        }
        if (hasAcceptPermission) {
            builder.addRow("/hardcoremon soulLink accept <player>", config.messages_soulLink_acceptInvite.get());
        }
        if (hasDeclinePermission) {
            builder.addRow("/hardcoremon soulLink decline <player>", config.messages_soulLink_declineInvite.get());
        }
        if (hasRemovePermission) {
            builder.addRow("/hardcoremon soulLink remove", config.messages_soulLink_removeSoulLink.get());
        }
    }

    private static int getSoulLinkSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer sourcePlayer = ctx.getSource().getPlayerOrException();
            SoulLinkDataEntry soulLink = PlayerData.getSoulLinkByPlayerUUID(sourcePlayer.getUUID());
            var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
            if (soulLink == null) {
                ctx.getSource().sendSystemMessage(Component.literal(config.messages_soulLink_getSoulLinkSelf_null.get()).withStyle(ChatFormatting.YELLOW));
                return 1;
            }

            UUID partnerUUID = SoulLink.getLinkedPlayerUUID(sourcePlayer.getUUID());
            if (partnerUUID == null) {
                ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(config.messages_soulLink_getSoulLinkSelf_invalid.get()));
                return 0;
            }

            String partnerName = SoulLink.getPlayerName(partnerUUID);
            String message = config.messages_soulLink_getSoulLinkSelf_valid.get()
                    .replace("{partner}", partnerName);
            ctx.getSource().sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }

    private static int inviteSoulLink(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer sourcePlayer = ctx.getSource().getPlayerOrException();
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            String validationError = SoulLink.getInviteValidationError(sourcePlayer, targetPlayer);
            if (validationError != null) {
                ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(validationError));
                return 0;
            }

            SoulLink.createInvite(sourcePlayer, targetPlayer);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }

    private static int acceptSoulLink(CommandContext<CommandSourceStack> ctx) {
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        try {
            ServerPlayer targetPlayer = ctx.getSource().getPlayerOrException();
            ServerPlayer sourcePlayer = EntityArgument.getPlayer(ctx, "player");
            String validationError = SoulLink.getAcceptValidationError(sourcePlayer.getUUID(), targetPlayer.getUUID());
            if (validationError != null) {
                ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(validationError));
                return 0;
            }

            if (!SoulLink.acceptInvite(sourcePlayer.getUUID(), targetPlayer.getUUID())) {
                ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(config.messages_soulLink_invites_noPendingInvites.get()));
                return 0;
            }

            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(config.messages_soulLink_invites_invalidUUID.get()));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }

    private static int declineSoulLink(CommandContext<CommandSourceStack> ctx) {
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        try {
            ServerPlayer targetPlayer = ctx.getSource().getPlayerOrException();
            ServerPlayer sourcePlayer = EntityArgument.getPlayer(ctx, "player");

            if (!SoulLink.declineInvite(sourcePlayer.getUUID(), targetPlayer.getUUID())) {
                ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(config.messages_soulLink_invites_noPendingInvites.get()));
                return 0;
            }

            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(config.messages_soulLink_invites_invalidUUID.get()));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }

    private static int removeSoulLink(CommandContext<CommandSourceStack> ctx) {
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        try {
            ServerPlayer sourcePlayer = ctx.getSource().getPlayerOrException();
            UUID soulLinkUUID = PlayerData.getPlayerDataEntry(sourcePlayer.getUUID()).getSoulLinkMapUUID();
            if (soulLinkUUID == null) {
                ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(config.messages_soulLink_remove_failed_noActiveLink.get()));
                return 0;
            }

            if (!SoulLink.removeSoulLink(sourcePlayer.getUUID())) {
                ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(config.messages_soulLink_remove_failed.get()));
                return 0;
            }

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(CobbleHardcoreMonConfig.getErrorComponent(e.getMessage()));
            return 0;
        }
    }
}
