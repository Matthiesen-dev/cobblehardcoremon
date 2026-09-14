package dev.matthiesen.cobblehardcoremon.common;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import dev.matthiesen.cobblehardcoremon.common.handlers.PlayerBattles;
import dev.matthiesen.cobblehardcoremon.common.handlers.PlayerPokeParty;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.api.platform.loader.ModConfigType;
import org.jetbrains.annotations.NotNull;

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

    private boolean isServerRunning = false;

    public boolean isServerRunning() {
        return isServerRunning;
    }

    public void initialize() {
        super.initialize();

        registerModConfig(MOD_ID, ModConfigType.SERVER, CobbleHardcoreMonConfig.SERVER_SPEC);

        PlatformEvents.SERVER_STARTED.subscribe(server -> isServerRunning = true);
        PlatformEvents.SERVER_STOPPING.subscribe(server -> isServerRunning = false);
        PlatformEvents.PLAYER_END_TICK.subscribe(PlayerPokeParty::handlePlayerTick);

        CobblemonEvents.BATTLE_STARTED_POST.subscribe(PlayerBattles::battleStartedPost);
        CobblemonEvents.BATTLE_FAINTED.subscribe(PlayerBattles::battlePokemonFainted);
        CobblemonEvents.BATTLE_VICTORY.subscribe(PlayerBattles::battleVictory);
        CobblemonEvents.BATTLE_FLED.subscribe(PlayerBattles::battleFled);

        createInfoLog("Initialized");
    }
}
