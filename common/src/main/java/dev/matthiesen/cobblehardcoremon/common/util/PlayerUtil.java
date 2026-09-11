package dev.matthiesen.cobblehardcoremon.common.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
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

public final class PlayerUtil {
    public static PlayerPartyStore getPlayerPartyStore(ServerPlayer player) {
        return Cobblemon.INSTANCE.getStorage().getParty(player);
    }

    public static Map<PokePartySlot, PokeHealthStatus> getPlayerPartyStatus(ServerPlayer player) {
        PlayerPartyStore playerPartyStore = getPlayerPartyStore(player);
        Map<PokePartySlot, PokeHealthStatus> partyStatusMap = new HashMap<>(PokePartySlot.getMaxSlots());
        for (int i = 0; i < PokePartySlot.getMaxSlots(); i++) {
            Pokemon partyPokemon = playerPartyStore.get(i);
            PokeHealthStatus status;
            if (partyPokemon == null) {
                status = PokeHealthStatus.EMPTY_SLOT;
            } else {
                status = partyPokemon.isFainted() ? PokeHealthStatus.FAINTED : PokeHealthStatus.HEALTHY;
            }
            partyStatusMap.put(PokePartySlot.fromIndex(i), status);
        }
        return partyStatusMap;
    }

    public static void alertPlayerAndRemovedPokemon(ServerPlayer player, Pokemon pokemon) {
        if (getPlayerPartyStore(player).remove(pokemon)) {
            MutableComponent message = pokemon.getDisplayName(false);
            message = message.append(" has fainted and is not holding a totem, and has been removed from your party.");
            player.sendSystemMessage(message.withStyle(ChatFormatting.RED));
        } else {
            CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to remove fainted Pokemon from player party: " + pokemon.getDisplayName(false).getString());
        }
    }

    public static boolean popPokemonTotem(ServerPlayer player, Pokemon pokemon) {
        if (pokemon.heldItem().is(Items.TOTEM_OF_UNDYING)) {
            pokemon.removeHeldItem();
            pokemon.heal();
            MutableComponent message = pokemon.getDisplayName(false);
            message = message.append(" was holding a totem, which has been consumed to prevent it from being removed from your party.");
            player.sendSystemMessage(message.withStyle(ChatFormatting.GOLD));
            new SoundsPlayer(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value()).play(player);
            return true;
        }
        return false;
    }
}
