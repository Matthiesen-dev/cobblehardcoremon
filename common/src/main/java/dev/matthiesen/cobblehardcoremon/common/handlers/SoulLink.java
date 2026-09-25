package dev.matthiesen.cobblehardcoremon.common.handlers;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import dev.matthiesen.cobblehardcoremon.common.config.CobbleHardcoreMonConfig;
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
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        if (sourcePlayer.getUUID().equals(targetPlayer.getUUID())) {
            return config.messages_soulLink_inviteValidationError_self.get();
        }
        if (PlayerData.getSoulLinkByPlayerUUID(sourcePlayer.getUUID()) != null) {
            return config.messages_soulLink_inviteValidationError_selfAlreadyLinked.get();
        }
        if (PlayerData.getSoulLinkByPlayerUUID(targetPlayer.getUUID()) != null) {
            return config.messages_soulLink_inviteValidationError_otherAlreadyLinked.get()
                    .replace("{player}", targetPlayer.getName().getString());
        }
        if (hasPendingInviteBetween(sourcePlayer.getUUID(), targetPlayer.getUUID())) {
            return config.messages_soulLink_inviteValidationError_alreadyInvited.get();
        }
        return null;
    }

    public static String getAcceptValidationError(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        if (PlayerData.getSoulLinkByPlayerUUID(sourcePlayerUUID) != null) {
            return config.messages_soulLink_inviteValidationError_selfAlreadyLinked.get();
        }
        if (PlayerData.getSoulLinkByPlayerUUID(targetPlayerUUID) != null) {
            return config.messages_soulLink_inviteValidationError_otherAlreadyLinked.get()
                    .replace("{player}", getPlayerName(targetPlayerUUID));
        }
        return null;
    }

    public static boolean declineInvite(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        SoulLinkInvite inviteToDecline = findInvite(sourcePlayerUUID, targetPlayerUUID);
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        if (inviteToDecline == null) {
            return false;
        }

        pendingInvites.remove(inviteToDecline);

        ServerUser sourcePlayer = new ServerUser(sourcePlayerUUID);
        ServerUser targetPlayer = new ServerUser(targetPlayerUUID);

        if (sourcePlayer.isOnline()) {
            sourcePlayer.getOnlinePlayer().sendSystemMessage(Component.literal(config.messages_soulLink_declineInvite_source.get().replace("{player}", targetPlayer.getUsername())).withStyle(ChatFormatting.RED));
        }
        if (targetPlayer.isOnline()) {
            targetPlayer.getOnlinePlayer().sendSystemMessage(Component.literal(config.messages_soulLink_declineInvite_target.get().replace("{player}", sourcePlayer.getUsername())).withStyle(ChatFormatting.RED));
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
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        SoulLinkInvite invite = new SoulLinkInvite(sourcePlayer.getUUID(), targetPlayer.getUUID(), 600);

        String sourceMessage = config.messages_soulLink_createInvite_source.get()
                .replace("{player}", targetPlayer.getName().getString());
        sourcePlayer.sendSystemMessage(
                Component.literal(sourceMessage).withStyle(ChatFormatting.GREEN));

        Style acceptStyle = Style.EMPTY
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hardcoremon soulLink accept " + sourcePlayer.getUUID()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(config.messages_soulLink_createInvite_targetAccept.get())));

        Style declineStyle = Style.EMPTY
                .withColor(ChatFormatting.RED)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hardcoremon soulLink decline " + sourcePlayer.getUUID()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(config.messages_soulLink_createInvite_targetDecline.get())));

        String targetMessageBase = config.messages_soulLink_createInvite_targetMessage_base.get()
                .replace("{player}", sourcePlayer.getName().getString());
        MutableComponent targetMessage = Component.literal(targetMessageBase)
                .append(Component.literal(config.messages_soulLink_createInvite_targetMessage_accept.get()).withStyle(acceptStyle))
                .append(Component.literal(" "))
                .append(Component.literal(config.messages_soulLink_createInvite_targetMessage_decline.get()).withStyle(declineStyle));

        targetPlayer.sendSystemMessage(targetMessage);
        pendingInvites.add(invite);
    }

    public static void notifyExpiredInvites(List<SoulLinkInvite> expiredInvites) {
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        for (SoulLinkInvite invite : expiredInvites) {
            ServerUser sourcePlayer = new ServerUser(invite.getSourcePlayer());
            ServerUser targetPlayer = new ServerUser(invite.getTargetPlayer());

            if (sourcePlayer.isOnline()) {
                String message = config.messages_soulLink_expired.get().replace("{player}", targetPlayer.getUsername());
                sourcePlayer.getOnlinePlayer().sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
            }

            if (targetPlayer.isOnline()) {
                String message = config.messages_soulLink_expired.get().replace("{player}", sourcePlayer.getUsername());
                targetPlayer.getOnlinePlayer().sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
            }
        }
    }

    private static void createSoulLink(UUID sourcePlayerUUID, UUID targetPlayerUUID) {
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        PlayerData.createSoulLink(sourcePlayerUUID, targetPlayerUUID);

        ServerUser sourcePlayer = new ServerUser(sourcePlayerUUID);
        ServerUser targetPlayer = new ServerUser(targetPlayerUUID);

        if (sourcePlayer.isOnline()) {
            String message = config.messages_soulLink_active.get().replace("{player}", targetPlayer.getUsername());
            sourcePlayer.getOnlinePlayer().sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GREEN));
        }
        if (targetPlayer.isOnline()) {
            String message = config.messages_soulLink_active.get().replace("{player}", sourcePlayer.getUsername());
            targetPlayer.getOnlinePlayer().sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GREEN));
        }
    }

    private static void notifySoulLinkRemoved(UUID playerAUUID, UUID playerBUUID) {
        var config = CobbleHardcoreMonConfig.SERVER_CONFIG;
        ServerUser playerA = new ServerUser(playerAUUID);
        ServerUser playerB = new ServerUser(playerBUUID);

        if (playerA.isOnline()) {
            String message = config.messages_soulLink_removed.get().replace("{player}", playerB.getUsername());
            playerA.getOnlinePlayer().sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
        }
        if (playerB.isOnline()) {
            String message = config.messages_soulLink_removed.get().replace("{player}", playerA.getUsername());
            playerB.getOnlinePlayer().sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.YELLOW));
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
