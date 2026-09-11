package dev.matthiesen.cobblehardcoremon.common.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PartyEntry;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokeHealthStatus;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokePartySlot;
import dev.matthiesen.matthiesen_core.common.utility.SoundsPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public final class PlayerPokeParty {
    private final ServerPlayer serverPlayer;
    private final PlayerPartyStore partyStore;

    public PlayerPokeParty(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
        this.partyStore = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
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

    public void alertPlayerAndRemovedPokemon(Pokemon pokemon) {
        if (partyStore.remove(pokemon)) {
            MutableComponent message = pokemon.getDisplayName(false);
            message = message.append(" has fainted and is not holding a totem, and has been removed from your party.");
            serverPlayer.sendSystemMessage(message.withStyle(ChatFormatting.RED));
        } else {
            CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to remove fainted Pokemon from player party: " + pokemon.getDisplayName(false).getString());
        }
    }

    public boolean popPokemonTotem(Pokemon pokemon) {
        if (pokemon.heldItem().is(Items.TOTEM_OF_UNDYING)) {
            pokemon.heal();
            pokemon.removeHeldItem();
            MutableComponent message = pokemon.getDisplayName(false);
            message = message.append(" was holding a totem, which has been consumed to prevent it from being removed from your party.");
            serverPlayer.sendSystemMessage(message.withStyle(ChatFormatting.GOLD));
            new SoundsPlayer(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value()).play(serverPlayer);
            return true;
        }
        return false;
    }
}
