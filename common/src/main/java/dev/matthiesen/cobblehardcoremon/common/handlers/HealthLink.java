package dev.matthiesen.cobblehardcoremon.common.handlers;

import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class HealthLink {
    private static final ResourceLocation HEALTH_MODIFIER_ID =
            CobbleHardcoreMonCommon.modResource("cobblemon_party_health");

    public static void overridePlayerMaxHealth(ServerPlayer player) {
        if (player.level().isClientSide()) return;

        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        int partySize = getCobblemonPartySize(player);
        double targetHealth = partySize * 2.0;

        if (targetHealth < 0.5D) {
            targetHealth = 0.5D;
        }

        double baseVanillaHealth = maxHealthAttr.getBaseValue(); // This is 20.0 for players
        double modifierAmount = targetHealth - baseVanillaHealth;

        AttributeModifier existingModifier = maxHealthAttr.getModifier(HEALTH_MODIFIER_ID);
        if (existingModifier != null && existingModifier.amount() == modifierAmount) {
            return;
        }

        maxHealthAttr.removeModifier(HEALTH_MODIFIER_ID);

        maxHealthAttr.addTransientModifier(new AttributeModifier(
                HEALTH_MODIFIER_ID,
                modifierAmount,
                AttributeModifier.Operation.ADD_VALUE
        ));

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        double healthDifference = modifierAmount - (existingModifier != null ? existingModifier.amount() : 0);
        if (healthDifference > 0) {
            player.heal((float) healthDifference); // Heal the player by the difference if their max health increased
        }
    }

    private static int getCobblemonPartySize(ServerPlayer player) {
        return new PlayerPokeParty(player).getLivingPokemonCount();
    }
}