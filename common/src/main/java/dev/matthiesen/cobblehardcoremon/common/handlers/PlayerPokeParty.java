package dev.matthiesen.cobblehardcoremon.common.handlers;

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonConfig;
import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PartyEntry;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokeHealthStatus;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokePartySlot;
import dev.matthiesen.matthiesen_core.common.api.events.server.PlayerEvent;
import dev.matthiesen.matthiesen_core.common.utility.SoundsPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerPokeParty {
    // Stored as UUID of the Pokemon and the remaining cooldown seconds.
    private static final Map<UUID, Integer> pokemonTotemCooldowns = new HashMap<>();

    public static void handlePlayerTick(PlayerEvent.EndTick event) {
        if (!CobbleHardcoreMonCommon.INSTANCE.isServerRunning()) return; // Only run this logic when the server is running
        ServerPlayer serverPlayer = event.player();

        if (!(serverPlayer.tickCount % 20 == 0)) return; // Only check every second to reduce performance impact
        PlayerPokeParty playerInstance = new PlayerPokeParty(serverPlayer);

        for (Map.Entry<PokePartySlot, PartyEntry> entry : playerInstance.getPlayerPartyStatus().entrySet()) {
            PartyEntry partyEntry = entry.getValue();
            if (partyEntry.healthStatus() == PokeHealthStatus.FAINTED) {
                Pokemon faintedPokemon = partyEntry.pokemon();
                // Check if the player is not in battle and not busy (e.g., in a menu) before attempting to remove the fainted Pokemon
                // We check to verify the player is not in battle or busy to avoid weird race conditions with Cobblemon's battle system and party management.
                if (faintedPokemon != null && playerInstance.playerIsBusy(serverPlayer)) {
                    // Check if the player's Pokemon has a Totem item and if the cooldown has expired
                    if (playerInstance.popPokemonTotem(faintedPokemon)) {
                        // If the Totem was consumed, we can skip the removal process
                        pokemonTotemCooldowns.put(faintedPokemon.getUuid(), 60); // Set a cooldown of 60 seconds before the Pokemon can be removed again
                        return;
                    }

                    // If the Pokemon does not have a totem to pop, verify if the cooldown has expired before removing it from the party
                    Integer cooldown = pokemonTotemCooldowns.get(faintedPokemon.getUuid());
                    if (cooldown == null || cooldown <= 0) {
                        playerInstance.alertPlayerAndRemovedPokemon(faintedPokemon);
                        pokemonTotemCooldowns.remove(faintedPokemon.getUuid());
                    } else {
                        // Decrement the cooldown for the fainted Pokemon
                        pokemonTotemCooldowns.put(faintedPokemon.getUuid(), cooldown - 1);
                    }
                }
            }
        }

        // Update the player's max health based on their current party size if HealthLink is enabled
        if (hasHealthLinkEnabled(serverPlayer) && playerInstance.playerIsBusy(serverPlayer)) {
            HealthLink.overridePlayerMaxHealth(serverPlayer);
        }
    }

    public static boolean hasHealthLinkEnabled(ServerPlayer player) {
        // If the global health link is enabled, all players have it enabled by default
        if (CobbleHardcoreMonConfig.SERVER_CONFIG.globalHealthLinkEnabled.getAsBoolean()) return true;
        PlayerData.PlayerDataEntry entry = PlayerData.getPlayerDataEntry(player.getUUID());
        return entry != null && entry.healthLinkEnabled();
    }

    private final ServerPlayer serverPlayer;
    private final PlayerPartyStore partyStore;

    public PlayerPokeParty(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
        this.partyStore = PlayerExtensionsKt.party(serverPlayer);
    }

    public boolean playerIsBusy(ServerPlayer serverPlayer) {
        return !PlayerExtensionsKt.isPartyBusy(serverPlayer) && !PlayerExtensionsKt.isInBattle(serverPlayer);
    }

    public Map<PokePartySlot, PartyEntry> getPlayerPartyStatus() {
        try {
            Map<PokePartySlot, PartyEntry> partyStatusMap = new HashMap<>(PokePartySlot.getMaxSlots());
            for (int i = 0; i < PokePartySlot.getMaxSlots(); i++) {
                Pokemon partyPokemon = partyStore.get(i);
                PokeHealthStatus status;
                if (partyPokemon == null) {
                    status = PokeHealthStatus.EMPTY_SLOT;
                } else {
                    status = partyPokemon.isFainted() ? PokeHealthStatus.FAINTED : PokeHealthStatus.HEALTHY;
                }
                partyStatusMap.put(PokePartySlot.fromIndex(i), new PartyEntry(partyPokemon, status));
            }
            return partyStatusMap;
        } catch (Exception e) {
            CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to retrieve player party status: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public int getLivingPokemonCount() {
        try {
            int count = 0;
            for (int i = 0; i < PokePartySlot.getMaxSlots(); i++) {
                Pokemon partyPokemon = partyStore.get(i);
                if (partyPokemon != null && !partyPokemon.isFainted()) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to count living Pokemon in player party: " + e.getMessage(), e);
            return 0;
        }
    }

    public boolean popPokemonTotem(Pokemon pokemon) {
        try {
            if (pokemon.heldItem().is(CobbleHardcoreMonConfig.getTotemItem())) {
                pokemon.heal();
                pokemon.removeHeldItem();
                String pokemonName = pokemon.getSpecies().getTranslatedName().getString();
                Component chatMessage = Component.literal(
                        CobbleHardcoreMonConfig.SERVER_CONFIG.messages_totemConsumed.get()
                                .replace("{pokemon}", pokemonName)
                ).withStyle(ChatFormatting.GOLD);
                serverPlayer.sendSystemMessage(chatMessage);
                new SoundsPlayer(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value()).play(serverPlayer);
                return true;
            }
        } catch (Exception e) {
            CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to pop Totem for Pokemon: " + e.getMessage(), e);
        }
        return false;
    }

    public void alertPlayerAndRemovedPokemon(Pokemon pokemon) {
        try {
            if (partyStore.remove(pokemon)) {
                String pokemonName = pokemon.getSpecies().getTranslatedName().getString();
                Component chatMessage = Component.literal(
                        CobbleHardcoreMonConfig.SERVER_CONFIG.messages_pokemonRemoved.get()
                                .replace("{pokemon}", pokemonName)
                ).withStyle(ChatFormatting.RED);
                serverPlayer.sendSystemMessage(chatMessage);
            } else {
                CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to remove fainted Pokemon from player party: " +
                        pokemon.getDisplayName(false).getString());
            }
        } catch (Exception e) {
            CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to send fainted Pokemon removal message to player: " + e.getMessage(), e);
        }
    }
}
