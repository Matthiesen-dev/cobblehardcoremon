package dev.matthiesen.cobblehardcoremon.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ServerConfig {

    public ModConfigSpec.ConfigValue<String> totemItemId;
    public ModConfigSpec.BooleanValue globalHealthLinkEnabled;

    public ModConfigSpec.BooleanValue battles_trackPlayerBattles;
    public ModConfigSpec.BooleanValue battles_trackNPCBattles;
    public ModConfigSpec.BooleanValue battles_trackWildBattles;

    public ModConfigSpec.ConfigValue<String> messages_totemConsumed;
    public ModConfigSpec.ConfigValue<String> messages_pokemonRemoved;
    public ModConfigSpec.ConfigValue<String> messages_helpTitle;
    public ModConfigSpec.ConfigValue<String> messages_noCommandsAvailable;
    public ModConfigSpec.ConfigValue<String> messages_noPermissions;
    public ModConfigSpec.ConfigValue<String> messages_errorOccurred;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_helpTitle;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_setHealthLinkText;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_getHealthLinkText;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_forceHealthLinkText;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_getHealthLinkOtherText;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_getHealthLinkSelfResponse;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_healthLinkSelfResponse;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_forceHealthLinkOtherResponse;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_forceHealthLinkOtherTarget;
    public ModConfigSpec.ConfigValue<String> messages_healthLink_getHealthLinkOtherResponse;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_helpTitle;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_status;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_invitePlayer;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_acceptInvite;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_declineInvite;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_removeSoulLink;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_getSoulLinkSelf_null;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_getSoulLinkSelf_invalid;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_getSoulLinkSelf_valid;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_invites_noPendingInvites;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_invites_invalidUUID;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_remove_failed_noActiveLink;
    public ModConfigSpec.ConfigValue<String> messages_soulLink_remove_failed;

    public ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Server configuration for CobbleHardcoreMon mod")
                .translation("cobblehardcoremon.configuration.server")
                .push("server");

        totemItemId = builder.comment("The item ID of the Totem item that prevents a Cobblemon from being lost when it faints.")
                .translation("cobblehardcoremon.configuration.server.totemItemId")
                .define("totemItemId", "minecraft:totem_of_undying");
        globalHealthLinkEnabled = builder.comment("Whether the Health Link feature is enabled by default for all players. If set to false, players can enable it individually in their settings.")
                .translation("cobblehardcoremon.configuration.server.globalHealthLinkEnabled")
                .define("globalHealthLinkEnabled", false);

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
        messages_helpTitle = builder.comment("Title for the help command output.")
                .translation("cobblehardcoremon.configuration.server.messages.helpTitle")
                .define("helpTitle", "CobbleHardcoreMon Commands");
        messages_noCommandsAvailable = builder.comment("Message displayed when a player has no commands available due to lack of permissions.")
                .translation("cobblehardcoremon.configuration.server.messages.noCommandsAvailable")
                .define("noCommandsAvailable", "No commands available");
        messages_noPermissions = builder.comment("Message displayed when a player tries to use a command they do not have permission for.")
                .translation("cobblehardcoremon.configuration.server.messages.noPermissions")
                .define("noPermissions", "You do not have permission to use any commands.");
        messages_errorOccurred = builder.comment("Message displayed when an error occurs while executing a command.")
                .translation("cobblehardcoremon.configuration.server.messages.errorOccurred")
                .define("errorOccurred", "An error occurred while executing the command: {error}");
        messages_healthLink_helpTitle = builder.comment("Title for the health link help command output.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.helpTitle")
                .define("healthLink_helpTitle", "Health Link Commands");
        messages_healthLink_setHealthLinkText = builder.comment("Description for the setHealthLink command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.setHealthLinkText")
                .define("healthLink_setHealthLinkText", "Toggle health link for your party");
        messages_healthLink_getHealthLinkText = builder.comment("Description for the getHealthLink command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.getHealthLinkText")
                .define("healthLink_getHealthLinkText", "Get health link status for your party");
        messages_healthLink_forceHealthLinkText = builder.comment("Description for the forceHealthLink command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.forceHealthLinkText")
                .define("healthLink_forceHealthLinkText", "Force health link for another player's party");
        messages_healthLink_getHealthLinkOtherText = builder.comment("Description for the getHealthLinkOther command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.getHealthLinkOtherText")
                .define("healthLink_getHealthLinkOtherText", "Get health link status for another player's party");
        messages_healthLink_getHealthLinkSelfResponse = builder.comment("Response message for the getHealthLink command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.getHealthLinkSelfResponse")
                .define("healthLink_getHealthLinkSelfResponse", "Health link for your party is currently: {status}");
        messages_healthLink_healthLinkSelfResponse = builder.comment("Response message for the setHealthLink command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.healthLinkSelfResponse")
                .define("healthLink_healthLinkSelfResponse", "Health link for your party has been set to: {status}");
        messages_healthLink_forceHealthLinkOtherResponse = builder.comment("Response message for the forceHealthLink command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.forceHealthLinkOtherResponse")
                .define("healthLink_forceHealthLinkOtherResponse", "Health link for {player}'s party has been set to: {status}");
        messages_healthLink_forceHealthLinkOtherTarget = builder.comment("Message sent to the target player when their health link is forced by another player.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.forceHealthLinkOtherTarget")
                .define("healthLink_forceHealthLinkOtherTarget", "Your party's health link has been set to: {status} by {player}");
        messages_healthLink_getHealthLinkOtherResponse = builder.comment("Response message for the getHealthLinkOther command.")
                .translation("cobblehardcoremon.configuration.server.messages.healthLink.getHealthLinkOtherResponse")
                .define("healthLink_getHealthLinkOtherResponse", "Health link for {player}'s party is currently: {status}");
        messages_soulLink_helpTitle = builder.comment("Title for the soul link help command output.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.helpTitle")
                .define("soulLink_helpTitle", "Soul Link Commands");
        messages_soulLink_status = builder.comment("Help text for the status command.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.status")
                .define("soulLink_status", "Show your current Soul Link status.");
        messages_soulLink_invitePlayer = builder.comment("Help text for the invitePlayer command.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.invitePlayer")
                .define("soulLink_invitePlayer", "Invite another player to Soul Link.");
        messages_soulLink_acceptInvite = builder.comment("Help text for the acceptInvite command.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.acceptInvite")
                .define("soulLink_acceptInvite", "Accept a pending Soul Link invite.");
        messages_soulLink_declineInvite = builder.comment("Help text for the declineInvite command.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.declineInvite")
                .define("soulLink_declineInvite", "Decline a pending Soul Link invite.");
        messages_soulLink_removeSoulLink = builder.comment("Help text for the removeSoulLink command.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.removeSoulLink")
                .define("soulLink_removeSoulLink", "Remove your current Soul Link with another player.");
        messages_soulLink_getSoulLinkSelf_null = builder.comment("Message displayed when a player checks their Soul Link status and they do not have an active Soul Link.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.getSoulLinkSelf.null")
                .define("soulLink_getSoulLinkSelf_null", "You do not currently have an active Soul Link.");
        messages_soulLink_getSoulLinkSelf_invalid = builder.comment("Message displayed when a player checks their Soul Link status and their Soul Link data is invalid.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.getSoulLinkSelf.invalid")
                .define("soulLink_getSoulLinkSelf_invalid", "Your Soul Link data is invalid.");
        messages_soulLink_getSoulLinkSelf_valid = builder.comment("Message displayed when a player checks their Soul Link status and they have an active Soul Link.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.getSoulLinkSelf.valid")
                .define("soulLink_getSoulLinkSelf_valid", "You are Soul Linked with {partner}.");
        messages_soulLink_invites_noPendingInvites = builder.comment("Message displayed when a player checks their pending Soul Link invites and they have none.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.invites.noPendingInvites")
                .define("soulLink_invites_noPendingInvites", "You do not have any pending Soul Link invites from that player.");
        messages_soulLink_invites_invalidUUID = builder.comment("Message displayed when a player provides an invalid UUID for a Soul Link invite.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.invites.invalidUUID")
                .define("soulLink_invites_invalidUUID", "The provided player UUID is invalid.");
        messages_soulLink_remove_failed = builder.comment("Message displayed when a player attempts to remove their Soul Link but fails.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.remove.failed")
                .define("soulLink_remove_failed", "Failed to remove your current Soul Link.");
        messages_soulLink_remove_failed_noActiveLink = builder.comment("Message displayed when a player attempts to remove their Soul Link but they do not have an active Soul Link.")
                .translation("cobblehardcoremon.configuration.server.messages.soulLink.remove.failed.noActiveLink")
                .define("soulLink_remove_failed_noActiveLink", "You do not currently have an active Soul Link to remove.");
        builder.pop(); // messages

        builder.pop(); // server
    }
}
