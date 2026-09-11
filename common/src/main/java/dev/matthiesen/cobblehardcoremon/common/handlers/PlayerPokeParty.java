package dev.matthiesen.cobblehardcoremon.common.handlers;

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonConfig;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PartyEntry;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokeHealthStatus;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokePartySlot;
import dev.matthiesen.matthiesen_core.common.utility.SoundsPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

import java.util.HashMap;
import java.util.Map;

public final class PlayerPokeParty {
    public static void tick(ServerPlayer serverPlayer) {
        PlayerPokeParty playerInstance = new PlayerPokeParty(serverPlayer);
        for (Map.Entry<PokePartySlot, PartyEntry> entry : playerInstance.getPlayerPartyStatus().entrySet()) {
            PartyEntry partyEntry = entry.getValue();
            if (partyEntry.healthStatus() == PokeHealthStatus.FAINTED) {
                Pokemon faintedPokemon = partyEntry.pokemon();
                if (faintedPokemon != null) {
                    if (!playerInstance.popPokemonTotem(faintedPokemon)) {
                        playerInstance.alertPlayerAndRemovedPokemon(faintedPokemon);
                    }
                } else {
                    CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to find fainted Pokemon in player party at slot: " +
                            entry.getKey().getIndex());
                }
            }
        }
    }

    private final ServerPlayer serverPlayer;
    private final PlayerPartyStore partyStore;

    public PlayerPokeParty(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
        this.partyStore = PlayerExtensionsKt.party(serverPlayer);
    }

    public Map<PokePartySlot, PartyEntry> getPlayerPartyStatus() {
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
    }

    public boolean popPokemonTotem(Pokemon pokemon) {
        if (pokemon.heldItem().is(CobbleHardcoreMonConfig.getTotemItem())) {
            pokemon.heal();
            pokemon.removeHeldItem();
            String pokemonName = pokemon.getDisplayName(false).toString();
            Component chatMessage = Component.literal(
                    CobbleHardcoreMonConfig.SERVER_CONFIG.messages_totemConsumed.get()
                            .replace("{pokemon}", pokemonName)
            ).withStyle(ChatFormatting.GOLD);
            serverPlayer.sendSystemMessage(chatMessage);
            new SoundsPlayer(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value()).play(serverPlayer);
            return true;
        }
        return false;
    }

    public void alertPlayerAndRemovedPokemon(Pokemon pokemon) {
        if (partyStore.remove(pokemon)) {
            String pokemonName = pokemon.getDisplayName(false).toString();
            Component chatMessage = Component.literal(
                    CobbleHardcoreMonConfig.SERVER_CONFIG.messages_pokemonRemoved.get()
                            .replace("{pokemon}", pokemonName)
            ).withStyle(ChatFormatting.RED);
            serverPlayer.sendSystemMessage(chatMessage);
        } else {
            CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to remove fainted Pokemon from player party: " +
                    pokemon.getDisplayName(false).getString());
        }
    }
}
