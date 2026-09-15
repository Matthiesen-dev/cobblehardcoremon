package dev.matthiesen.cobblehardcoremon.common.data;

import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerData extends SavedData {
    public static final String PLAYER_DATA_KEY = "playerData";
    public final Map<UUID, PlayerDataEntry> playerDataMap = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag playerDataTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerDataEntry> entry : playerDataMap.entrySet()) {
            playerDataTag.put(entry.getKey().toString(), entry.getValue().toCompoundTag());
        }
        compoundTag.put(PLAYER_DATA_KEY, playerDataTag);
        return compoundTag;
    }

    public static PlayerData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        PlayerData playerData = new PlayerData();
        CompoundTag playerDataTag = compoundTag.getCompound(PLAYER_DATA_KEY);
        for (String key : playerDataTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            PlayerDataEntry entry = PlayerDataEntry.fromCompoundTag(playerDataTag.getCompound(key));
            playerData.playerDataMap.put(uuid, entry);
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
        return playerData.playerDataMap.get(uuid);
    }

    public record PlayerDataEntry(
            boolean healthLinkEnabled
    ) {
        public static final String HEALTH_LINK_ENABLED_KEY = "healthLinkEnabled";

        public CompoundTag toCompoundTag() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(HEALTH_LINK_ENABLED_KEY, healthLinkEnabled);

            return tag;
        }

        public static PlayerDataEntry fromCompoundTag(CompoundTag tag) {
            boolean healthLinkEnabled = tag.getBoolean(HEALTH_LINK_ENABLED_KEY);

            return new PlayerDataEntry(healthLinkEnabled);
        }
    }
}
