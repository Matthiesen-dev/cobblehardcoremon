package dev.matthiesen.cobblehardcoremon.common.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record SoulLinkDataEntry(UUID playerA, UUID playerB) {
    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("playerA", playerA);
        tag.putUUID("playerB", playerB);
        return tag;
    }

    public static SoulLinkDataEntry fromCompoundTag(CompoundTag tag) {
        UUID playerA = tag.getUUID("playerA");
        UUID playerB = tag.getUUID("playerB");
        return new SoulLinkDataEntry(playerA, playerB);
    }
}
