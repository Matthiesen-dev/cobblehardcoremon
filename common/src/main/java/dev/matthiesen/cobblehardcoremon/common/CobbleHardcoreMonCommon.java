package dev.matthiesen.cobblehardcoremon.common;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import dev.matthiesen.cobblehardcoremon.common.config.CobbleHardcoreMonConfig;
import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.cobblehardcoremon.common.handlers.MolangExt;
import dev.matthiesen.cobblehardcoremon.common.registry.CommandRegistry;
import dev.matthiesen.cobblehardcoremon.common.handlers.PlayerBattles;
import dev.matthiesen.cobblehardcoremon.common.handlers.PlayerPokeParty;
import dev.matthiesen.cobblehardcoremon.common.registry.PermissionsRegistry;
import dev.matthiesen.libs.faststats.Token;
import dev.matthiesen.matthiesen_core.common.AbstractCommonMod;
import dev.matthiesen.matthiesen_core.common.api.events.PlatformEvents;
import dev.matthiesen.matthiesen_core.common.api.platform.loader.ModConfigType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class CobbleHardcoreMonCommon extends AbstractCommonMod {
    public static final String MOD_ID = "cobblehardcoremon";
    public static final String MOD_NAME = "CobbleHardcoreMon";
    public static @Token final String METRICS_TOKEN = "768f3c7f3b02526ca25e65c50a0d6d98";
    public static final CobbleHardcoreMonCommon INSTANCE = new CobbleHardcoreMonCommon();

    public CobbleHardcoreMonCommon() {
        super(MOD_ID, MOD_NAME);
    }

    public static ResourceLocation modResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public String modConfigPath(String path) {
        return MOD_ID + "/" + path + ".toml";
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

        registerModConfig(MOD_ID, ModConfigType.SERVER, CobbleHardcoreMonConfig.SERVER_SPEC, modConfigPath("server"));
        registerModConfig(MOD_ID, ModConfigType.STARTUP, CobbleHardcoreMonConfig.PERMISSIONS_SPEC, modConfigPath("permissions"));

        PlatformEvents.SERVER_STARTED.subscribe(server -> isServerRunning = true);
        PlatformEvents.SERVER_STOPPING.subscribe(server -> isServerRunning = false);
        PlatformEvents.PLAYER_END_TICK.subscribe(PlayerPokeParty::handlePlayerTick);
        PlatformEvents.PLAYER_JOIN.subscribe(PlayerData::onPlayerLogin);

        CobblemonEvents.BATTLE_STARTED_POST.subscribe(PlayerBattles::battleStartedPost);
        CobblemonEvents.BATTLE_FAINTED.subscribe(PlayerBattles::battlePokemonFainted);
        CobblemonEvents.BATTLE_VICTORY.subscribe(PlayerBattles::battleVictory);
        CobblemonEvents.BATTLE_FLED.subscribe(PlayerBattles::battleFled);

        PermissionsRegistry.init();
        CommandRegistry.init();
        MolangExt.init();

        createInfoLog("Initialized");
    }
}
