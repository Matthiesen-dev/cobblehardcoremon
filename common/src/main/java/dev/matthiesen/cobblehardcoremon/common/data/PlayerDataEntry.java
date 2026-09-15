package dev.matthiesen.cobblehardcoremon.common.data;

import net.minecraft.nbt.CompoundTag;

public final class PlayerDataEntry {
    public static final String HEALTH_LINK_ENABLED_KEY = "healthLinkEnabled";
    public static final String HAS_SOUL_LINK_KEY = "hasSoulLink";

    private boolean healthLinkEnabled;
    private boolean hasSoulLink;

    public PlayerDataEntry(boolean healthLinkEnabled, boolean hasSoulLink) {
        this.healthLinkEnabled = healthLinkEnabled;
        this.hasSoulLink = hasSoulLink;
    }

    public static PlayerDataEntry fromCompoundTag(CompoundTag tag) {
        boolean healthLinkEnabled = tag.getBoolean(HEALTH_LINK_ENABLED_KEY);
        boolean hasSoulLink = tag.getBoolean(HAS_SOUL_LINK_KEY);
        return new PlayerDataEntry(healthLinkEnabled, hasSoulLink);
    }

    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(HEALTH_LINK_ENABLED_KEY, healthLinkEnabled);
        tag.putBoolean(HAS_SOUL_LINK_KEY, hasSoulLink);
        return tag;
    }

    public boolean healthLinkEnabled() {
        return healthLinkEnabled;
    }

    public void setHealthLinkEnabled(boolean enabled) {
        this.healthLinkEnabled = enabled;
    }

    public boolean hasSoulLink() {
        return hasSoulLink;
    }

    public void setHasSoulLink(boolean hasSoulLink) {
        this.hasSoulLink = hasSoulLink;
    }
}
