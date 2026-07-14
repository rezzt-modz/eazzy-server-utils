package dev.rezzt.eazzyserverutils.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.rezzt.eazzyserverutils.TimeUtils;
import dev.rezzt.eazzyserverutils.storage.BanEntry;
import dev.rezzt.eazzyserverutils.storage.BanManager;
import dev.rezzt.eazzyserverutils.storage.WarningManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public class PunishmentCommands {

    private static final String DEFAULT_KICK_REASON = "Kicked by staff";
    private static final String DEFAULT_BAN_REASON = "Banned by staff";
    private static final String DEFAULT_WARN_REASON = "Warned by staff";
    private static final String DEFAULT_IPBAN_REASON = "IP banned by staff";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("warn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", StringArgumentType.word())
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(PunishmentCommands::warn))));

        dispatcher.register(Commands.literal("warns")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(PunishmentCommands::warns)));

        dispatcher.register(Commands.literal("clearwarns")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(PunishmentCommands::clearWarns)));

        dispatcher.register(Commands.literal("kick")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(PunishmentCommands::kickNoReason)
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(PunishmentCommands::kick))));

        dispatcher.register(Commands.literal("ban")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(PunishmentCommands::banNoReason)
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(PunishmentCommands::ban))));

        dispatcher.register(Commands.literal("tempban")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", StringArgumentType.word())
                        .then(Commands.argument("duration", StringArgumentType.word())
                                .executes(PunishmentCommands::tempbanNoReason)
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(PunishmentCommands::tempban)))));

        dispatcher.register(Commands.literal("unban")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(PunishmentCommands::unban)));

        dispatcher.register(Commands.literal("ipban")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(PunishmentCommands::ipbanNoReason)
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(PunishmentCommands::ipban))));
    }

    private static int warn(CommandContext<CommandSourceStack> context) {
        try {
            String targetName = StringArgumentType.getString(context, "target");
            String reason = StringArgumentType.getString(context, "reason");
            MinecraftServer server = context.getSource().getServer();

            TargetResult target = resolveTarget(targetName, server);
            if (target == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.player_not_found"));
                return 0;
            }

            UUID issuerUUID = context.getSource().getPlayer() != null ? context.getSource().getPlayer().getUUID() : new UUID(0, 0);
            String issuerName = context.getSource().getPlayer() != null ? context.getSource().getPlayer().getName().getString() : "Console";

            WarningManager.warn(target.uuid, target.name, issuerUUID, issuerName, reason);
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warn.sender", target.name, reason), true);

            ServerPlayer online = server.getPlayerList().getPlayer(target.uuid);
            if (online != null) {
                online.sendSystemMessage(Component.translatable("command.eazzy_server_utils.warn.received", issuerName, reason));
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int warns(CommandContext<CommandSourceStack> context) {
        try {
            String targetName = StringArgumentType.getString(context, "target");
            MinecraftServer server = context.getSource().getServer();

            TargetResult target = resolveTarget(targetName, server);
            if (target == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.player_not_found"));
                return 0;
            }

            java.util.List<dev.rezzt.eazzyserverutils.storage.Warning> warnings = WarningManager.getWarnings(target.uuid);
            if (warnings.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warn.list.empty", target.name), false);
                return 1;
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < warnings.size(); i++) {
                dev.rezzt.eazzyserverutils.storage.Warning warning = warnings.get(i);
                if (i > 0) {
                    builder.append("§8, ");
                }
                builder.append("§f#").append(i + 1)
                        .append(" §7(").append(warning.issuerName).append(") §f")
                        .append(warning.reason);
            }
            String finalList = builder.toString();
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warn.list", target.name, finalList), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int clearWarns(CommandContext<CommandSourceStack> context) {
        try {
            String targetName = StringArgumentType.getString(context, "target");
            MinecraftServer server = context.getSource().getServer();

            TargetResult target = resolveTarget(targetName, server);
            if (target == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.player_not_found"));
                return 0;
            }

            WarningManager.clearWarnings(target.uuid);
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.warn.cleared", target.name), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int kickNoReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return kickInternal(context, DEFAULT_KICK_REASON);
    }

    private static int kick(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String reason = StringArgumentType.getString(context, "reason");
        return kickInternal(context, reason);
    }

    private static int kickInternal(CommandContext<CommandSourceStack> context, String reason) throws CommandSyntaxException {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            target.connection.disconnect(Component.translatable("command.eazzy_server_utils.kick.screen", reason));
            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.kick.sender", target.getDisplayName()), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int banNoReason(CommandContext<CommandSourceStack> context) {
        return banInternal(context, DEFAULT_BAN_REASON);
    }

    private static int ban(CommandContext<CommandSourceStack> context) {
        String reason = StringArgumentType.getString(context, "reason");
        return banInternal(context, reason);
    }

    private static int banInternal(CommandContext<CommandSourceStack> context, String reason) {
        try {
            String targetName = StringArgumentType.getString(context, "target");
            MinecraftServer server = context.getSource().getServer();

            TargetResult target = resolveTarget(targetName, server);
            if (target == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.player_not_found"));
                return 0;
            }

            BanEntry entry = createBanEntry(target, context, reason, 0, null, BanEntry.TYPE_BAN);
            BanManager.ban(target.uuid, entry);

            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.ban.sender", target.name), true);

            ServerPlayer online = server.getPlayerList().getPlayer(target.uuid);
            if (online != null) {
                online.connection.disconnect(BanManager.getBanMessage(entry));
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int tempbanNoReason(CommandContext<CommandSourceStack> context) {
        return tempbanInternal(context, DEFAULT_BAN_REASON);
    }

    private static int tempban(CommandContext<CommandSourceStack> context) {
        String reason = StringArgumentType.getString(context, "reason");
        return tempbanInternal(context, reason);
    }

    private static int tempbanInternal(CommandContext<CommandSourceStack> context, String reason) {
        try {
            String targetName = StringArgumentType.getString(context, "target");
            String durationInput = StringArgumentType.getString(context, "duration");
            MinecraftServer server = context.getSource().getServer();

            long duration;
            try {
                duration = TimeUtils.parseDuration(durationInput);
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.invalid_duration"));
                return 0;
            }

            TargetResult target = resolveTarget(targetName, server);
            if (target == null) {
                context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.player_not_found"));
                return 0;
            }

            long expiresAt = System.currentTimeMillis() + duration;
            BanEntry entry = createBanEntry(target, context, reason, expiresAt, null, BanEntry.TYPE_TEMPBAN);
            BanManager.ban(target.uuid, entry);

            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.tempban.sender", target.name, TimeUtils.formatDuration(duration)), true);

            ServerPlayer online = server.getPlayerList().getPlayer(target.uuid);
            if (online != null) {
                online.connection.disconnect(BanManager.getBanMessage(entry));
            }
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int unban(CommandContext<CommandSourceStack> context) {
        try {
            String targetName = StringArgumentType.getString(context, "target");
            MinecraftServer server = context.getSource().getServer();

            boolean success = BanManager.unbanByName(targetName, server);
            if (success) {
                context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.unban.sender", targetName), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable("command.eazzy_server_utils.error.player_not_found"));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static int ipbanNoReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ipbanInternal(context, DEFAULT_IPBAN_REASON);
    }

    private static int ipban(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String reason = StringArgumentType.getString(context, "reason");
        return ipbanInternal(context, reason);
    }

    private static int ipbanInternal(CommandContext<CommandSourceStack> context, String reason) throws CommandSyntaxException {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            String ip = extractIp(target);
            if (ip == null || ip.isEmpty()) {
                context.getSource().sendFailure(Component.literal("Could not determine player IP."));
                return 0;
            }

            TargetResult targetResult = new TargetResult(target.getUUID(), target.getGameProfile().getName());
            BanEntry entry = createBanEntry(targetResult, context, reason, 0, ip, BanEntry.TYPE_IPBAN);
            BanManager.ipBan(ip, entry);

            context.getSource().sendSuccess(() -> Component.translatable("command.eazzy_server_utils.ipban.sender", target.getDisplayName(), ip), true);
            target.connection.disconnect(BanManager.getBanMessage(entry));
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }

    private static BanEntry createBanEntry(TargetResult target, CommandContext<CommandSourceStack> context, String reason, long expiresAt, String ip, String type) {
        UUID issuerUUID = context.getSource().getPlayer() != null ? context.getSource().getPlayer().getUUID() : new UUID(0, 0);
        String issuerName = context.getSource().getPlayer() != null ? context.getSource().getPlayer().getName().getString() : "Console";
        return new BanEntry(
                target.uuid,
                target.name,
                issuerUUID,
                issuerName,
                reason,
                System.currentTimeMillis(),
                expiresAt,
                ip,
                type
        );
    }

    private static TargetResult resolveTarget(String name, MinecraftServer server) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) {
            return new TargetResult(online.getUUID(), online.getGameProfile().getName());
        }
        Optional<GameProfile> profile = server.getProfileCache().get(name);
        return profile.map(gameProfile -> new TargetResult(gameProfile.getId(), gameProfile.getName())).orElse(null);
    }

    private static String extractIp(ServerPlayer player) {
        java.net.SocketAddress address = player.connection.connection.getRemoteAddress();
        if (address == null) {
            return null;
        }
        String raw = address.toString();
        if (raw.startsWith("/")) {
            raw = raw.substring(1);
        }
        int portIndex = raw.indexOf(':');
        return portIndex >= 0 ? raw.substring(0, portIndex) : raw;
    }

    private record TargetResult(UUID uuid, String name) {
    }
}
