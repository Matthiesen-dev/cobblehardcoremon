package dev.matthiesen.cobblehardcoremon.common.handlers;

import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.matthiesen_core.common.api.events.server.ServerEvent;
import dev.matthiesen.matthiesen_core.common.utility.player_data.ServerUser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SoulLink {
    public static final List<SoulLinkInvite> pendingInvites = new ArrayList<>();

    public static void tick(@SuppressWarnings("unused") ServerEvent.EndTick event) {
        tickInvites();
    }

    public static boolean declineInvite(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        SoulLinkInvite inviteToDecline = null;
        for (SoulLinkInvite invite : pendingInvites) {
            if (invite.getSourcePlayer().equals(sourcePlayerUUID) && invite.getTargetPlayer().equals(targetPlayerUUID)) {
                inviteToDecline = invite;
                break;
            }
        }

        if (inviteToDecline != null) {
            pendingInvites.remove(inviteToDecline);

            ServerUser sourcePlayer = new ServerUser(sourcePlayerUUID);
            ServerUser targetPlayer = new ServerUser(targetPlayerUUID);

            if (sourcePlayer.isOnline()) {
                sourcePlayer.getOnlinePlayer().sendSystemMessage(Component.literal("Your Soul Link invite to " + targetPlayer.getUsername() + " has been declined.").withStyle(ChatFormatting.RED));
            }
            if (targetPlayer.isOnline()) {
                targetPlayer.getOnlinePlayer().sendSystemMessage(Component.literal("You have declined the Soul Link invite from " + sourcePlayer.getUsername() + ".").withStyle(ChatFormatting.RED));
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean acceptInvite(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        SoulLinkInvite inviteToAccept = null;
        for (SoulLinkInvite invite : pendingInvites) {
            if (invite.getSourcePlayer().equals(sourcePlayerUUID) && invite.getTargetPlayer().equals(targetPlayerUUID)) {
                inviteToAccept = invite;
                break;
            }
        }

        if (inviteToAccept != null) {
            pendingInvites.remove(inviteToAccept);
            PlayerData.createSoulLink(sourcePlayerUUID, targetPlayerUUID);
            return true;
        } else {
            return false;
        }
    }

    public static void tickInvites() {
        List<SoulLinkInvite> expiredInvites = new ArrayList<>();
        for (SoulLinkInvite invite : pendingInvites) {
            invite.decrementTicks();
            if (invite.isExpired()) {
                expiredInvites.add(invite);
            }
        }
        notifyExpiredInvites(expiredInvites);
        pendingInvites.removeAll(expiredInvites);
    }

    public static void notifyExpiredInvites(List<SoulLinkInvite> expiredInvites) {
        for (SoulLinkInvite invite : expiredInvites) {
            ServerUser sourcePlayer = new ServerUser(invite.getSourcePlayer());
            ServerUser targetPlayer = new ServerUser(invite.getTargetPlayer());

            if (sourcePlayer.isOnline()) {
                sourcePlayer.getOnlinePlayer().sendSystemMessage(Component.literal("Your Soul Link invite to " + targetPlayer.getUsername() + " has expired.").withStyle(ChatFormatting.RED));
            }

            if (targetPlayer.isOnline()) {
                targetPlayer.getOnlinePlayer().sendSystemMessage(Component.literal("The Soul Link invite from " + sourcePlayer.getUsername() + " has expired.").withStyle(ChatFormatting.RED));
            }
        }
    }

    public static void createInvite(ServerPlayer sourcePlayer, ServerPlayer targetPlayer) {
        SoulLinkInvite invite = new SoulLinkInvite(sourcePlayer.getUUID(), targetPlayer.getUUID(), 600); // 30 seconds at 20 ticks per second

        sourcePlayer.sendSystemMessage(
                Component.literal("You have sent a Soul Link invite to "
                        + targetPlayer.getName().getString()
                        + ". It will expire in 30 seconds."
                ).withStyle(ChatFormatting.GREEN));

        Style acceptStyle = Style.EMPTY
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hardcoremon soul-link accept " + sourcePlayer.getUUID()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to accept the Soul Link invite.")));

        Style declineStyle = Style.EMPTY
                .withColor(ChatFormatting.RED)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hardcoremon soul-link decline " + sourcePlayer.getUUID()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to decline the Soul Link invite.")));

        MutableComponent targetMessage = Component.literal(sourcePlayer.getName().getString() + " has sent you a Soul Link invite. ")
                .append(Component.literal("[Accept]").withStyle(acceptStyle))
                .append(Component.literal(" "))
                .append(Component.literal("[Decline]").withStyle(declineStyle));

        targetPlayer.sendSystemMessage(targetMessage);
        pendingInvites.add(invite);
    }

    public static class SoulLinkInvite {
        private final UUID sourcePlayer;
        private final UUID targetPlayer;
        private int ticksRemaining;

        public SoulLinkInvite(UUID sourcePlayer, UUID targetPlayer, int ticksRemaining) {
            this.sourcePlayer = sourcePlayer;
            this.targetPlayer = targetPlayer;
            this.ticksRemaining = ticksRemaining;
        }

        public UUID getSourcePlayer() {
            return sourcePlayer;
        }

        public UUID getTargetPlayer() {
            return targetPlayer;
        }

        public void decrementTicks() {
            if (ticksRemaining > 0) {
                ticksRemaining--;
            }
        }

        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
    }
}
