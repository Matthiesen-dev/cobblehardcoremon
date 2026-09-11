package dev.matthiesen.cobblehardcoremon.neoforge;

import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import net.neoforged.fml.common.Mod;

@Mod(CobbleHardcoreMonCommon.MOD_ID)
public final class CobbleHardcoreMonNeoForge {
    public static final CobbleHardcoreMonCommon INSTANCE = CobbleHardcoreMonCommon.INSTANCE;

    public CobbleHardcoreMonNeoForge() {
        INSTANCE.createInfoLog("Loading for NeoForge Mod Loader");
        INSTANCE.initialize();
    }
}
