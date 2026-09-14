package dev.matthiesen.cobblehardcoremon.common.handlers;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokePartySlot;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerBattles {
    public static final Map<UUID, Map<UUID, Set<UUID>>> trackedBattles = new HashMap<>();

    public static void battleStartedPost(BattleStartedEvent.Post event) {
        PokemonBattle battle = event.getBattle();
        Map<UUID, Set<UUID>> faintedPokemonByPlayer = new HashMap<>();

        for (ServerPlayer player : battle.getPlayers()) {
            faintedPokemonByPlayer.put(player.getUUID(), new HashSet<>());
        }

        trackedBattles.put(battle.getBattleId(), faintedPokemonByPlayer);
    }

    public static void battlePokemonFainted(BattleFaintedEvent event) {
        PokemonBattle battle = event.getBattle();
        BattlePokemon faintedPokemon = event.getKilled();

        Map<UUID, Set<UUID>> trackedBattle = trackedBattles.get(battle.getBattleId());
        if (trackedBattle == null) {
            return;
        }

        for (UUID playerUUID : faintedPokemon.getActor().getPlayerUUIDs()) {
            trackedBattle
                    .computeIfAbsent(playerUUID, ignored -> new HashSet<>())
                    .add(faintedPokemon.getUuid());
        }
    }

    public static void battleVictory(BattleVictoryEvent event) {
        resolveTrackedBattle(event.getBattle());
    }

    public static void battleFled(BattleFledEvent event) {
        resolveTrackedBattle(event.getBattle());
    }

    private static void resolveTrackedBattle(PokemonBattle battle) {
        Map<UUID, Set<UUID>> trackedBattle = trackedBattles.remove(battle.getBattleId());
        if (trackedBattle == null) {
            return;
        }

        for (ServerPlayer player : battle.getPlayers()) {
            Set<UUID> faintedPokemon = trackedBattle.get(player.getUUID());
            if (faintedPokemon == null || faintedPokemon.isEmpty()) {
                continue;
            }

            PlayerPartyStore partyStore = PlayerExtensionsKt.party(player);
            PlayerPokeParty playerPokeParty = new PlayerPokeParty(player);
            for (UUID pokemonUUID : new HashSet<>(faintedPokemon)) {
                Pokemon partyPokemon = findPokemonByUuid(partyStore, pokemonUUID);
                if (partyPokemon == null || !partyPokemon.isFainted()) {
                    continue;
                }

                if (playerPokeParty.popPokemonTotem(partyPokemon)) {
                    continue;
                }

                playerPokeParty.alertPlayerAndRemovedPokemon(partyPokemon);
            }
        }
    }

    private static Pokemon findPokemonByUuid(PlayerPartyStore partyStore, UUID pokemonUUID) {
        for (int slotIndex = 0; slotIndex < PokePartySlot.getMaxSlots(); slotIndex++) {
            Pokemon partyPokemon = partyStore.get(slotIndex);
            if (partyPokemon != null && partyPokemon.getUuid().equals(pokemonUUID)) {
                return partyPokemon;
            }
        }
        return null;
    }
}
