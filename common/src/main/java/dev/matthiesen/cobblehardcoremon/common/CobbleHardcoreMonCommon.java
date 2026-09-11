package dev.matthiesen.cobblehardcoremon.common;

import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PartyEntry;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokeHealthStatus;
import dev.matthiesen.cobblehardcoremon.common.interfaces.PokePartySlot;
import dev.matthiesen.cobblehardcoremon.common.util.PlayerPokeParty;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.api.events.server.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class CobbleHardcoreMonCommon extends AbstractCommonMod {
    public static final String MOD_ID = "cobblehardcoremon";
    public static final String MOD_NAME = "CobbleHardcoreMon";
    public static @Token final String METRICS_TOKEN = "768f3c7f3b02526ca25e65c50a0d6d98";
    public static final CobbleHardcoreMonCommon INSTANCE = new CobbleHardcoreMonCommon();

    public CobbleHardcoreMonCommon() {
        super(MOD_ID, MOD_NAME);
    }

    @Override
    public @Token @NotNull String getMetricsToken() {
        return METRICS_TOKEN;
    }

    public void initialize() {
        super.initialize();

        PlatformEvents.PLAYER_END_TICK.subscribe(this::handlePlayerTick);

        createInfoLog("Initialized");
    }

    public void handlePlayerTick(PlayerEvent.EndTick event) {
        if (!(event.player().tickCount % 20 == 0)) return; // Only check every second to reduce performance impact
        PlayerPokeParty playerInstance = new PlayerPokeParty(event.player());
        for (Map.Entry<PokePartySlot, PartyEntry> entry : playerInstance.getPlayerPartyStatus().entrySet()) {
            PartyEntry partyEntry = entry.getValue();
            if (partyEntry.healthStatus() == PokeHealthStatus.FAINTED) {
                Pokemon faintedPokemon = partyEntry.pokemon();
                if (faintedPokemon != null) {
                    if (!playerInstance.popPokemonTotem(faintedPokemon)) {
                        playerInstance.alertPlayerAndRemovedPokemon(faintedPokemon);
                    }
                } else {
                    CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Failed to find fainted Pokemon in player party at slot: " + entry.getKey().getIndex());
                }
            }
        }
    }
}
