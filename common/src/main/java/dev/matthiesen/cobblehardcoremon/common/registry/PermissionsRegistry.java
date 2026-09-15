package dev.matthiesen.cobblehardcoremon.common.registry;

import dev.matthiesen.cobblehardcoremon.common.CobbleHardcoreMonCommon;
import dev.matthiesen.cobblehardcoremon.common.config.CobbleHardcoreMonConfig;
import dev.matthiesen.matthiesen_core.common.api.permissions.Permission;
import dev.matthiesen.matthiesen_core.common.api.permissions.PermissionLevel;
import dev.matthiesen.matthiesen_core.common.utility.AbstractPermission;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

public final class PermissionsRegistry {
    public static Permission COMMAND_ROOT_PERMISSION = register(
            "command.hardcoremon",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon.get()
    );
    public static Permission COMMAND_SETHEALTHLINK_PERMISSION = register(
            "command.hardcoremon.set-health-link",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_setHealthLink.get()
    );
    public static Permission COMMAND_GETHEALTHLINK_PERMISSION = register(
            "command.hardcoremon.get-health-link",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_getHealthLink.get()
    );
    public static Permission COMMAND_FORCEHEALTHLINK_PERMISSION = register(
            "command.hardcoremon.force-health-link",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_forceHealthLink.get()
    );
    public static Permission COMMAND_GETHEALTHLINKOTHER_PERMISSION = register(
            "command.hardcoremon.get-health-link-other",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_getHealthLinkOther.get()
    );
    public static Permission COMMAND_SOULLINK_PERMISSION = register(
            "command.hardcoremon.soul-link",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_soulLink.get()
    );
    public static Permission COMMAND_SOULLINK_INVITE_PERMISSION = register(
            "command.hardcoremon.soul-link.invite",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_soulLink_invite.get()
    );
    public static Permission COMMAND_SOULLINK_ACCEPT_PERMISSION = register(
            "command.hardcoremon.soul-link.accept",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_soulLink_accept.get()
    );
    public static Permission COMMAND_SOULLINK_DECLINE_PERMISSION = register(
            "command.hardcoremon.soul-link.decline",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_soulLink_decline.get()
    );
    public static Permission COMMAND_SOULLINK_REMOVE_PERMISSION = register(
            "command.hardcoremon.soul-link.remove",
            CobbleHardcoreMonConfig.PERMISSIONS_CONFIG.command_hardcoremon_soulLink_remove.get()
    );

    public static void init() {}

    public static boolean checkPermission(CommandSourceStack source, Permission permission) {
        return CobbleHardcoreMonCommon.INSTANCE.getPermissionsManager().getPermissionValidator().hasPermission(source, permission);
    }

    public static boolean hasAnyPermission(List<Boolean> permissions) {
        for (boolean permission : permissions) {
            if (permission) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private static Permission register(String node, PermissionLevel level) {
        var newPermission = modPermission(node, level);
        CobbleHardcoreMonCommon.INSTANCE.getPermissionsManager().registerPermission(newPermission);
        return newPermission;
    }

    private static Permission modPermission(String node, PermissionLevel level) {
        return new AbstractPermission(node, level) {
            @Override
            protected String getModId() {
                return CobbleHardcoreMonCommon.MOD_ID;
            }

            @Override
            protected String getPermissionNamespace() {
                return "CobbleHardcoreMon";
            }
        };
    }
}
