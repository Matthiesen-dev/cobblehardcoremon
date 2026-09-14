package dev.matthiesen.cobblehardcoremon.common;

import dev.matthiesen.matthiesen_core.common.utility.item.ItemDecoder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class CobbleHardcoreMonConfig {
    public static final ServerConfig SERVER_CONFIG;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG = specPair.getLeft();
        SERVER_SPEC = specPair.getRight();
    }

    public static Item getTotemItem() {
        return ItemDecoder.stringToItem(SERVER_CONFIG.totemItemId.get(), Items.TOTEM_OF_UNDYING);
    }

    public static class ServerConfig {

        public ModConfigSpec.ConfigValue<String> totemItemId;

        public ModConfigSpec.BooleanValue battles_trackPlayerBattles;
        public ModConfigSpec.BooleanValue battles_trackNPCBattles;
        public ModConfigSpec.BooleanValue battles_trackWildBattles;

        public ModConfigSpec.ConfigValue<String> messages_totemConsumed;
        public ModConfigSpec.ConfigValue<String> messages_pokemonRemoved;

        public ServerConfig(ModConfigSpec.Builder builder) {
            builder.comment("Server configuration for CobbleHardcoreMon mod")
                    .translation("cobblehardcoremon.configuration.server")
                    .push("server");

            totemItemId = builder.comment("The item ID of the Totem item that prevents a Cobblemon from being lost when it faints.")
                    .translation("cobblehardcoremon.configuration.server.totemItemId")
                    .define("totemItemId", "minecraft:totem_of_undying");

            builder.comment("Battle tracking configuration. These settings determine which types of battles are tracked for Cobblemon fainting events.")
                    .translation("cobblehardcoremon.configuration.server.battles")
                    .push("battles");
            battles_trackPlayerBattles = builder.comment("Whether to track player battles.")
                    .translation("cobblehardcoremon.configuration.server.battles.trackPlayerBattles")
                    .define("trackPlayerBattles", true);
            battles_trackNPCBattles = builder.comment("Whether to track NPC battles.")
                    .translation("cobblehardcoremon.configuration.server.battles.trackNPCBattles")
                    .define("trackNPCBattles", true);
            battles_trackWildBattles = builder.comment("Whether to track wild battles.")
                    .translation("cobblehardcoremon.configuration.server.battles.trackWildBattles")
                    .define("trackWildBattles", true);
            builder.pop(); // battles

            builder.comment("Messages displayed to the player when a Cobblemon faints and is removed from their party. You can use {pokemon} as a placeholder for the Pokemon's name.")
                    .translation("cobblehardcoremon.configuration.server.messages")
                    .push("messages");
            messages_totemConsumed = builder.comment("Message displayed when a Cobblemon faints and the player has a Totem item. The Totem item will be consumed and the Cobblemon will be saved.")
                    .translation("cobblehardcoremon.configuration.server.messages.totemConsumed")
                    .define("totemConsumed", "{pokemon} was holding a totem, which has been consumed to prevent it from being removed from your party.");
            messages_pokemonRemoved = builder.comment("Message displayed when a Cobblemon faints and is removed from the player's party.")
                    .translation("cobblehardcoremon.configuration.server.messages.pokemonRemoved")
                    .define("pokemonRemoved", "{pokemon} has fainted and is not holding a totem, and has been removed from your party.");
            builder.pop(); // messages

            builder.pop(); // server
        }
    }
}
