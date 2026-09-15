package dev.matthiesen.cobblehardcoremon.common.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class PlayerDataEntry {
    public static final String HEALTH_LINK_ENABLED_KEY = "healthLinkEnabled";
    public static final String SOUL_LINKED_PLAYER_UUID_KEY = "soulLinkedPlayerUUID";

    private boolean healthLinkEnabled;
    private UUID soulLinkMapUUID;

    public PlayerDataEntry(boolean healthLinkEnabled) {
        this.healthLinkEnabled = healthLinkEnabled;
        this.soulLinkMapUUID = null;
    }

    public PlayerDataEntry(boolean healthLinkEnabled, UUID soulLinkMapUUID) {
        this.healthLinkEnabled = healthLinkEnabled;
        this.soulLinkMapUUID = soulLinkMapUUID;
    }

    public static PlayerDataEntry fromCompoundTag(CompoundTag tag) {
        boolean healthLinkEnabled = tag.getBoolean(HEALTH_LINK_ENABLED_KEY);
        UUID soulLinkedPlayerUUID = tag.contains(SOUL_LINKED_PLAYER_UUID_KEY) ? UUID.fromString(tag.getString(SOUL_LINKED_PLAYER_UUID_KEY)) : null;
        return new PlayerDataEntry(healthLinkEnabled, soulLinkedPlayerUUID);
    }

    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(HEALTH_LINK_ENABLED_KEY, healthLinkEnabled);
        if (soulLinkMapUUID != null) {
            tag.putString(SOUL_LINKED_PLAYER_UUID_KEY, soulLinkMapUUID.toString());
        }
        return tag;
    }

    public boolean healthLinkEnabled() {
        return healthLinkEnabled;
    }

    public void setHealthLinkEnabled(boolean enabled) {
        this.healthLinkEnabled = enabled;
    }

    public UUID getSoulLinkMapUUID() {
        return soulLinkMapUUID;
    }

    public void setSoulLinkMapUUID(UUID soulLinkMapUUID) {
        this.soulLinkMapUUID = soulLinkMapUUID;
    }
}
