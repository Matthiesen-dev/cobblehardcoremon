package dev.matthiesen.cobblehardcoremon.common.handlers;

import com.bedrockk.molang.runtime.MoParams;
import com.bedrockk.molang.runtime.value.DoubleValue;
import com.bedrockk.molang.runtime.value.StringValue;
import com.cobblemon.mod.common.api.molang.ObjectValue;
import com.cobblemon.mod.common.api.molang.function.PlayerMoLangFunctions;
import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import dev.matthiesen.cobblehardcoremon.common.data.PlayerData;
import dev.matthiesen.cobblehardcoremon.common.data.SoulLinkDataEntry;
import dev.matthiesen.matthiesen_core.common.utility.player_data.ServerUser;
import kotlin.jvm.functions.Function1;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public final class MolangExt {
    public static void init() {
        PlayerMoLangFunctions.INSTANCE.getCustom().add(player -> {
            Map<String, Function1<MoParams, Object>> map = new HashMap<>();

            // q.player.hardcoremon() -> { "playerUUID": "string" }
            // q.player.hardcoremon.get_health_link() -> returns 1 if health link is enabled, 0 otherwise
            // q.player.hardcoremon.set_health_link(<enabled int>) -> returns 1 for success, 0 for failure
            map.put("hardcoremon", HardcoreMonObject.getCustom(player));

            return map;
        });
    }

    public record HardcoreMonObject(Player player) {
        public static Function1<MoParams, Object> getCustom(Player cPlayer) {
            return params -> {
                var data = new HardcoreMonObject(cPlayer);
                return data.asMolangValue();
            };
        }

        public Map<String, ? extends Function<MoParams, Object>> getPlayerFunctions() {
            Map<String, Function<MoParams, Object>> map = new HashMap<>();

            // q.player.hardcoremon.get_health_link() -> returns 1 if health link is enabled, 0 otherwise
            map.put("get_health_link", params -> {
                try {
                    var playerDataEntry = PlayerData.getPlayerDataEntry(player.getUUID());
                    return playerDataEntry.healthLinkEnabled() ? 1 : 0;
                } catch (Exception e) {
                    CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Error in get_health_link: " + e.getMessage());
                    return 0;
                }
            });

            // q.player.hardcoremon.set_health_link(<enabled int>) -> returns 1 for success, 0 for failure
            map.put("set_health_link", params -> {
                try {
                    int enabled = params.getInt(0);

                    var playerDataEntry = PlayerData.getPlayerDataEntry(player.getUUID());
                    playerDataEntry.setHealthLinkEnabled(enabled == 1);
                    PlayerData.setPlayerDataEntry(player.getUUID(), playerDataEntry);
                    return 1;
                } catch (Exception e) {
                    CobbleHardcoreMonCommon.INSTANCE.createErrorLog("Error in set_health_link: " + e.getMessage());
                    return 0;
                }
            });

            // q.player.hardcoremon.soul_link() -> { "playerUUID": "string", "soulLinkPartnerUUID": "string", "soulLinkPartnerName": "string" }
            // q.player.hardcoremon.soul_link.has_partner() -> returns 1 if the player has a soul link partner, 0 otherwise
            // q.player.hardcoremon.soul_link.get_partner_uuid() -> returns the UUID of the soul link partner as a string, 0 otherwise
            // q.player.hardcoremon.soul_link.get_partner_name() -> returns the name of the soul link partner as a string, 0 otherwise
            map.put("soul_link", SoulLinkObject.getCustom(player));

            return map;
        }

        public String makeString(HardcoreMonObject obj) {
            return "{" +
                    "\"playerUUID\": \"" + obj.player.getUUID() + "\"" +
                    "}";
        }

        public ObjectValue<HardcoreMonObject> asMolangValue() {
            ObjectValue<HardcoreMonObject> value = new ObjectValue<>(this, this::makeString, d -> 1.0);
            value.functions.putAll(getPlayerFunctions());
            return value;
        }
    }

    public record SoulLinkObject(Player player) {
        public static Function<MoParams, Object> getCustom(Player cPlayer) {
            return params -> {
                var data = new SoulLinkObject(cPlayer);
                return data.asMolangValue();
            };
        }

        public SoulLinkDataEntry getPlayerSoulLinkData() {
            return PlayerData.getSoulLinkByPlayerUUID(player.getUUID());
        }

        public UUID getSoulLinkPartnerUUID() {
            SoulLinkDataEntry soulLinkData = getPlayerSoulLinkData();
            if (soulLinkData == null) return null;
            UUID playerAUUID = soulLinkData.playerA();
            UUID playerBUUID = soulLinkData.playerB();

            if (player.getUUID().equals(playerAUUID)) {
                return playerBUUID;
            } else if (player.getUUID().equals(playerBUUID)) {
                return playerAUUID;
            } else {
                return null; // This should not happen if the data is consistent
            }
        }

        public String getSoulLinkPartnerName() {
            UUID partnerUUID = getSoulLinkPartnerUUID();
            if (partnerUUID == null) return null;
            ServerUser partnerUser = new ServerUser(partnerUUID);
            return partnerUser.getUsername();
        }

        public Map<String, ? extends Function<MoParams, Object>> getPlayerFunctions() {
            Map<String, Function<MoParams, Object>> map = new HashMap<>();

            // q.player.hardcoremon.soul_link.has_partner() -> returns 1 if the player has a soul link partner, 0 otherwise
            map.put("has_partner", params -> getSoulLinkPartnerUUID() != null ? new DoubleValue(1) : new DoubleValue(0));

            // q.player.hardcoremon.soul_link.get_partner_uuid() -> returns the UUID of the soul link partner as a string, 0 otherwise
            map.put("get_partner_uuid", params -> {
                UUID partnerUUID = getSoulLinkPartnerUUID();
                return partnerUUID != null ? new StringValue(partnerUUID.toString()) : new DoubleValue(0);
            });

            // q.player.hardcoremon.soul_link.get_partner_name() -> returns the name of the soul link partner as a string, 0 otherwise
            map.put("get_partner_name", params -> getSoulLinkPartnerName() != null ? new StringValue(getSoulLinkPartnerName()) : new DoubleValue(0));

            return map;
        }

        public String makeString(SoulLinkObject obj) {
            return "{" +
                    "\"playerUUID\": \"" + obj.player.getUUID() + "\"" +
                    "\"soulLinkPartnerUUID\": " + (obj.getSoulLinkPartnerUUID() != null ? "\"" + obj.getSoulLinkPartnerUUID() + "\"" : "null") +
                    "\"soulLinkPartnerName\": " + (obj.getSoulLinkPartnerUUID() != null ? "\"" + obj.getSoulLinkPartnerName() + "\"" : "null") +
                    "}";
        }

        public ObjectValue<SoulLinkObject> asMolangValue() {
            ObjectValue<SoulLinkObject> value = new ObjectValue<>(this, this::makeString, d -> 1.0);
            value.functions.putAll(getPlayerFunctions());
            return value;
        }
    }
}
