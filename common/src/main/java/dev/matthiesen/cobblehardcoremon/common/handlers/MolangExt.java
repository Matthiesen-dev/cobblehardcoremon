package dev.matthiesen.cobblehardcoremon.common.handlers;

import com.bedrockk.molang.runtime.MoParams;
import com.cobblemon.mod.common.api.molang.function.PlayerMoLangFunctions;
import dev.matthiesen.cobblehardcoremon.common.molang_data.HardcoreMonObject;
import kotlin.jvm.functions.Function1;

import java.util.HashMap;
import java.util.Map;

public final class MolangExt {
    public static void init() {
        PlayerMoLangFunctions.INSTANCE.getCustom().add(player -> {
            Map<String, Function1<MoParams, Object>> map = new HashMap<>();

            // q.player.hardcoremon() -> { "playerUUID": "string" }
            // q.player.hardcoremon.get_health_link() -> returns 1 if health link is enabled, 0 otherwise
            // q.player.hardcoremon.set_health_link(<enabled int>) -> returns 1 for success, 0 for failure
            // q.player.hardcoremon.soul_link() -> { "playerUUID": "string", "soulLinkPartnerUUID": "string", "soulLinkPartnerName": "string" }
            // q.player.hardcoremon.soul_link.has_partner() -> returns 1 if the player has a soul link partner, 0 otherwise
            // q.player.hardcoremon.soul_link.get_partner_uuid() -> returns the UUID of the soul link partner as a string, 0 otherwise
            // q.player.hardcoremon.soul_link.get_partner_name() -> returns the name of the soul link partner as a string, 0 otherwise
            map.put("hardcoremon", HardcoreMonObject.getCustom(player));

            return map;
        });
    }
}
