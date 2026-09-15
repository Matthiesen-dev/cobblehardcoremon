package dev.matthiesen.cobblehardcoremon.common.handlers;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.cobblehardcoremon.common.data.SoulLinkDataEntry;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokePartySlot;
import dev.matthiesen.matthiesen_core.common.api.events.server.ServerEvent;
import dev.matthiesen.matthiesen_core.common.utility.player_data.ServerUser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SoulLink {
    public static final List<SoulLinkInvite> pendingInvites = new ArrayList<>();

    public static void tick(@SuppressWarnings("unused") ServerEvent.EndTick event) {
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

    public static String getInviteValidationError(ServerPlayer sourcePlayer, ServerPlayer targetPlayer) {
        if (sourcePlayer.getUUID().equals(targetPlayer.getUUID())) {
            return "You cannot Soul Link with yourself.";
        }
        if (PlayerData.getSoulLinkByPlayerUUID(sourcePlayer.getUUID()) != null) {
            return "You already have an active Soul Link.";
        }
        if (PlayerData.getSoulLinkByPlayerUUID(targetPlayer.getUUID()) != null) {
            return targetPlayer.getName().getString() + " already has an active Soul Link.";
        }
        if (hasPendingInviteBetween(sourcePlayer.getUUID(), targetPlayer.getUUID())) {
            return "A Soul Link invite between you and that player is already pending.";
        }
        return null;
    }

    public static String getAcceptValidationError(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        if (PlayerData.getSoulLinkByPlayerUUID(sourcePlayerUUID) != null) {
            return getPlayerName(sourcePlayerUUID) + " already has an active Soul Link.";
        }
        if (PlayerData.getSoulLinkByPlayerUUID(targetPlayerUUID) != null) {
            return "You already have an active Soul Link.";
        }
        return null;
    }

    public static boolean declineInvite(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        SoulLinkInvite inviteToDecline = findInvite(sourcePlayerUUID, targetPlayerUUID);
        if (inviteToDecline == null) {
            return false;
        }

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
    }

    public static boolean acceptInvite(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        SoulLinkInvite inviteToAccept = findInvite(sourcePlayerUUID, targetPlayerUUID);
        if (inviteToAccept == null) {
            return false;
        }

        pendingInvites.remove(inviteToAccept);
        createSoulLink(sourcePlayerUUID, targetPlayerUUID);
        return true;
    }

    public static boolean removeSoulLink(UUID playerUUID) {
        UUID soulLinkUUID = PlayerData.getPlayerDataEntry(playerUUID).getSoulLinkMapUUID();
        if (soulLinkUUID == null) {
            return false;
        }

        SoulLinkDataEntry entry = PlayerData.getPlayerData().soulLinkDataMap.get(soulLinkUUID);
        if (entry == null) {
            return PlayerData.removeSoulLink(soulLinkUUID);
        }

        boolean removed = PlayerData.removeSoulLink(soulLinkUUID);
        if (removed) {
            notifySoulLinkRemoved(entry.playerA(), entry.playerB());
        }
        return removed;
    }

    public static UUID getLinkedPlayerUUID(UUID playerUUID) {
        SoulLinkDataEntry soulLink = PlayerData.getSoulLinkByPlayerUUID(playerUUID);
        if (soulLink == null) {
            return null;
        }
        if (soulLink.playerA().equals(playerUUID)) {
            return soulLink.playerB();
        }
        if (soulLink.playerB().equals(playerUUID)) {
            return soulLink.playerA();
        }
        return null;
    }

    public static String getPlayerName(UUID playerUUID) {
        ServerUser user = new ServerUser(playerUUID);
        String username = user.getUsername();
        return username != null ? username : playerUUID.toString();
    }

    public static void resolveLinkedSlotRemoval(ServerPlayer sourcePlayer, PokePartySlot slot) {
        UUID linkedPlayerUUID = getLinkedPlayerUUID(sourcePlayer.getUUID());
        if (linkedPlayerUUID == null) {
            return;
        }

        ServerUser linkedUser = new ServerUser(linkedPlayerUUID);
        if (!linkedUser.isOnline()) {
            return;
        }

        ServerPlayer linkedPlayer = linkedUser.getOnlinePlayer();
        if (PlayerExtensionsKt.isPartyBusy(linkedPlayer) || PlayerExtensionsKt.isInBattle(linkedPlayer)) {
            return;
        }

        PlayerPokeParty linkedParty = new PlayerPokeParty(linkedPlayer);
        Pokemon linkedPokemon = linkedParty.getPokemonInSlot(slot);
        if (linkedPokemon == null) {
            return;
        }

        if (linkedParty.popPokemonTotem(linkedPokemon)) {
            return;
        }

        linkedParty.alertPlayerAndRemovedPokemon(linkedPokemon, true, getPlayerName(sourcePlayer.getUUID()));
    }

    public static void createInvite(ServerPlayer sourcePlayer, ServerPlayer targetPlayer) {
        SoulLinkInvite invite = new SoulLinkInvite(sourcePlayer.getUUID(), targetPlayer.getUUID(), 600);

        sourcePlayer.sendSystemMessage(
                Component.literal("You have sent a Soul Link invite to "
                        + targetPlayer.getName().getString()
                        + ". It will expire in 30 seconds."
                ).withStyle(ChatFormatting.GREEN));

        Style acceptStyle = Style.EMPTY
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hardcoremon soulLink accept " + sourcePlayer.getUUID()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to accept the Soul Link invite.")));

        Style declineStyle = Style.EMPTY
                .withColor(ChatFormatting.RED)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hardcoremon soulLink decline " + sourcePlayer.getUUID()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to decline the Soul Link invite.")));

        MutableComponent targetMessage = Component.literal(sourcePlayer.getName().getString() + " has sent you a Soul Link invite. ")
                .append(Component.literal("[Accept]").withStyle(acceptStyle))
                .append(Component.literal(" "))
                .append(Component.literal("[Decline]").withStyle(declineStyle));

        targetPlayer.sendSystemMessage(targetMessage);
        pendingInvites.add(invite);
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

    private static void createSoulLink(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        PlayerData.createSoulLink(sourcePlayerUUID, targetPlayerUUID);

        ServerUser sourcePlayer = new ServerUser(sourcePlayerUUID);
        ServerUser targetPlayer = new ServerUser(targetPlayerUUID);

        if (sourcePlayer.isOnline()) {
            sourcePlayer.getOnlinePlayer().sendSystemMessage(Component.literal("Your Soul Link with " + targetPlayer.getUsername() + " is now active.").withStyle(ChatFormatting.GREEN));
        }
        if (targetPlayer.isOnline()) {
            targetPlayer.getOnlinePlayer().sendSystemMessage(Component.literal("Your Soul Link with " + sourcePlayer.getUsername() + " is now active.").withStyle(ChatFormatting.GREEN));
        }
    }

    private static void notifySoulLinkRemoved(UUID playerAUUID, UUID playerBUUID) {
        ServerUser playerA = new ServerUser(playerAUUID);
        ServerUser playerB = new ServerUser(playerBUUID);

        if (playerA.isOnline()) {
            playerA.getOnlinePlayer().sendSystemMessage(Component.literal("Your Soul Link with " + getPlayerName(playerBUUID) + " has been removed.").withStyle(ChatFormatting.YELLOW));
        }
        if (playerB.isOnline()) {
            playerB.getOnlinePlayer().sendSystemMessage(Component.literal("Your Soul Link with " + getPlayerName(playerAUUID) + " has been removed.").withStyle(ChatFormatting.YELLOW));
        }
    }

    private static boolean hasPendingInviteBetween(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        return findInvite(sourcePlayerUUID, targetPlayerUUID) != null || findInvite(targetPlayerUUID, sourcePlayerUUID) != null;
    }

    private static SoulLinkInvite findInvite(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        for (SoulLinkInvite invite : pendingInvites) {
            if (invite.getSourcePlayer().equals(sourcePlayerUUID) && invite.getTargetPlayer().equals(targetPlayerUUID)) {
                return invite;
            }
        }
        return null;
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
