package dev.matthiesen.cobblehardcoremon.fabric;

import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import net.fabricmc.api.ModInitializer;

public final class CobbleHardcoreMonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var instance = CobbleHardcoreMonCommon.INSTANCE;
        instance.createInfoLog("Loading for Fabric Mod Loader");
        instance.initialize();
    }
}
