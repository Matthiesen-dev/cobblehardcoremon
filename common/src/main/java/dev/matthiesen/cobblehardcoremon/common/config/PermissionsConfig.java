package dev.matthiesen.cobblehardcoremon.common.config;

import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PermissionsConfig {
    public ModConfigSpec.EnumValue<PermissionLevel> command_hardcoremon;
    public ModConfigSpec.EnumValue<PermissionLevel> command_hardcoremon_setHealthLink;
    public ModConfigSpec.EnumValue<PermissionLevel> command_hardcoremon_getHealthLink;
    public ModConfigSpec.EnumValue<PermissionLevel> command_hardcoremon_forceHealthLink;
    public ModConfigSpec.EnumValue<PermissionLevel> command_hardcoremon_getHealthLinkOther;

    public PermissionsConfig(ModConfigSpec.Builder builder) {
        builder.comment("Permissions configuration for CobbleHardcoreMon mod")
                .translation("cobblehardcoremon.configuration.permissions")
                .push("permissions");
        builder.comment("Command permissions configuration")
                .translation("cobblehardcoremon.configuration.permissions.commands")
                .push("commands");

        command_hardcoremon = builder.comment("The permission level required to use the /hardcoremon command.")
                .translation("cobblehardcoremon.configuration.permissions.commands.hardcoremon")
                .defineEnum("hardcoremon", PermissionLevel.NONE);

        command_hardcoremon_setHealthLink = builder.comment("The permission level required to use the /hardcoremon setHealthLink command.")
                .translation("cobblehardcoremon.configuration.permissions.commands.setHealthLink")
                .defineEnum("setHealthLink", PermissionLevel.NONE);

        command_hardcoremon_getHealthLink = builder.comment("The permission level required to use the /hardcoremon getHealthLink command.")
                .translation("cobblehardcoremon.configuration.permissions.commands.getHealthLink")
                .defineEnum("getHealthLink", PermissionLevel.NONE);

        command_hardcoremon_forceHealthLink = builder.comment("The permission level required to use the /hardcoremon forceHealthLink command.")
                .translation("cobblehardcoremon.configuration.permissions.commands.forceHealthLink")
                .defineEnum("forceHealthLink", PermissionLevel.ALL_COMMANDS);

        command_hardcoremon_getHealthLinkOther = builder.comment("The permission level required to use the /hardcoremon getHealthLink <player> command.")
                .translation("cobblehardcoremon.configuration.permissions.commands.getHealthLinkOther")
                .defineEnum("getHealthLinkOther", PermissionLevel.ALL_COMMANDS);

        builder.pop(); // commands
        builder.pop(); // permissions
    }
}
