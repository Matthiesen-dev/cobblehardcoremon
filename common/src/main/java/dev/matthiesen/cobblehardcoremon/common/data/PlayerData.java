package dev.matthiesen.cobblehardcoremon.common.data;

import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import dev.matthiesen.cobblehardcoremon.common.config.CobbleHardcoreMonConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerData extends SavedData {
    public static final String PLAYER_DATA_KEY = "playerData";
    public static final String SOUL_LINK_DATA_KEY = "soulLinkData";
    public final Map<UUID, PlayerDataEntry> playerDataMap = new HashMap<>();
    public final Map<UUID, SoulLinkDataEntry> soulLinkDataMap = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        // Player data
        CompoundTag playerDataTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerDataEntry> entry : playerDataMap.entrySet()) {
            playerDataTag.put(entry.getKey().toString(), entry.getValue().toCompoundTag());
        }
        compoundTag.put(PLAYER_DATA_KEY, playerDataTag);

        // Soul link data
        CompoundTag soulLinkDataTag = new CompoundTag();
        for (Map.Entry<UUID, SoulLinkDataEntry> entry : soulLinkDataMap.entrySet()) {
            soulLinkDataTag.put(entry.getKey().toString(), entry.getValue().toCompoundTag());
        }
        compoundTag.put(SOUL_LINK_DATA_KEY, soulLinkDataTag);

        return compoundTag;
    }

    public static PlayerData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        PlayerData playerData = new PlayerData();

        // Load player data
        CompoundTag playerDataTag = compoundTag.getCompound(PLAYER_DATA_KEY);
        for (String key : playerDataTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            PlayerDataEntry entry = PlayerDataEntry.fromCompoundTag(playerDataTag.getCompound(key));
            playerData.playerDataMap.put(uuid, entry);
        }

        // Soul link data
        CompoundTag soulLinkDataTag = compoundTag.getCompound(SOUL_LINK_DATA_KEY);
        for (String key : soulLinkDataTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            SoulLinkDataEntry entry = SoulLinkDataEntry.fromCompoundTag(soulLinkDataTag.getCompound(key));
            playerData.soulLinkDataMap.put(uuid, entry);
        }

        return playerData;
    }

    public static final SavedData.Factory<PlayerData> FACTORY = new SavedData.Factory<>(
            PlayerData::new,
            PlayerData::load,
            null
    );

    public static PlayerData getPlayerData() {
        return CobbleHardcoreMonCommon.INSTANCE.getCommonUtils()
                .getServer().overworld().getDataStorage()
                .computeIfAbsent(PlayerData.FACTORY, CobbleHardcoreMonCommon.MOD_ID);
    }

    public static PlayerDataEntry getPlayerDataEntry(UUID uuid) {
        PlayerData playerData = getPlayerData();
        return playerData.playerDataMap.computeIfAbsent(uuid, k ->
                new PlayerDataEntry(CobbleHardcoreMonConfig.SERVER_CONFIG.globalHealthLinkEnabled.getAsBoolean()));
    }

    public static void setPlayerDataEntry(UUID uuid, PlayerDataEntry entry) {
        PlayerData playerData = getPlayerData();
        playerData.playerDataMap.put(uuid, entry);
        playerData.setDirty();
    }

    public static void setHealthLinkEnabled(UUID uuid, boolean enabled) {
        if (CobbleHardcoreMonConfig.SERVER_CONFIG.globalHealthLinkEnabled.getAsBoolean()) {
            // If the global health link is enabled, we don't allow individual players to change their setting.
            return;
        }
        PlayerDataEntry entry = getPlayerDataEntry(uuid);
        entry.setHealthLinkEnabled(enabled);
        setPlayerDataEntry(uuid, entry);
    }

    @SuppressWarnings("unused")
    public static SoulLinkDataEntry getSoulLinkByPlayerUUID(UUID playerUUID) {
        PlayerData playerData = getPlayerData();
        // try to get the soul link directly from player first
        var playerDataEntry = getPlayerDataEntry(playerUUID);
        var soulLinkMapUUID = playerDataEntry.getSoulLinkMapUUID();
        if (soulLinkMapUUID != null) {
            var entry = playerData.soulLinkDataMap.get(soulLinkMapUUID);
            if (entry != null) {
                return entry;
            }
        }

        // if not found, iterate through all soul links to find one that contains the player
        for (var entry : playerData.soulLinkDataMap.entrySet()) {
            if (entry.getValue().playerA().equals(playerUUID) || entry.getValue().playerB().equals(playerUUID)) {
                // update the player's soul link reference for faster access next time
                playerDataEntry.setSoulLinkMapUUID(entry.getKey());
                setPlayerDataEntry(playerUUID, playerDataEntry);
                return entry.getValue();
            }
        }
        return null;
    }
}
