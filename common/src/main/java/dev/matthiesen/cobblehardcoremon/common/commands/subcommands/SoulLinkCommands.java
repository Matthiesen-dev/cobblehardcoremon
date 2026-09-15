package dev.matthiesen.cobblehardcoremon.common.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
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
    public static final CommandBuilder ROOT = CommandBuilder.create("soul-link")
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

        if (PermissionsRegistry.hasAnyPermission(List.of(
                hasRootPermission, hasInvitePermission, hasAcceptPermission, hasDeclinePermission, hasRemovePermission
        ))) {
            builder.addSection("Soul Link Commands");
        }

        if (hasRootPermission) {
            builder.addRow("/hardcoremon soul-link", "Show your current Soul Link status");
        }
        if (hasInvitePermission) {
            builder.addRow("/hardcoremon soul-link invite <player>", "Invite another player to Soul Link");
        }
        if (hasAcceptPermission) {
            builder.addRow("/hardcoremon soul-link accept <player>", "Accept a pending Soul Link invite");
        }
        if (hasDeclinePermission) {
            builder.addRow("/hardcoremon soul-link decline <player>", "Decline a pending Soul Link invite");
        }
        if (hasRemovePermission) {
            builder.addRow("/hardcoremon soul-link remove", "Remove your current Soul Link");
        }
    }

    private static int getSoulLinkSelf(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer sourcePlayer = ctx.getSource().getPlayerOrException();
            SoulLinkDataEntry soulLink = PlayerData.getSoulLinkByPlayerUUID(sourcePlayer.getUUID());
            if (soulLink == null) {
                ctx.getSource().sendSystemMessage(Component.literal("You do not currently have an active Soul Link.").withStyle(ChatFormatting.YELLOW));
                return 1;
            }

            UUID partnerUUID = SoulLink.getLinkedPlayerUUID(sourcePlayer.getUUID());
            if (partnerUUID == null) {
                ctx.getSource().sendFailure(Component.literal("Your Soul Link data is invalid."));
                return 0;
            }

            String partnerName = SoulLink.getPlayerName(partnerUUID);
            ctx.getSource().sendSystemMessage(Component.literal("You are Soul Linked with " + partnerName + ".").withStyle(ChatFormatting.GREEN));
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }

    private static int inviteSoulLink(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer sourcePlayer = ctx.getSource().getPlayerOrException();
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            String validationError = SoulLink.getInviteValidationError(sourcePlayer, targetPlayer);
            if (validationError != null) {
                ctx.getSource().sendFailure(Component.literal(validationError));
                return 0;
            }

            SoulLink.createInvite(sourcePlayer, targetPlayer);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }

    private static int acceptSoulLink(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = ctx.getSource().getPlayerOrException();
            ServerPlayer sourcePlayer = EntityArgument.getPlayer(ctx, "player");
            String validationError = SoulLink.getAcceptValidationError(sourcePlayer.getUUID(), targetPlayer.getUUID());
            if (validationError != null) {
                ctx.getSource().sendFailure(Component.literal(validationError));
                return 0;
            }

            if (!SoulLink.acceptInvite(sourcePlayer.getUUID(), targetPlayer.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("No pending Soul Link invite from that player was found."));
                return 0;
            }

            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("The provided player UUID is invalid."));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }

    private static int declineSoulLink(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = ctx.getSource().getPlayerOrException();
            ServerPlayer sourcePlayer = EntityArgument.getPlayer(ctx, "player");

            if (!SoulLink.declineInvite(sourcePlayer.getUUID(), targetPlayer.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("No pending Soul Link invite from that player was found."));
                return 0;
            }

            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("The provided player UUID is invalid."));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }

    private static int removeSoulLink(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer sourcePlayer = ctx.getSource().getPlayerOrException();
            UUID soulLinkUUID = PlayerData.getPlayerDataEntry(sourcePlayer.getUUID()).getSoulLinkMapUUID();
            if (soulLinkUUID == null) {
                ctx.getSource().sendFailure(Component.literal("You do not currently have an active Soul Link to remove."));
                return 0;
            }

            if (!SoulLink.removeSoulLink(sourcePlayer.getUUID())) {
                ctx.getSource().sendFailure(Component.literal("Failed to remove your current Soul Link."));
                return 0;
            }

            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An error occurred while executing the command: " + e.getMessage()));
            return 0;
        }
    }
}
