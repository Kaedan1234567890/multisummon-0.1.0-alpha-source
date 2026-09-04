package com.chillzone.multisummon;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class ChillZoneMultiSummon implements ModInitializer {
    public static final String PERMISSION = "chillzonemultisummon.command.msummon";
    private static final int MAX_AMOUNT = 100;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("msummon")
                    .requires(ChillZoneMultiSummon::hasPermission)
                    .then(
                        Commands.argument("entity", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                                for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
                                    String full = id.toString();
                                    String shortId = id.getNamespace().equals("minecraft") ? id.getPath() : full;
                                    if (shortId.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                        builder.suggest(shortId);
                                    } else if (full.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                                        builder.suggest(full);
                                    }
                                }
                                return builder.buildFuture();
                            })
                            .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1, MAX_AMOUNT))
                                    .executes(context -> {
                                        CommandSourceStack source = context.getSource();
                                        String entity = StringArgumentType.getString(context, "entity");
                                        int amount = IntegerArgumentType.getInteger(context, "amount");
                                        return summonMany(source, entity, amount, source.getPosition());
                                    })
                                    .then(
                                        Commands.argument("pos", Vec3Argument.vec3())
                                            .executes(context -> {
                                                CommandSourceStack source = context.getSource();
                                                String entity = StringArgumentType.getString(context, "entity");
                                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                                Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                return summonMany(source, entity, amount, pos);
                                            })
                                    )
                            )
                    )
            );
        });
    }

    private static int summonMany(CommandSourceStack source, String requestedEntity, int amount, Vec3 position) {
        ResourceLocation entityId = ResourceLocation.tryParse(
            requestedEntity.contains(":") ? requestedEntity : "minecraft:" + requestedEntity
        );

        if (entityId == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            source.sendFailure(Component.literal("Unknown entity: " + requestedEntity));
            return 0;
        }

        String cmd = "summon " + entityId + " " + position.x + " " + position.y + " " + position.z;
        int successful = 0;

        for (int i = 0; i < amount; i++) {
            try {
                int result = source.getServer().getCommands().performPrefixedCommand(source, cmd);
                if (result > 0) successful++;
            } catch (Exception ignored) {
                break;
            }
        }

        if (successful == 0) {
            source.sendFailure(Component.literal(
                "Could not summon " + requestedEntity + ". That entity may not be summonable."
            ));
            return 0;
        }

        final int count = successful;
        source.sendSuccess(
            () -> Component.literal("Summoned " + count + "x " + entityId + " at the same position."),
            false
        );
        return successful;
    }

    private static boolean hasPermission(CommandSourceStack source) {
        if (source.getEntity() == null) return true;
        if (!(source.getEntity() instanceof ServerPlayer player)) return false;

        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUUID());
            return user != null && user.getCachedData().getPermissionData()
                .checkPermission(PERMISSION).asBoolean();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
