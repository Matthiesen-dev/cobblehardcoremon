package dev.matthiesen.cobblehardcoremon.common.interfaces;

public enum PokePartySlot {
    SLOT_1(0),
    SLOT_2(1),
    SLOT_3(2),
    SLOT_4(3),
    SLOT_5(4),
    SLOT_6(5);

    private final int index;

    PokePartySlot(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static PokePartySlot fromIndex(int index) {
        for (PokePartySlot slot : PokePartySlot.values()) {
            if (slot.getIndex() == index) {
                return slot;
            }
        }
        throw new IllegalArgumentException("Invalid index for PokePartySlot: " + index);
    }

    public static int getMaxSlots() {
        return values().length;
    }
}
